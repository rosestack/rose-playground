package io.github.rosestack.i18n.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存统计信息类
 */
public class CacheStatistics {
    /**
     * 缓存命中次数
     */
    private final AtomicLong hitCount = new AtomicLong(0);

    /**
     * 缓存未命中次数
     */
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * 缓存写入次数
     */
    private final AtomicLong putCount = new AtomicLong(0);

    /**
     * 缓存淘汰次数
     */
    private final AtomicLong evictionCount = new AtomicLong(0);

    public void recordHit() {
        hitCount.incrementAndGet();
    }

    public void recordMiss() {
        missCount.incrementAndGet();
    }

    public void recordPut() {
        putCount.incrementAndGet();
    }

    public void recordEviction(int count) {
        evictionCount.addAndGet(count);
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public long getMissCount() {
        return missCount.get();
    }

    public long getPutCount() {
        return putCount.get();
    }

    public long getEvictionCount() {
        return evictionCount.get();
    }

    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total;
    }

    @Override
    public String toString() {
        return String.format(
                "CacheStatistics{hitCount=%d, missCount=%d, putCount=%d, evictionCount=%d, hitRate=%.2f%%}",
                hitCount.get(), missCount.get(), putCount.get(), evictionCount.get(), getHitRate() * 100);
    }
}
