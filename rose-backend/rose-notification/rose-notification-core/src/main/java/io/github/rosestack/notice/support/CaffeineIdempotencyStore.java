package io.github.rosestack.notice.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.notice.spi.IdempotencyStore;
import java.time.Duration;

/**
 * 基于 Caffeine 的幂等存储，支持 TTL 与容量上限。
 */
public class CaffeineIdempotencyStore implements IdempotencyStore {
    private final Cache<String, Boolean> cache;

    public CaffeineIdempotencyStore() {
        this(10_000, Duration.ofHours(1));
    }

    public CaffeineIdempotencyStore(int maxSize, Duration ttl) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(100, maxSize))
                .expireAfterWrite(ttl)
                .build();
    }

    @Override
    public boolean exists(String requestId) {
        return cache.getIfPresent(requestId) != null;
    }

    @Override
    public void put(String requestId) {
        cache.put(requestId, Boolean.TRUE);
    }
}
