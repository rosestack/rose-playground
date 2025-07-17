package io.github.rose.i18n.core;

import io.github.rose.i18n.*;
import io.github.rose.i18n.interpolation.DefaultMessageInterpolator;
import io.github.rose.i18n.interpolation.MessageInterpolator;
import io.github.rose.i18n.spi.I18nMessageChangeListener;
import io.github.rose.i18n.spi.I18nMessageProvider;
import io.github.rose.i18n.spi.I18nProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 组合式国际化消息源实现
 *
 * <p>基于SPI机制的组合式消息源，支持多个消息提供者的组合使用。
 * 提供缓存、热重载、优先级排序等企业级特性。</p>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class CompositeI18nMessageSource implements I18nCompositeMessageSource, I18nMessageChangeListener {

    /**
     * 解决接口冲突：明确实现containsMessages方法
     */
    @Override
    public Map<String, Boolean> containsMessages(Set<String> codes, Locale locale) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Boolean> result = new HashMap<>();
        for (String code : codes) {
            result.put(code, containsMessage(code, locale));
        }
        return result;
    }

    private static final Logger logger = LoggerFactory.getLogger(CompositeI18nMessageSource.class);

    private final List<I18nMessageProvider> providers = new CopyOnWriteArrayList<>();
    private final Map<Locale, Map<String, String>> messageCache = new ConcurrentHashMap<>();
    private final Set<Locale> supportedLocales = ConcurrentHashMap.newKeySet();
    
    private Locale defaultLocale = Locale.getDefault();
    private MessageInterpolator messageInterpolator = new DefaultMessageInterpolator();
    private boolean cacheEnabled = true;
    private volatile boolean initialized = false;

    /**
     * 添加消息提供者
     * 
     * @param provider 消息提供者
     * @param config 配置信息
     */
    public void addProvider(I18nMessageProvider provider, I18nProviderConfig config) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        if (!provider.supports(config)) {
            throw new IllegalArgumentException("Provider " + provider.getName() + " does not support the given config");
        }

        try {
            provider.initialize(config);
            providers.add(provider);
            
            // 按优先级排序
            providers.sort(Comparator.comparingInt(I18nMessageProvider::getPriority));
            
            // 更新支持的语言环境
            supportedLocales.addAll(provider.getSupportedLocales());
            
            // 添加变更监听器
            if (provider.supportsHotReload()) {
                provider.addChangeListener(this);
            }
            
            logger.info("Added I18n message provider: {} with priority {}", 
                       provider.getName(), provider.getPriority());
        } catch (Exception e) {
            logger.error("Failed to add I18n message provider: " + provider.getName(), e);
            throw new RuntimeException("Failed to add provider: " + provider.getName(), e);
        }
    }

    /**
     * 移除消息提供者
     * 
     * @param providerName 提供者名称
     */
    public void removeProvider(String providerName) {
        providers.removeIf(provider -> {
            if (provider.getName().equals(providerName)) {
                try {
                    if (provider.supportsHotReload()) {
                        provider.removeChangeListener(this);
                    }
                    provider.destroy();
                    logger.info("Removed I18n message provider: {}", providerName);
                    return true;
                } catch (Exception e) {
                    logger.error("Error destroying provider: " + providerName, e);
                    return true; // 仍然移除，避免内存泄漏
                }
            }
            return false;
        });
        
        // 重新计算支持的语言环境
        recalculateSupportedLocales();
        
        // 清除缓存
        clearCache();
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (code == null) {
            return defaultMessage;
        }

        ensureInitialized();

        try {
            String message = getMessageInternal(code, locale);
            return messageInterpolator.interpolate(message, args, locale);
        } catch (I18nMessageNotFoundException e) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws I18nMessageNotFoundException {
        if (code == null) {
            throw new I18nMessageNotFoundException(code, locale);
        }

        ensureInitialized();

        String message = getMessageInternal(code, locale);
        return messageInterpolator.interpolate(message, args, locale);
    }

    @Override
    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }

        ensureInitialized();

        Map<String, String> result = new HashMap<>();
        for (String code : codes) {
            try {
                String message = getMessageInternal(code, locale);
                result.put(code, message);
            } catch (I18nMessageNotFoundException e) {
                // 忽略不存在的消息
            }
        }
        return result;
    }

    @Override
    public Map<String, String> getAllMessages(Locale locale) {
        ensureInitialized();

        Map<String, String> allMessages = new HashMap<>();
        
        // 按优先级倒序遍历，让高优先级的消息覆盖低优先级的
        for (int i = providers.size() - 1; i >= 0; i--) {
            I18nMessageProvider provider = providers.get(i);
            try {
                Map<String, String> providerMessages = provider.loadMessages(locale);
                allMessages.putAll(providerMessages);
            } catch (Exception e) {
                logger.warn("Failed to load messages from provider: " + provider.getName(), e);
            }
        }
        
        return allMessages;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return new HashSet<>(supportedLocales);
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public Locale getCurrentLocale() {
        // 从Spring的LocaleContextHolder获取当前语言环境
        try {
            Class<?> localeContextHolderClass = Class.forName("org.springframework.context.i18n.LocaleContextHolder");
            return (Locale) localeContextHolderClass.getMethod("getLocale").invoke(null);
        } catch (Exception e) {
            // 如果Spring不可用，返回默认语言环境
            return defaultLocale;
        }
    }

    @Override
    public void init() {
        if (initialized) {
            return;
        }

        logger.info("Initializing CompositeI18nMessageSource with {} providers", providers.size());

        // 先设置为已初始化，避免递归调用
        initialized = true;

        // 预加载常用消息到缓存
        if (cacheEnabled) {
            preloadCache();
        }

        logger.info("CompositeI18nMessageSource initialized successfully");
    }

    @Override
    public void destroy() {
        logger.info("Destroying CompositeI18nMessageSource");
        
        for (I18nMessageProvider provider : providers) {
            try {
                if (provider.supportsHotReload()) {
                    provider.removeChangeListener(this);
                }
                provider.destroy();
            } catch (Exception e) {
                logger.error("Error destroying provider: " + provider.getName(), e);
            }
        }
        
        providers.clear();
        messageCache.clear();
        supportedLocales.clear();
        initialized = false;
        
        logger.info("CompositeI18nMessageSource destroyed");
    }

    @Override
    public void refresh() {
        logger.info("Refreshing CompositeI18nMessageSource");
        clearCache();
        
        for (I18nMessageProvider provider : providers) {
            try {
                provider.refresh();
            } catch (Exception e) {
                logger.warn("Failed to refresh provider: " + provider.getName(), e);
            }
        }
        
        if (cacheEnabled) {
            preloadCache();
        }
    }

    @Override
    public boolean containsMessage(String code, Locale locale) {
        if (code == null) {
            return false;
        }

        ensureInitialized();

        try {
            getMessageInternal(code, locale);
            return true;
        } catch (I18nMessageNotFoundException e) {
            return false;
        }
    }

    @Override
    public Set<String> getMessageCodes(Locale locale) {
        ensureInitialized();

        Set<String> allCodes = new HashSet<>();
        for (I18nMessageProvider provider : providers) {
            try {
                Map<String, String> messages = provider.loadMessages(locale);
                allCodes.addAll(messages.keySet());
            } catch (Exception e) {
                logger.warn("Failed to get message codes from provider: " + provider.getName(), e);
            }
        }
        return allCodes;
    }

    @Override
    public void onMessageChanged(String key, String value, Locale locale, Object source, ChangeType changeType) {
        if (cacheEnabled) {
            // 清除相关缓存
            Map<String, String> localeCache = messageCache.get(locale);
            if (localeCache != null) {
                localeCache.remove(key);
            }
        }

        logger.debug("Message changed: key={}, locale={}, changeType={}, source={}",
                    key, locale, changeType, source);
    }

    /**
     * 设置默认语言环境
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * 设置消息插值器
     */
    public void setMessageInterpolator(MessageInterpolator messageInterpolator) {
        this.messageInterpolator = messageInterpolator;
    }

    /**
     * 设置是否启用缓存
     */
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        if (!cacheEnabled) {
            clearCache();
        }
    }

    /**
     * 获取消息提供者列表
     */
    public List<I18nMessageProvider> getProviders() {
        return new ArrayList<>(providers);
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        messageCache.clear();
        logger.debug("Message cache cleared");
    }

    /**
     * 内部获取消息方法
     */
    private String getMessageInternal(String key, Locale locale) throws I18nMessageNotFoundException {
        // 先从缓存获取
        if (cacheEnabled) {
            Map<String, String> localeCache = messageCache.get(locale);
            if (localeCache != null) {
                String cachedMessage = localeCache.get(key);
                if (cachedMessage != null) {
                    return cachedMessage;
                }
            }
        }

        // 从提供者获取消息
        for (I18nMessageProvider provider : providers) {
            try {
                Map<String, String> messages = provider.loadMessages(locale);
                String message = messages.get(key);
                if (message != null) {
                    // 缓存消息
                    if (cacheEnabled) {
                        messageCache.computeIfAbsent(locale, k -> new ConcurrentHashMap<>()).put(key, message);
                    }
                    return message;
                }
            } catch (Exception e) {
                logger.warn("Failed to load message from provider: " + provider.getName(), e);
            }
        }

        // 如果指定语言环境找不到，尝试默认语言环境
        if (!locale.equals(defaultLocale)) {
            try {
                return getMessageInternal(key, defaultLocale);
            } catch (I18nMessageNotFoundException e) {
                // 忽略，继续抛出原始异常
            }
        }

        throw new I18nMessageNotFoundException(key, locale);
    }

    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }

    /**
     * 预加载缓存
     */
    private void preloadCache() {
        logger.debug("Preloading message cache");
        
        for (Locale locale : supportedLocales) {
            try {
                Map<String, String> allMessages = getAllMessages(locale);
                messageCache.put(locale, new ConcurrentHashMap<>(allMessages));
            } catch (Exception e) {
                logger.warn("Failed to preload cache for locale: " + locale, e);
            }
        }
        
        logger.debug("Message cache preloaded for {} locales", supportedLocales.size());
    }

    /**
     * 重新计算支持的语言环境
     */
    private void recalculateSupportedLocales() {
        supportedLocales.clear();
        for (I18nMessageProvider provider : providers) {
            supportedLocales.addAll(provider.getSupportedLocales());
        }
    }
}
