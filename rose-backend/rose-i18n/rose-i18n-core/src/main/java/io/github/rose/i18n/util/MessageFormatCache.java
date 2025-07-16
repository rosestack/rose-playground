package io.github.rose.i18n.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高性能 MessageFormat 缓存工具类
 * <p>缓存已解析的 MessageFormat 实例，避免重复解析，提升性能。</p>
 */
public final class MessageFormatCache {
    /** 缓存结构: Locale -> (MessagePattern -> MessageFormat) */
    private final Map<Locale, Map<String, MessageFormat>> messageFormatCache = new ConcurrentHashMap<>();
    /** 缓存大小限制，防止内存泄漏 */
    private static final int MAX_CACHE_SIZE_PER_LOCALE = 256;

    /**
     * 格式化消息
     * @param message 消息模板
     * @param locale 区域设置
     * @param args 参数
     * @return 格式化后的消息
     */
    public String formatMessage(String message, Locale locale, Object... args) {
        if (message == null) return null;
        if (args == null || args.length == 0) return message;
        if (!hasPlaceholders(message)) return message;
        MessageFormat format = getMessageFormat(message, locale);
        try {
            return format.format(args);
        } catch (Exception e) {
            return message;
        }
    }

    /**
     * 判断消息是否包含 MessageFormat 占位符（如 {0}）
     */
    private boolean hasPlaceholders(String message) {
        return message.contains("{") && message.contains("}");
    }

    /**
     * 获取缓存的 MessageFormat 实例
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
     * 清理指定 Locale 的缓存
     */
    public void clearCache(Locale locale) {
        messageFormatCache.remove(locale);
    }

    /**
     * 清理所有缓存
     */
    public void clearAllCache() {
        messageFormatCache.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public Map<Locale, Integer> getCacheStats() {
        Map<Locale, Integer> stats = new HashMap<>();
        messageFormatCache.forEach((locale, cache) -> stats.put(locale, cache.size()));
        return stats;
    }

    /**
     * 预热缓存（可选）
     */
    public void warmupCache(Map<String, String> messages, Locale locale) {
        messages.forEach((code, message) -> {
            if (hasPlaceholders(message)) {
                getMessageFormat(message, locale);
            }
        });
    }
}