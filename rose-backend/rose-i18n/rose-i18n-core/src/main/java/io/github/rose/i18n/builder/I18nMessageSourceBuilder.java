package io.github.rose.i18n.builder;

import io.github.rose.i18n.I18nCompositeMessageSource;
import io.github.rose.i18n.core.CompositeI18nMessageSource;
import io.github.rose.i18n.interpolation.MessageInterpolator;
import io.github.rose.i18n.provider.JsonMessageProvider;
import io.github.rose.i18n.provider.PropertiesMessageProvider;
import io.github.rose.i18n.provider.YamlMessageProvider;
import io.github.rose.i18n.spi.I18nMessageProvider;
import io.github.rose.i18n.spi.I18nProviderConfig;

import java.util.*;

/**
 * 国际化消息源构建器
 * 
 * <p>提供流式API来构建和配置I18nMessageSource实例。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * I18nCompositeMessageSource messageSource = I18nMessageSourceBuilder.create()
 *     .addPropertiesProvider("classpath:i18n/messages")
 *     .addJsonProvider("classpath:i18n/messages.json")
 *     .addYamlProvider("classpath:i18n/messages.yml")
 *     .setDefaultLocale(Locale.ENGLISH)
 *     .setSupportedLocales(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)
 *     .enableCache(true)
 *     .build();
 * </pre>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class I18nMessageSourceBuilder {

    private final CompositeI18nMessageSource messageSource = new CompositeI18nMessageSource();
    private final List<ProviderConfig> providerConfigs = new ArrayList<>();
    
    private Locale defaultLocale = Locale.getDefault();
    private Set<Locale> supportedLocales = new HashSet<>();
    private MessageInterpolator messageInterpolator;
    private boolean cacheEnabled = true;

    /**
     * 创建构建器实例
     */
    public static I18nMessageSourceBuilder create() {
        return new I18nMessageSourceBuilder();
    }

    /**
     * 添加Properties提供者
     * 
     * @param baseName 基础文件名
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addPropertiesProvider(String baseName) {
        return addPropertiesProvider(baseName, "classpath:");
    }

    /**
     * 添加Properties提供者
     * 
     * @param baseName 基础文件名
     * @param location 文件位置
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addPropertiesProvider(String baseName, String location) {
        I18nProviderConfig config = I18nProviderConfig.builder()
                .type("properties")
                .property("baseName", baseName)
                .property("location", location)
                .build();
        
        providerConfigs.add(new ProviderConfig(new PropertiesMessageProvider(), config));
        return this;
    }

    /**
     * 添加Properties提供者（带热重载）
     * 
     * @param baseName 基础文件名
     * @param location 文件位置
     * @param hotReload 是否启用热重载
     * @param checkInterval 检查间隔（毫秒）
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addPropertiesProvider(String baseName, String location, 
                                                         boolean hotReload, long checkInterval) {
        I18nProviderConfig config = I18nProviderConfig.builder()
                .type("properties")
                .property("baseName", baseName)
                .property("location", location)
                .property("hotReload", hotReload)
                .property("checkInterval", checkInterval)
                .build();
        
        providerConfigs.add(new ProviderConfig(new PropertiesMessageProvider(), config));
        return this;
    }

    /**
     * 添加JSON提供者
     * 
     * @param baseName 基础文件名
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addJsonProvider(String baseName) {
        return addJsonProvider(baseName, "classpath:");
    }

    /**
     * 添加JSON提供者
     * 
     * @param baseName 基础文件名
     * @param location 文件位置
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addJsonProvider(String baseName, String location) {
        I18nProviderConfig config = I18nProviderConfig.builder()
                .type("json")
                .property("baseName", baseName)
                .property("location", location)
                .build();
        
        providerConfigs.add(new ProviderConfig(new JsonMessageProvider(), config));
        return this;
    }

    /**
     * 添加JSON提供者（带扁平化配置）
     * 
     * @param baseName 基础文件名
     * @param location 文件位置
     * @param flattenKeys 是否扁平化键名
     * @param keySeparator 键分隔符
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addJsonProvider(String baseName, String location, 
                                                   boolean flattenKeys, String keySeparator) {
        I18nProviderConfig config = I18nProviderConfig.builder()
                .type("json")
                .property("baseName", baseName)
                .property("location", location)
                .property("flattenKeys", flattenKeys)
                .property("keySeparator", keySeparator)
                .build();
        
        providerConfigs.add(new ProviderConfig(new JsonMessageProvider(), config));
        return this;
    }

    /**
     * 添加YAML提供者
     * 
     * @param baseName 基础文件名
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addYamlProvider(String baseName) {
        return addYamlProvider(baseName, "classpath:");
    }

    /**
     * 添加YAML提供者
     * 
     * @param baseName 基础文件名
     * @param location 文件位置
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addYamlProvider(String baseName, String location) {
        I18nProviderConfig config = I18nProviderConfig.builder()
                .type("yaml")
                .property("baseName", baseName)
                .property("location", location)
                .build();
        
        providerConfigs.add(new ProviderConfig(new YamlMessageProvider(), config));
        return this;
    }

    /**
     * 添加YAML提供者（带扁平化配置）
     * 
     * @param baseName 基础文件名
     * @param location 文件位置
     * @param flattenKeys 是否扁平化键名
     * @param keySeparator 键分隔符
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addYamlProvider(String baseName, String location, 
                                                   boolean flattenKeys, String keySeparator) {
        I18nProviderConfig config = I18nProviderConfig.builder()
                .type("yaml")
                .property("baseName", baseName)
                .property("location", location)
                .property("flattenKeys", flattenKeys)
                .property("keySeparator", keySeparator)
                .build();
        
        providerConfigs.add(new ProviderConfig(new YamlMessageProvider(), config));
        return this;
    }

    /**
     * 添加自定义提供者
     * 
     * @param provider 消息提供者
     * @param config 配置信息
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder addProvider(I18nMessageProvider provider, I18nProviderConfig config) {
        providerConfigs.add(new ProviderConfig(provider, config));
        return this;
    }

    /**
     * 设置默认语言环境
     * 
     * @param defaultLocale 默认语言环境
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        return this;
    }

    /**
     * 设置支持的语言环境
     * 
     * @param locales 支持的语言环境
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder setSupportedLocales(Locale... locales) {
        this.supportedLocales = new HashSet<>(Arrays.asList(locales));
        return this;
    }

    /**
     * 设置支持的语言环境
     * 
     * @param locales 支持的语言环境集合
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder setSupportedLocales(Set<Locale> locales) {
        this.supportedLocales = new HashSet<>(locales);
        return this;
    }

    /**
     * 设置消息插值器
     * 
     * @param messageInterpolator 消息插值器
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder setMessageInterpolator(MessageInterpolator messageInterpolator) {
        this.messageInterpolator = messageInterpolator;
        return this;
    }

    /**
     * 设置是否启用缓存
     * 
     * @param cacheEnabled 是否启用缓存
     * @return 构建器实例
     */
    public I18nMessageSourceBuilder enableCache(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }

    /**
     * 构建I18nCompositeMessageSource实例
     *
     * @return I18nCompositeMessageSource实例
     */
    public I18nCompositeMessageSource build() {
        // 设置基本配置
        messageSource.setDefaultLocale(defaultLocale);
        messageSource.setCacheEnabled(cacheEnabled);
        
        if (messageInterpolator != null) {
            messageSource.setMessageInterpolator(messageInterpolator);
        }

        // 添加所有提供者
        for (ProviderConfig providerConfig : providerConfigs) {
            // 如果配置了支持的语言环境，则设置到提供者配置中
            if (!supportedLocales.isEmpty()) {
                providerConfig.config.setSupportedLocales(supportedLocales);
            }
            
            messageSource.addProvider(providerConfig.provider, providerConfig.config);
        }

        // 初始化消息源
        messageSource.init();

        return messageSource;
    }

    /**
     * 提供者配置内部类
     */
    private static class ProviderConfig {
        final I18nMessageProvider provider;
        final I18nProviderConfig config;

        ProviderConfig(I18nMessageProvider provider, I18nProviderConfig config) {
            this.provider = provider;
            this.config = config;
        }
    }
}
