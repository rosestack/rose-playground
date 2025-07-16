package io.github.rose.i18n.cache;

import io.github.rose.i18n.AbstractMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cacheable Service Message Source with TTL support
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public abstract class CacheableMessageSource extends AbstractMessageSource {

    protected static final Logger logger = LoggerFactory.getLogger(CacheableMessageSource.class);

    private final ConcurrentMap<String, CachedMessage> messageCache = new ConcurrentHashMap<>();
    private final Duration cacheTtl;
    private final boolean cacheEnabled;

    public CacheableMessageSource(String source) {
        this(source, Duration.ofMinutes(30), true);
    }

    public CacheableMessageSource(String source, Duration cacheTtl, boolean cacheEnabled) {
        super(source);
        this.cacheTtl = cacheTtl;
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    protected final String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args) {
        if (!cacheEnabled) {
            return doGetInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
        }

        String cacheKey = buildCacheKey(resolvedCode, resolvedLocale);
        CachedMessage cachedMessage = messageCache.get(cacheKey);
        
        if (cachedMessage != null && !cachedMessage.isExpired()) {
            logger.trace("Cache hit for key: {}", cacheKey);
            return resolveMessage(cachedMessage.getMessagePattern(), args);
        }

        // Cache miss or expired, load from source
        String messagePattern = doGetMessagePattern(code, resolvedCode, locale, resolvedLocale);
        if (messagePattern != null) {
            messageCache.put(cacheKey, new CachedMessage(messagePattern, LocalDateTime.now().plus(cacheTtl)));
            logger.trace("Cached message for key: {}", cacheKey);
            return resolveMessage(messagePattern, args);
        }

        return null;
    }

    /**
     * Abstract method to get message pattern from the actual source
     */
    protected abstract String doGetMessagePattern(String code, String resolvedCode, Locale locale, Locale resolvedLocale);

    /**
     * Legacy method for compatibility
     */
    protected String doGetInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args) {
        String messagePattern = doGetMessagePattern(code, resolvedCode, locale, resolvedLocale);
        return messagePattern != null ? resolveMessage(messagePattern, args) : null;
    }

    /**
     * Build cache key from code and locale
     */
    private String buildCacheKey(String resolvedCode, Locale resolvedLocale) {
        return resolvedCode + "@" + resolvedLocale.toString();
    }

    /**
     * Clear all cached messages
     */
    public void clearCache() {
        messageCache.clear();
        logger.debug("Source '{}' cache cleared", source);
    }

    /**
     * Clear expired cache entries
     */
    public void clearExpiredCache() {
        LocalDateTime now = LocalDateTime.now();
        messageCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        logger.debug("Source '{}' expired cache entries cleared", source);
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        long totalEntries = messageCache.size();
        long expiredEntries = messageCache.values().stream()
                .mapToLong(msg -> msg.isExpired() ? 1 : 0)
                .sum();
        return new CacheStatistics(totalEntries, expiredEntries, totalEntries - expiredEntries);
    }

    @Override
    public void destroy() {
        clearCache();
        super.destroy();
    }

    /**
     * Cached message with expiration
     */
    private static class CachedMessage {
        private final String messagePattern;
        private final LocalDateTime expiryTime;

        public CachedMessage(String messagePattern, LocalDateTime expiryTime) {
            this.messagePattern = messagePattern;
            this.expiryTime = expiryTime;
        }

        public String getMessagePattern() {
            return messagePattern;
        }

        public boolean isExpired() {
            return isExpired(LocalDateTime.now());
        }

        public boolean isExpired(LocalDateTime now) {
            return now.isAfter(expiryTime);
        }
    }

    /**
     * Cache statistics
     */
    public static class CacheStatistics {
        private final long totalEntries;
        private final long expiredEntries;
        private final long validEntries;

        public CacheStatistics(long totalEntries, long expiredEntries, long validEntries) {
            this.totalEntries = totalEntries;
            this.expiredEntries = expiredEntries;
            this.validEntries = validEntries;
        }

        public long getTotalEntries() { return totalEntries; }
        public long getExpiredEntries() { return expiredEntries; }
        public long getValidEntries() { return validEntries; }

        @Override
        public String toString() {
            return String.format("CacheStatistics{total=%d, expired=%d, valid=%d}", 
                    totalEntries, expiredEntries, validEntries);
        }
    }
}