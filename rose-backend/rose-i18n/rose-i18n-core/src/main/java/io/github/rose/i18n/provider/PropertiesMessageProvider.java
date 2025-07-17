package io.github.rose.i18n.provider;

import io.github.rose.i18n.spi.I18nMessageChangeListener;
import io.github.rose.i18n.spi.I18nMessageProvider;
import io.github.rose.i18n.spi.I18nProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Properties文件消息提供者
 * 
 * <p>从Properties文件加载国际化消息，支持热重载功能。</p>
 * 
 * <p>配置参数：</p>
 * <ul>
 *   <li>baseName: 基础文件名，如"messages"</li>
 *   <li>location: 文件位置，支持classpath:和file:前缀</li>
 *   <li>encoding: 文件编码，默认UTF-8</li>
 *   <li>hotReload: 是否启用热重载</li>
 *   <li>checkInterval: 热重载检查间隔（毫秒）</li>
 * </ul>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class PropertiesMessageProvider implements I18nMessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesMessageProvider.class);

    private String baseName;
    private String location;
    private String encoding = "UTF-8";
    private boolean hotReloadEnabled = false;
    private long checkInterval = 60000; // 1分钟
    private int priority = 0;

    private final Map<Locale, Properties> messageCache = new ConcurrentHashMap<>();
    private final Map<Locale, Long> lastModifiedTimes = new ConcurrentHashMap<>();
    private final List<I18nMessageChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final Set<Locale> supportedLocales = ConcurrentHashMap.newKeySet();

    private ScheduledExecutorService hotReloadExecutor;
    private WatchService watchService;
    private boolean initialized = false;

    @Override
    public String getName() {
        return "PropertiesMessageProvider";
    }

    @Override
    public boolean supports(I18nProviderConfig config) {
        return "properties".equals(config.getType()) || 
               config.getStringProperty("baseName") != null;
    }

    @Override
    public void initialize(I18nProviderConfig config) {
        this.baseName = config.getStringProperty("baseName");
        this.location = config.getStringProperty("location", "classpath:");
        this.encoding = config.getStringProperty("encoding", "UTF-8");
        this.hotReloadEnabled = config.getBooleanProperty("hotReload", false);
        this.checkInterval = config.getLongProperty("checkInterval", 60000L);
        this.priority = config.getIntProperty("priority", 0);

        if (baseName == null) {
            throw new IllegalArgumentException("baseName is required for PropertiesMessageProvider");
        }

        // 扫描并加载所有支持的语言环境
        scanSupportedLocales();
        
        // 预加载消息
        loadAllMessages();

        // 启动热重载
        if (hotReloadEnabled) {
            startHotReload();
        }

        initialized = true;
        logger.info("PropertiesMessageProvider initialized: baseName={}, location={}, supportedLocales={}", 
                   baseName, location, supportedLocales);
    }

    @Override
    public Map<String, String> loadMessages(Locale locale) {
        if (!initialized) {
            throw new IllegalStateException("Provider not initialized");
        }

        Properties properties = messageCache.get(locale);
        if (properties == null) {
            properties = loadPropertiesForLocale(locale);
            if (properties != null) {
                messageCache.put(locale, properties);
            }
        }

        if (properties == null) {
            return Collections.emptyMap();
        }

        Map<String, String> messages = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            messages.put(key, properties.getProperty(key));
        }
        return messages;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return new HashSet<>(supportedLocales);
    }

    @Override
    public boolean supportsHotReload() {
        return hotReloadEnabled;
    }

    @Override
    public void addChangeListener(I18nMessageChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(I18nMessageChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void refresh() {
        logger.info("Refreshing PropertiesMessageProvider");
        messageCache.clear();
        lastModifiedTimes.clear();
        loadAllMessages();
        
        // 通知监听器
        for (Locale locale : supportedLocales) {
            notifyListeners(locale);
        }
    }

    @Override
    public void destroy() {
        if (hotReloadExecutor != null) {
            hotReloadExecutor.shutdown();
            try {
                if (!hotReloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    hotReloadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                hotReloadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.warn("Error closing watch service", e);
            }
        }

        messageCache.clear();
        lastModifiedTimes.clear();
        listeners.clear();
        supportedLocales.clear();
        initialized = false;

        logger.info("PropertiesMessageProvider destroyed");
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 扫描支持的语言环境
     */
    private void scanSupportedLocales() {
        // 默认语言环境（无后缀）
        if (resourceExists(getResourcePath(null))) {
            supportedLocales.add(Locale.ROOT);
        }

        // 常见语言环境
        Locale[] commonLocales = {
            Locale.ENGLISH,
            Locale.SIMPLIFIED_CHINESE,
            Locale.TRADITIONAL_CHINESE,
            Locale.JAPANESE,
            Locale.KOREAN,
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.ITALIAN
        };

        for (Locale locale : commonLocales) {
            if (resourceExists(getResourcePath(locale))) {
                supportedLocales.add(locale);
            }
        }

        logger.debug("Scanned supported locales: {}", supportedLocales);
    }

    /**
     * 加载所有消息
     */
    private void loadAllMessages() {
        for (Locale locale : supportedLocales) {
            Properties properties = loadPropertiesForLocale(locale);
            if (properties != null) {
                messageCache.put(locale, properties);
            }
        }
    }

    /**
     * 为指定语言环境加载Properties
     */
    private Properties loadPropertiesForLocale(Locale locale) {
        String resourcePath = getResourcePath(locale);
        
        try (InputStream inputStream = getResourceInputStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }

            Properties properties = new Properties();
            properties.load(new java.io.InputStreamReader(inputStream, encoding));
            
            // 更新最后修改时间
            updateLastModifiedTime(locale, resourcePath);
            
            logger.debug("Loaded {} messages for locale {} from {}", 
                        properties.size(), locale, resourcePath);
            return properties;
        } catch (IOException e) {
            logger.warn("Failed to load properties for locale {} from {}", locale, resourcePath, e);
            return null;
        }
    }

    /**
     * 获取资源路径
     */
    private String getResourcePath(Locale locale) {
        StringBuilder path = new StringBuilder();
        
        if (location.startsWith("classpath:")) {
            path.append(location.substring("classpath:".length()));
        } else if (location.startsWith("file:")) {
            path.append(location.substring("file:".length()));
        } else {
            path.append(location);
        }
        
        if (!path.toString().endsWith("/") && !path.toString().isEmpty()) {
            path.append("/");
        }
        
        path.append(baseName);
        
        if (locale != null && !Locale.ROOT.equals(locale)) {
            path.append("_").append(locale.toString());
        }
        
        path.append(".properties");
        
        return path.toString();
    }

    /**
     * 检查资源是否存在
     */
    private boolean resourceExists(String resourcePath) {
        if (location.startsWith("classpath:")) {
            return getClass().getClassLoader().getResource(resourcePath) != null;
        } else {
            return Files.exists(Paths.get(resourcePath));
        }
    }

    /**
     * 获取资源输入流
     */
    private InputStream getResourceInputStream(String resourcePath) throws IOException {
        if (location.startsWith("classpath:")) {
            return getClass().getClassLoader().getResourceAsStream(resourcePath);
        } else {
            Path path = Paths.get(resourcePath);
            return Files.exists(path) ? Files.newInputStream(path) : null;
        }
    }

    /**
     * 更新最后修改时间
     */
    private void updateLastModifiedTime(Locale locale, String resourcePath) {
        try {
            long lastModified;
            if (location.startsWith("classpath:")) {
                // 对于classpath资源，使用当前时间
                lastModified = System.currentTimeMillis();
            } else {
                Path path = Paths.get(resourcePath);
                lastModified = Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : 0;
            }
            lastModifiedTimes.put(locale, lastModified);
        } catch (IOException e) {
            logger.warn("Failed to get last modified time for {}", resourcePath, e);
        }
    }

    /**
     * 启动热重载
     */
    private void startHotReload() {
        if (location.startsWith("classpath:")) {
            // classpath资源不支持热重载
            logger.warn("Hot reload not supported for classpath resources");
            return;
        }

        hotReloadExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "PropertiesMessageProvider-HotReload");
            thread.setDaemon(true);
            return thread;
        });

        hotReloadExecutor.scheduleWithFixedDelay(this::checkForChanges, 
                                               checkInterval, checkInterval, TimeUnit.MILLISECONDS);

        logger.info("Hot reload started with check interval: {}ms", checkInterval);
    }

    /**
     * 检查文件变更
     */
    private void checkForChanges() {
        for (Locale locale : supportedLocales) {
            String resourcePath = getResourcePath(locale);
            Path path = Paths.get(resourcePath);
            
            if (!Files.exists(path)) {
                continue;
            }

            try {
                long currentModified = Files.getLastModifiedTime(path).toMillis();
                Long lastModified = lastModifiedTimes.get(locale);
                
                if (lastModified == null || currentModified > lastModified) {
                    logger.info("Detected change in {}, reloading...", resourcePath);
                    
                    Properties newProperties = loadPropertiesForLocale(locale);
                    if (newProperties != null) {
                        messageCache.put(locale, newProperties);
                        notifyListeners(locale);
                    }
                }
            } catch (IOException e) {
                logger.warn("Error checking file modification time: {}", resourcePath, e);
            }
        }
    }

    /**
     * 通知监听器
     */
    private void notifyListeners(Locale locale) {
        for (I18nMessageChangeListener listener : listeners) {
            try {
                listener.onMessagesReloaded(locale, this);
            } catch (Exception e) {
                logger.warn("Error notifying listener", e);
            }
        }
    }
}
