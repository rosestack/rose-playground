package io.github.rose.i18n.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高性能 MessageFormat 缓存工具类
 * 
 * <p>该类实现了类似 Spring Framework ResourceBundleMessageSource 的优化策略：</p>
 * <ul>
 *   <li>缓存已解析的 MessageFormat 实例</li>
 *   <li>避免无参数消息的 MessageFormat 创建</li>
 *   <li>支持多级缓存（按 Locale 分组）</li>
 *   <li>线程安全</li>
 * </ul>
 * 
 * @author Rose Framework
 * @since 1.0.0
 */
public class MessageFormatCache {
    
    /**
     * 缓存结构: Locale -> (MessagePattern -> MessageFormat)
     */
    private final Map<Locale, Map<String, MessageFormat>> messageFormatCache = new ConcurrentHashMap<>();
    
    /**
     * 缓存大小限制，防止内存泄漏
     */
    private static final int MAX_CACHE_SIZE_PER_LOCALE = 256;
    
    /**
     * 格式化消息
     * 
     * @param message 消息模板
     * @param locale 区域设置
     * @param args 参数
     * @return 格式化后的消息
     */
    public String formatMessage(String message, Locale locale, Object... args) {
        if (message == null) {
            return null;
        }
        
        // 优化1: 如果没有参数，直接返回原消息（Spring 的策略）
        if (args == null || args.length == 0) {
            return message;
        }
        
        // 优化2: 快速检查是否包含占位符
        if (!containsPlaceholders(message)) {
            return message;
        }
        
        // 优化3: 从缓存获取或创建 MessageFormat
        MessageFormat messageFormat = getMessageFormat(message, locale);
        
        try {
            // 优化4: 使用实例方法而不是静态方法
            return messageFormat.format(args);
        } catch (Exception e) {
            // 格式化失败，降级返回原消息
            return message;
        }
    }
    
    /**
     * 快速检查消息是否包含 MessageFormat 占位符
     */
    private boolean containsPlaceholders(String message) {
        // 简单但有效的检查：查找 {数字} 模式
        int length = message.length();
        for (int i = 0; i < length - 2; i++) {
            if (message.charAt(i) == '{') {
                for (int j = i + 1; j < length; j++) {
                    char ch = message.charAt(j);
                    if (ch == '}') {
                        // 找到了 {xxx} 模式
                        return true;
                    } else if (!Character.isDigit(ch) && ch != ',' && ch != ' ') {
                        // 非标准占位符，跳过
                        break;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 获取缓存的 MessageFormat 实例
     */
    private MessageFormat getMessageFormat(String message, Locale locale) {
        Map<String, MessageFormat> localeCache = messageFormatCache.computeIfAbsent(
            locale, k -> new ConcurrentHashMap<>()
        );
        
        return localeCache.computeIfAbsent(message, pattern -> {
            // 检查缓存大小限制
            if (localeCache.size() >= MAX_CACHE_SIZE_PER_LOCALE) {
                // 可以实现 LRU 清理策略，这里简单清空
                localeCache.clear();
            }
            
            try {
                MessageFormat messageFormat = new MessageFormat(pattern, locale);
                return messageFormat;
            } catch (Exception e) {
                // 返回一个简单的格式器作为降级
                return new MessageFormat(pattern, Locale.ROOT);
            }
        });
    }
    
    /**
     * 清理指定 Locale 的缓存
     */
    public void clearCache(Locale locale) {
        Map<String, MessageFormat> removed = messageFormatCache.remove(locale);
        // 清理指定 Locale 的缓存，无日志
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
            if (containsPlaceholders(message)) {
                getMessageFormat(message, locale);
            }
        });
    }
}