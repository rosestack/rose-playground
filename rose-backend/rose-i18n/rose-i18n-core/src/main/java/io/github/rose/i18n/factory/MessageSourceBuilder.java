package io.github.rose.i18n.factory;


import io.github.rose.i18n.AbstractMessageSource;
import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.config.MessageSourceConfiguration;
import io.github.rose.i18n.impl.ClassPathPropertiesMessageSource;
import io.github.rose.i18n.impl.CachedClassPathPropertiesMessageSource;
import io.github.rose.i18n.impl.CachedClassPathYamlResourceMessageSource;
import io.github.rose.i18n.impl.ClassPathYamlResourceMessageSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Fluent builder for creating MessageSource instances
 *
 * <p>This builder can create both {@link MessageSource} and {@link MessageSource}
 * instances (they are the same - MessageSource is just an alias). The builder provides
 * a fluent API for configuring various aspects of the message source including type,
 * caching, locales, and resource paths.</p>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Create a simple YAML message source
 * MessageSource source = MessageSourceBuilder.create("app").yaml().build();
 *
 * // Create with custom configuration
 * MessageSource source = MessageSourceBuilder.create("app")
 *     .yaml()
 *     .cache(Duration.ofHours(1))
 *     .supportedLocales(Locale.ENGLISH, Locale.CHINESE)
 *     .defaultLocale(Locale.ENGLISH)
 *     .build();
 *
 * // Auto-detect format
 * MessageSource source = MessageSourceBuilder.create("app").auto().build();
 * }</pre>
 *
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class MessageSourceBuilder {

    private final String source;
    private MessageSourceFactory.MessageSourceType type = MessageSourceFactory.MessageSourceType.AUTO;
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private List<String> basePaths = new ArrayList<>();
    private Charset encoding = StandardCharsets.UTF_8;
    private List<Locale> supportedLocales = new ArrayList<>();
    private Locale defaultLocale = Locale.getDefault();
    private boolean cacheEnabled = true;
    private Duration cacheTtl = Duration.ofMinutes(30);
    private boolean failFast = false;
    private boolean enableReloading = false;

    public MessageSourceBuilder(String source) {
        this.source = source;
        // Set default base paths
        this.basePaths.add("META-INF/i18n/");
        this.basePaths.add("META-INF/");
        this.basePaths.add("");
    }

    /**
     * Create a new MessageSourceBuilder
     *
     * @param source the source name
     * @return MessageSourceBuilder instance
     */
    public static MessageSourceBuilder create(String source) {
        return new MessageSourceBuilder(source);
    }

    /**
     * Set the message source type
     */
    public MessageSourceBuilder type(MessageSourceFactory.MessageSourceType type) {
        this.type = type;
        return this;
    }

    /**
     * Set the message source type by string
     */
    public MessageSourceBuilder type(String type) {
        this.type = MessageSourceFactory.MessageSourceType.fromString(type);
        return this;
    }

    /**
     * Use Properties format
     */
    public MessageSourceBuilder properties() {
        this.type = MessageSourceFactory.MessageSourceType.PROPERTIES;
        return this;
    }

    /**
     * Use YAML format
     */
    public MessageSourceBuilder yaml() {
        this.type = MessageSourceFactory.MessageSourceType.YAML;
        return this;
    }

    /**
     * Auto-detect format
     */
    public MessageSourceBuilder auto() {
        this.type = MessageSourceFactory.MessageSourceType.AUTO;
        return this;
    }

    /**
     * Set the ClassLoader
     */
    public MessageSourceBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Set base paths for resource resolution
     */
    public MessageSourceBuilder basePaths(String... basePaths) {
        this.basePaths = Arrays.asList(basePaths);
        return this;
    }

    /**
     * Add a base path
     */
    public MessageSourceBuilder addBasePath(String basePath) {
        this.basePaths.add(basePath);
        return this;
    }

    /**
     * Set encoding
     */
    public MessageSourceBuilder encoding(Charset encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Set encoding by name
     */
    public MessageSourceBuilder encoding(String encoding) {
        this.encoding = Charset.forName(encoding);
        return this;
    }

    /**
     * Use UTF-8 encoding
     */
    public MessageSourceBuilder utf8() {
        this.encoding = StandardCharsets.UTF_8;
        return this;
    }

    /**
     * Set supported locales
     */
    public MessageSourceBuilder supportedLocales(Locale... locales) {
        this.supportedLocales = Arrays.asList(locales);
        return this;
    }

    /**
     * Add a supported locale
     */
    public MessageSourceBuilder addSupportedLocale(Locale locale) {
        this.supportedLocales.add(locale);
        return this;
    }

    /**
     * Add common locales (en, zh_CN, zh_TW, ja, ko, fr, de, es, it)
     */
    public MessageSourceBuilder withCommonLocales() {
        this.supportedLocales.addAll(Arrays.asList(
                Locale.ENGLISH,
                Locale.SIMPLIFIED_CHINESE,
                Locale.TRADITIONAL_CHINESE,
                Locale.JAPANESE,
                Locale.KOREAN,
                Locale.FRENCH,
                Locale.GERMAN,
                new Locale("es"), // Spanish
                Locale.ITALIAN
        ));
        return this;
    }

    /**
     * Set default locale
     */
    public MessageSourceBuilder defaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        return this;
    }

    /**
     * Enable or disable caching
     */
    public MessageSourceBuilder cache(boolean enabled) {
        this.cacheEnabled = enabled;
        return this;
    }

    /**
     * Enable caching
     */
    public MessageSourceBuilder enableCache() {
        this.cacheEnabled = true;
        return this;
    }

    /**
     * Disable caching
     */
    public MessageSourceBuilder disableCache() {
        this.cacheEnabled = false;
        return this;
    }

    /**
     * Set cache TTL
     */
    public MessageSourceBuilder cacheTtl(Duration ttl) {
        this.cacheTtl = ttl;
        return this;
    }

    /**
     * Set cache TTL in minutes
     */
    public MessageSourceBuilder cacheTtlMinutes(long minutes) {
        this.cacheTtl = Duration.ofMinutes(minutes);
        return this;
    }

    /**
     * Enable or disable fail fast
     */
    public MessageSourceBuilder failFast(boolean enabled) {
        this.failFast = enabled;
        return this;
    }

    /**
     * Enable fail fast
     */
    public MessageSourceBuilder enableFailFast() {
        this.failFast = true;
        return this;
    }

    /**
     * Enable or disable reloading
     */
    public MessageSourceBuilder reloading(boolean enabled) {
        this.enableReloading = enabled;
        return this;
    }

    /**
     * Enable reloading
     */
    public MessageSourceBuilder enableReloading() {
        this.enableReloading = true;
        return this;
    }

    /**
     * Build the MessageSource
     *
     * @return MessageSource instance (alias for MessageSource)
     */
    public MessageSource build() {
        MessageSource messageSource = createMessageSource();
        configureMessageSource(messageSource);
        return (MessageSource) messageSource;
    }

    /**
     * Build and initialize the MessageSource
     *
     * @return initialized MessageSource instance
     */
    public MessageSource buildAndInit() {
        MessageSource messageSource = build();
        messageSource.init();
        return messageSource;
    }

    /**
     * Create the actual message source instance
     */
    private MessageSource createMessageSource() {
        switch (type) {
            case PROPERTIES:
                return createPropertiesMessageSource();

            case YAML:
                return createYamlMessageSource();

            case AUTO:
                return createAutoDetectMessageSource();

            default:
                throw new IllegalArgumentException("Unsupported message source type: " + type);
        }
    }

    /**
     * Create Properties message source
     */
    private MessageSource createPropertiesMessageSource() {
        if (cacheEnabled && cacheTtl != null) {
            // Use the cached version if caching is enabled with TTL
            return new CachedClassPathPropertiesMessageSource(source, cacheTtl, true);
        } else {
            // Use the standard version
            return new ClassPathPropertiesMessageSource(source);
        }
    }

    /**
     * Create YAML message source
     */
    private MessageSource createYamlMessageSource() {
        String[] basePathArray = basePaths.toArray(new String[0]);

        if (cacheEnabled && cacheTtl != null) {
            // Use the cached version if caching is enabled with TTL
            return new CachedClassPathYamlResourceMessageSource(source, classLoader, basePathArray, cacheTtl, true);
        } else {
            // Use the standard version
            return new ClassPathYamlResourceMessageSource(source, classLoader, basePathArray, cacheEnabled, failFast);
        }
    }

    /**
     * Create auto-detect message source
     */
    private MessageSource createAutoDetectMessageSource() {
        // Try YAML first, then fallback to Properties
        try {
            MessageSource yamlSource = createYamlMessageSource();
            yamlSource.init();
            return yamlSource;
        } catch (Exception e) {
            // Fallback to Properties
            return createPropertiesMessageSource();
        }
    }

    /**
     * Apply configuration to the message source
     */
    private void configureMessageSource(MessageSource messageSource) {
        // Apply configuration if the message source supports it
        if (messageSource instanceof io.github.rose.i18n.AbstractMessageSource) {
            AbstractMessageSource abstractSource =
                    (AbstractMessageSource) messageSource;

            if (!supportedLocales.isEmpty()) {
                abstractSource.setSupportedLocales(supportedLocales);
            }

            if (defaultLocale != null) {
                abstractSource.setDefaultLocale(defaultLocale);
            }
        }
    }

    /**
     * Create a MessageSourceConfiguration object
     */
    public MessageSourceConfiguration toConfiguration() {
        MessageSourceConfiguration config = new MessageSourceConfiguration();
        config.setSource(source);
        config.setEncoding(encoding);
        config.setBasePaths(basePaths);
        config.setSupportedLocales(supportedLocales);
        config.setDefaultLocale(defaultLocale);
        config.setCacheTtl(cacheTtl);
        config.setEnableFallback(true);
        config.setEnableReloading(enableReloading);
        config.setFailFast(failFast);
        return config;
    }

    @Override
    public String toString() {
        return String.format("MessageSourceBuilder{source='%s', type=%s, basePaths=%s, cacheEnabled=%s}",
                source, type, basePaths, cacheEnabled);
    }
}