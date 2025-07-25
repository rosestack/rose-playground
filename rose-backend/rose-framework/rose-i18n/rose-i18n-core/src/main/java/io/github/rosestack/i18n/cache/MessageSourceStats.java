package io.github.rosestack.i18n.cache;

import lombok.Builder;
import lombok.Data;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
public class MessageSourceStats {
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong loadTime = new AtomicLong(0);
    private int messageCount;
    private Set<Locale> supportedLocales;

    // 默认构造函数
    public MessageSourceStats() {
        this.messageCount = 0;
        this.supportedLocales = Set.of();
    }

    // 带参数的构造函数
    public MessageSourceStats(int messageCount, Set<Locale> supportedLocales) {
        this.messageCount = messageCount;
        this.supportedLocales = supportedLocales;
    }

    // 统计方法
    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrementCacheHits() {
        cacheHits.incrementAndGet();
    }

    public void incrementCacheMisses() {
        cacheMisses.incrementAndGet();
    }

    public void addLoadTime(long timeMs) {
        loadTime.addAndGet(timeMs);
    }

    // 获取统计数据的便捷方法
    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getCacheHits() {
        return cacheHits.get();
    }

    public long getCacheMisses() {
        return cacheMisses.get();
    }

    public long getLoadTime() {
        return loadTime.get();
    }

    // 计算缓存命中率
    public double getCacheHitRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) cacheHits.get() / total;
    }

    // 计算平均加载时间
    public double getAverageLoadTime() {
        long misses = cacheMisses.get();
        if (misses == 0) {
            return 0.0;
        }
        return (double) loadTime.get() / misses;
    }

    // 重置统计数据
    public void reset() {
        totalRequests.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        loadTime.set(0);
    }

    // 获取统计摘要
    public String getSummary() {
        return String.format(
                "MessageSource Stats - Total: %d, Hits: %d, Misses: %d, Hit Rate: %.2f%%, Avg Load Time: %.2fms",
                getTotalRequests(),
                getCacheHits(),
                getCacheMisses(),
                getCacheHitRate() * 100,
                getAverageLoadTime()
        );
    }

    // 合并统计数据
    public void merge(MessageSourceStats other) {
        if (other != null) {
            totalRequests.addAndGet(other.getTotalRequests());
            cacheHits.addAndGet(other.getCacheHits());
            cacheMisses.addAndGet(other.getCacheMisses());
            loadTime.addAndGet(other.getLoadTime());
        }
    }
}