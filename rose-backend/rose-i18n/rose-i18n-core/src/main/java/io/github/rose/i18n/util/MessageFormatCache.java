package io.github.rose.i18n.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance MessageFormat cache utility.
 * <p>Caches parsed MessageFormat instances to avoid repeated parsing and improve performance.</p>
 */
public final class MessageFormatCache {
    /**
     * Cache structure: Locale -> (MessagePattern -> MessageFormat)
     */
    private final Map<Locale, Map<String, MessageFormat>> messageFormatCache = new ConcurrentHashMap<>();
    /**
     * Cache size limit per locale to prevent memory leaks
     */
    private static final int MAX_CACHE_SIZE_PER_LOCALE = 256;

    /**
     * Format a message.
     *
     * @param message message template
     * @param locale  locale
     * @param args    arguments
     * @return formatted message
     */
    public String formatMessage(String message, Locale locale, Object... args) {
        if (message == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return message;
        }
        if (!hasPlaceholders(message)) {
            return message;
        }
        MessageFormat format = getMessageFormat(message, locale);
        try {
            return format.format(args);
        } catch (Exception e) {
            return message;
        }
    }

    /**
     * Check if a message contains MessageFormat placeholders (e.g., {0}).
     */
    private boolean hasPlaceholders(String message) {
        return message.contains("{") && message.contains("}");
    }

    /**
     * Get a cached MessageFormat instance.
     */
    private MessageFormat getMessageFormat(String message, Locale locale) {
        Map<String, MessageFormat> localeCache = messageFormatCache.computeIfAbsent(
                locale, k -> new ConcurrentHashMap<>()
        );
        if (localeCache.size() >= MAX_CACHE_SIZE_PER_LOCALE) {
            localeCache.clear();
        }
        return localeCache.computeIfAbsent(message, pattern -> {
            try {
                return new MessageFormat(pattern, locale);
            } catch (Exception e) {
                return new MessageFormat(pattern, Locale.ROOT);
            }
        });
    }

    /**
     * Clear cache for a specific Locale.
     */
    public void clearCache(Locale locale) {
        messageFormatCache.remove(locale);
    }

    /**
     * Clear all caches.
     */
    public void clearAllCache() {
        messageFormatCache.clear();
    }

    /**
     * Get cache statistics.
     */
    public Map<Locale, Integer> getCacheStats() {
        Map<Locale, Integer> stats = new HashMap<>();
        messageFormatCache.forEach((locale, cache) -> stats.put(locale, cache.size()));
        return stats;
    }

    /**
     * Warm up cache (optional).
     */
    public void warmupCache(Map<String, String> messages, Locale locale) {
        messages.forEach((code, message) -> {
            if (hasPlaceholders(message)) {
                getMessageFormat(message, locale);
            }
        });
    }
}