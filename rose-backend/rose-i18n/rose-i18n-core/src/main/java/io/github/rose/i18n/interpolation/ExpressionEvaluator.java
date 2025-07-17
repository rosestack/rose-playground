package io.github.rose.i18n.interpolation;

import java.util.Locale;
import java.util.Map;

/**
 * 表达式评估器接口
 * 
 * <p>用于评估表达式语法中的复杂表达式，如EL表达式。
 * 这是一个可插拔的接口，支持不同的表达式引擎实现。</p>
 * 
 * <p>支持的实现：</p>
 * <ul>
 *   <li>Jakarta EL (Glassfish实现)</li>
 *   <li>Tomcat EL实现</li>
 *   <li>Spring EL实现</li>
 *   <li>自定义简单表达式实现</li>
 * </ul>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface ExpressionEvaluator {

    /**
     * 评估表达式
     * 
     * @param expression 表达式内容（不包含${}包装）
     * @param variables 变量映射
     * @param locale 语言环境
     * @return 评估结果，如果评估失败返回null
     */
    Object evaluate(String expression, Map<String, Object> variables, Locale locale);

    /**
     * 检查是否支持指定的表达式
     * 
     * @param expression 表达式内容
     * @return 如果支持返回true，否则返回false
     */
    boolean supports(String expression);

    /**
     * 获取评估器名称
     * 
     * @return 评估器名称
     */
    String getName();

    /**
     * 获取评估器优先级
     * 
     * <p>数值越小优先级越高，用于多个评估器的排序</p>
     * 
     * @return 优先级数值
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 检查评估器是否可用
     * 
     * <p>用于检查依赖的库是否存在</p>
     * 
     * @return 如果可用返回true，否则返回false
     */
    boolean isAvailable();

    /**
     * 清除缓存（如果有的话）
     */
    default void clearCache() {
        // 默认实现为空
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    default CacheStatistics getCacheStatistics() {
        return new CacheStatistics(0, 0, 0);
    }

    /**
     * 缓存统计信息
     */
    class CacheStatistics {
        private final int size;
        private final long hitCount;
        private final long missCount;

        public CacheStatistics(int size, long hitCount, long missCount) {
            this.size = size;
            this.hitCount = hitCount;
            this.missCount = missCount;
        }

        public int getSize() {
            return size;
        }

        public long getHitCount() {
            return hitCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public double getHitRate() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }

        @Override
        public String toString() {
            return String.format("CacheStatistics{size=%d, hitCount=%d, missCount=%d, hitRate=%.2f%%}", 
                               size, hitCount, missCount, getHitRate() * 100);
        }
    }
}
