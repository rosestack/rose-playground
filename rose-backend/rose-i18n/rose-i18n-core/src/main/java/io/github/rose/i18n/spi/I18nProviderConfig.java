package io.github.rose.i18n.spi;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 国际化提供者配置
 * 
 * <p>用于配置I18nMessageProvider的各种参数和选项。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class I18nProviderConfig {

    private String name;
    private String type;
    private Map<String, Object> properties = new HashMap<>();
    private Set<Locale> supportedLocales;
    private Locale defaultLocale;
    private boolean hotReloadEnabled = false;
    private long hotReloadInterval = 60000; // 1分钟
    private boolean cacheEnabled = true;
    private long cacheExpireTime = 3600000; // 1小时
    private int priority = 0;

    /**
     * 获取提供者名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置提供者名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取提供者类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置提供者类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取配置属性
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * 设置配置属性
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * 获取属性值
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * 获取字符串属性值
     */
    public String getStringProperty(String key) {
        Object value = properties.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取字符串属性值（带默认值）
     */
    public String getStringProperty(String key, String defaultValue) {
        String value = getStringProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔属性值
     */
    public Boolean getBooleanProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    /**
     * 获取布尔属性值（带默认值）
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        Boolean value = getBooleanProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数属性值
     */
    public Integer getIntProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取整数属性值（带默认值）
     */
    public int getIntProperty(String key, int defaultValue) {
        Integer value = getIntProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取长整数属性值
     */
    public Long getLongProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取长整数属性值（带默认值）
     */
    public long getLongProperty(String key, long defaultValue) {
        Long value = getLongProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 设置属性值
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * 获取支持的语言环境
     */
    public Set<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    /**
     * 设置支持的语言环境
     */
    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    /**
     * 获取默认语言环境
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * 设置默认语言环境
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * 是否启用热重载
     */
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }

    /**
     * 设置是否启用热重载
     */
    public void setHotReloadEnabled(boolean hotReloadEnabled) {
        this.hotReloadEnabled = hotReloadEnabled;
    }

    /**
     * 获取热重载检查间隔（毫秒）
     */
    public long getHotReloadInterval() {
        return hotReloadInterval;
    }

    /**
     * 设置热重载检查间隔（毫秒）
     */
    public void setHotReloadInterval(long hotReloadInterval) {
        this.hotReloadInterval = hotReloadInterval;
    }

    /**
     * 是否启用缓存
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * 设置是否启用缓存
     */
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    /**
     * 获取缓存过期时间（毫秒）
     */
    public long getCacheExpireTime() {
        return cacheExpireTime;
    }

    /**
     * 设置缓存过期时间（毫秒）
     */
    public void setCacheExpireTime(long cacheExpireTime) {
        this.cacheExpireTime = cacheExpireTime;
    }

    /**
     * 获取优先级
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final I18nProviderConfig config = new I18nProviderConfig();

        public Builder name(String name) {
            config.setName(name);
            return this;
        }

        public Builder type(String type) {
            config.setType(type);
            return this;
        }

        public Builder property(String key, Object value) {
            config.setProperty(key, value);
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            config.setProperties(new HashMap<>(properties));
            return this;
        }

        public Builder supportedLocales(Set<Locale> locales) {
            config.setSupportedLocales(locales);
            return this;
        }

        public Builder defaultLocale(Locale locale) {
            config.setDefaultLocale(locale);
            return this;
        }

        public Builder hotReload(boolean enabled, long interval) {
            config.setHotReloadEnabled(enabled);
            config.setHotReloadInterval(interval);
            return this;
        }

        public Builder cache(boolean enabled, long expireTime) {
            config.setCacheEnabled(enabled);
            config.setCacheExpireTime(expireTime);
            return this;
        }

        public Builder priority(int priority) {
            config.setPriority(priority);
            return this;
        }

        public I18nProviderConfig build() {
            return config;
        }
    }
}
