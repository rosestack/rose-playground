package io.github.rose.i18n.impl;

import io.github.rose.i18n.cache.CacheableMessageSource;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Cached ClassPath Properties Service Message Source
 *
 * <p>This implementation provides TTL-based caching for Properties-based messages.
 * It delegates to ClassPathPropertiesMessageSource for the actual Properties loading
 * and adds a caching layer on top for better performance.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Message-level caching with TTL support</li>
 *   <li>High-performance concurrent access</li>
 *   <li>Automatic cache expiration and cleanup</li>
 *   <li>Cache statistics monitoring</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create with default settings (30 minutes TTL)
 * CachedClassPathPropertiesMessageSource source =
 *     new CachedClassPathPropertiesMessageSource("app");
 * source.init();
 *
 * // Create with custom cache settings
 * CachedClassPathPropertiesMessageSource source =
 *     new CachedClassPathPropertiesMessageSource("app", Duration.ofHours(1), true);
 *
 * // Get message (automatically cached)
 * String message = source.getMessage("welcome.message", Locale.ENGLISH, "John");
 * }</pre>
 *
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class CachedClassPathPropertiesMessageSource extends CacheableMessageSource {

    private final ClassPathPropertiesMessageSource delegate;

    /**
     * Constructor with default cache settings (30 minutes TTL, caching enabled)
     *
     * @param source the source name
     */
    public CachedClassPathPropertiesMessageSource(String source) {
        this(source, Duration.ofMinutes(30), true);
    }

    /**
     * Constructor with custom cache settings
     *
     * @param source       the source name
     * @param cacheTtl     cache time-to-live duration
     * @param cacheEnabled whether caching is enabled
     */
    public CachedClassPathPropertiesMessageSource(String source, Duration cacheTtl, boolean cacheEnabled) {
        super(source, cacheTtl, cacheEnabled);
        this.delegate = new ClassPathPropertiesMessageSource(source);
    }

    @Override
    public void init() {
        delegate.init();
        super.init();
    }

    @Override
    protected String doGetMessagePattern(String code, String resolvedCode, Locale locale, Locale resolvedLocale) {
        // Get the messages map from delegate
        Map<String, String> messages = delegate.getMessages(resolvedLocale);
        if (messages != null) {
            return messages.get(resolvedCode);
        }
        return null;
    }

    /**
     * Get the delegate Properties message source
     *
     * @return the underlying ClassPathPropertiesMessageSource
     */
    public ClassPathPropertiesMessageSource getDelegate() {
        return delegate;
    }

    @Override
    public void destroy() {
        delegate.destroy();
        super.destroy();
    }

    @Override
    public String toString() {
        return String.format("CachedClassPathPropertiesMessageSource{source='%s', delegate=%s, cacheStats=%s}",
                getSource(), delegate, getCacheStatistics());
    }
}