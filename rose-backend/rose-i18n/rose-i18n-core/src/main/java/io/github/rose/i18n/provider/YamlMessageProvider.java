package io.github.rose.i18n.provider;

import io.github.rose.i18n.spi.I18nMessageChangeListener;
import io.github.rose.i18n.spi.I18nMessageProvider;
import io.github.rose.i18n.spi.I18nProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * YAML文件消息提供者
 * 
 * <p>从YAML文件加载国际化消息，支持嵌套结构。</p>
 * 
 * <p>YAML文件格式示例：</p>
 * <pre>
 * common:
 *   save: Save
 *   cancel: Cancel
 * user:
 *   name: Name
 *   email: Email
 * </pre>
 * 
 * <p>配置参数：</p>
 * <ul>
 *   <li>baseName: 基础文件名，如"messages"</li>
 *   <li>location: 文件位置，支持classpath:和file:前缀</li>
 *   <li>flattenKeys: 是否扁平化键名，默认true</li>
 *   <li>keySeparator: 键分隔符，默认"."</li>
 * </ul>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class YamlMessageProvider implements I18nMessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(YamlMessageProvider.class);

    private String baseName;
    private String location;
    private boolean flattenKeys = true;
    private String keySeparator = ".";
    private int priority = 0;

    private final Yaml yaml = new Yaml();
    private final Map<Locale, Map<String, String>> messageCache = new ConcurrentHashMap<>();
    private final List<I18nMessageChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final Set<Locale> supportedLocales = ConcurrentHashMap.newKeySet();

    private boolean initialized = false;

    @Override
    public String getName() {
        return "YamlMessageProvider";
    }

    @Override
    public boolean supports(I18nProviderConfig config) {
        return "yaml".equals(config.getType()) || "yml".equals(config.getType()) ||
               (config.getStringProperty("baseName") != null && 
                ("yaml".equals(config.getStringProperty("format", "")) || 
                 "yml".equals(config.getStringProperty("format", ""))));
    }

    @Override
    public void initialize(I18nProviderConfig config) {
        this.baseName = config.getStringProperty("baseName");
        this.location = config.getStringProperty("location", "classpath:");
        this.flattenKeys = config.getBooleanProperty("flattenKeys", true);
        this.keySeparator = config.getStringProperty("keySeparator", ".");
        this.priority = config.getIntProperty("priority", 0);

        if (baseName == null) {
            throw new IllegalArgumentException("baseName is required for YamlMessageProvider");
        }

        // 扫描并加载所有支持的语言环境
        scanSupportedLocales();
        
        // 预加载消息
        loadAllMessages();

        initialized = true;
        logger.info("YamlMessageProvider initialized: baseName={}, location={}, supportedLocales={}", 
                   baseName, location, supportedLocales);
    }

    @Override
    public Map<String, String> loadMessages(Locale locale) {
        if (!initialized) {
            throw new IllegalStateException("Provider not initialized");
        }

        Map<String, String> messages = messageCache.get(locale);
        if (messages == null) {
            messages = loadMessagesForLocale(locale);
            if (messages != null) {
                messageCache.put(locale, messages);
            }
        }

        return messages != null ? new HashMap<>(messages) : Collections.emptyMap();
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return new HashSet<>(supportedLocales);
    }

    @Override
    public boolean supportsHotReload() {
        return false; // YAML文件暂不支持热重载
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
        logger.info("Refreshing YamlMessageProvider");
        messageCache.clear();
        loadAllMessages();
        
        // 通知监听器
        for (Locale locale : supportedLocales) {
            notifyListeners(locale);
        }
    }

    @Override
    public void destroy() {
        messageCache.clear();
        listeners.clear();
        supportedLocales.clear();
        initialized = false;
        logger.info("YamlMessageProvider destroyed");
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 扫描支持的语言环境
     */
    private void scanSupportedLocales() {
        // 检查.yaml和.yml两种扩展名
        String[] extensions = {".yaml", ".yml"};
        
        for (String extension : extensions) {
            // 默认语言环境（无后缀）
            if (resourceExists(getResourcePath(null, extension))) {
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
                if (resourceExists(getResourcePath(locale, extension))) {
                    supportedLocales.add(locale);
                }
            }
        }

        logger.debug("Scanned supported locales: {}", supportedLocales);
    }

    /**
     * 加载所有消息
     */
    private void loadAllMessages() {
        for (Locale locale : supportedLocales) {
            Map<String, String> messages = loadMessagesForLocale(locale);
            if (messages != null) {
                messageCache.put(locale, messages);
            }
        }
    }

    /**
     * 为指定语言环境加载消息
     */
    private Map<String, String> loadMessagesForLocale(Locale locale) {
        // 尝试.yaml和.yml两种扩展名
        String[] extensions = {".yaml", ".yml"};
        
        for (String extension : extensions) {
            String resourcePath = getResourcePath(locale, extension);
            
            try (InputStream inputStream = getResourceInputStream(resourcePath)) {
                if (inputStream == null) {
                    continue;
                }

                Object yamlData = yaml.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                Map<String, String> messages = new HashMap<>();
                
                if (yamlData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> yamlMap = (Map<String, Object>) yamlData;
                    
                    if (flattenKeys) {
                        flattenYamlMap("", yamlMap, messages);
                    } else {
                        extractSimpleMessages(yamlMap, messages);
                    }
                }
                
                logger.debug("Loaded {} messages for locale {} from {}", 
                            messages.size(), locale, resourcePath);
                return messages;
            } catch (IOException e) {
                logger.warn("Failed to load YAML messages for locale {} from {}", locale, resourcePath, e);
            }
        }
        
        return null;
    }

    /**
     * 扁平化YAML映射
     */
    @SuppressWarnings("unchecked")
    private void flattenYamlMap(String prefix, Map<String, Object> map, Map<String, String> messages) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + keySeparator + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                flattenYamlMap(key, (Map<String, Object>) value, messages);
            } else if (value instanceof List) {
                List<?> list = (List<?>) value;
                for (int i = 0; i < list.size(); i++) {
                    String arrayKey = key + "[" + i + "]";
                    Object arrayValue = list.get(i);
                    if (arrayValue instanceof Map) {
                        flattenYamlMap(arrayKey, (Map<String, Object>) arrayValue, messages);
                    } else {
                        messages.put(arrayKey, String.valueOf(arrayValue));
                    }
                }
            } else {
                messages.put(key, String.valueOf(value));
            }
        }
    }

    /**
     * 提取简单消息（不扁平化）
     */
    private void extractSimpleMessages(Map<String, Object> map, Map<String, String> messages) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Map) && !(value instanceof List)) {
                messages.put(entry.getKey(), String.valueOf(value));
            }
        }
    }

    /**
     * 获取资源路径
     */
    private String getResourcePath(Locale locale, String extension) {
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
        
        path.append(extension);
        
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
