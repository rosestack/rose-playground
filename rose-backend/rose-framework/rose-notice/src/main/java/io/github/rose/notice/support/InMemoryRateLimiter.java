package io.github.rose.notice.support;

import io.github.rose.notice.SendRequest;
import io.github.rose.notice.spi.RateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单内存限流器，支持每分钟最大次数限制（全局/按目标）。
 */
public class InMemoryRateLimiter implements RateLimiter {
    private final int maxPerMinute;
    private final Map<String, AtomicInteger> counter = new ConcurrentHashMap<>();
    private volatile long lastReset = System.currentTimeMillis();

    public InMemoryRateLimiter(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
    }

    @Override
    public boolean allow(SendRequest request) {
        resetIfNeeded();
        String key = request.getTarget();
        counter.putIfAbsent(key, new AtomicInteger(0));
        return counter.get(key).get() < maxPerMinute;
    }

    @Override
    public void record(SendRequest request) {
        resetIfNeeded();
        String key = request.getTarget();
        counter.putIfAbsent(key, new AtomicInteger(0));
        counter.get(key).incrementAndGet();
    }

    private void resetIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastReset > 60_000) {
            counter.clear();
            lastReset = now;
        }
    }
}
