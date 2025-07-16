package io.github.rose.i18n.impl;

import io.github.rose.i18n.cache.CacheableMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Cached ClassPath YAML Resource Service Message Source
 * 
 * <p>This implementation extends ClassPathYamlResourceMessageSource with advanced caching capabilities.
 * It provides TTL-based caching for resolved messages (not just YAML data) and better performance monitoring.</p>
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class CachedClassPathYamlResourceMessageSource extends CacheableMessageSource {

    private static final Logger logger = LoggerFactory.getLogger(CachedClassPathYamlResourceMessageSource.class);
    
    private final ClassPathYamlResourceMessageSource delegate;

    /**
     * Constructor with default cache settings
     */
    public CachedClassPathYamlResourceMessageSource(String source) {
        this(source, Duration.ofMinutes(30), true);
    }

    /**
     * Constructor with custom cache settings
     */
    public CachedClassPathYamlResourceMessageSource(String source, Duration cacheTtl, boolean cacheEnabled) {
        super(source, cacheTtl, cacheEnabled);
        this.delegate = new ClassPathYamlResourceMessageSource(source,
                Thread.currentThread().getContextClassLoader(), null, true, false);
    }

    /**
     * Constructor with full configuration
     */
    public CachedClassPathYamlResourceMessageSource(String source, ClassLoader classLoader,
                                                    String[] basePaths, Duration cacheTtl, boolean cacheEnabled) {
        super(source, cacheTtl, cacheEnabled);
        this.delegate = new ClassPathYamlResourceMessageSource(source, classLoader, basePaths, true, false);
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

    // Note: Cannot override final methods from AbstractMessageSource
    // The parent class CacheableMessageSource already provides these methods

    /**
     * Get the delegate YAML message source
     */
    public ClassPathYamlResourceMessageSource getDelegate() {
        return delegate;
    }

    /**
     * Clear both message cache and YAML cache
     */
    @Override
    public void clearCache() {
        super.clearCache();
        delegate.clearYamlCache();
        logger.debug("Source '{}' all caches cleared", source);
    }

    /**
     * Get combined cache statistics
     */
    public CombinedCacheStatistics getCombinedCacheStatistics() {
        CacheStatistics messageStats = getCacheStatistics();
        int yamlCacheSize = delegate.getYamlCacheSize();
        return new CombinedCacheStatistics(messageStats, yamlCacheSize);
    }

    @Override
    public void destroy() {
        delegate.destroy();
        super.destroy();
    }

    @Override
    public String toString() {
        return String.format("CachedClassPathYamlResourceMessageSource{source='%s', delegate=%s, cacheStats=%s}",
                source, delegate, getCacheStatistics());
    }

    /**
     * Combined cache statistics for both message and YAML caches
     */
    public static class CombinedCacheStatistics {
        private final CacheStatistics messageCache;
        private final int yamlCacheSize;

        public CombinedCacheStatistics(CacheStatistics messageCache, int yamlCacheSize) {
            this.messageCache = messageCache;
            this.yamlCacheSize = yamlCacheSize;
        }

        public CacheStatistics getMessageCache() {
            return messageCache;
        }

        public int getYamlCacheSize() {
            return yamlCacheSize;
        }

        public long getTotalCacheEntries() {
            return messageCache.getTotalEntries() + yamlCacheSize;
        }

        @Override
        public String toString() {
            return String.format("CombinedCacheStatistics{messageCache=%s, yamlCache=%d, total=%d}", 
                    messageCache, yamlCacheSize, getTotalCacheEntries());
        }
    }
}