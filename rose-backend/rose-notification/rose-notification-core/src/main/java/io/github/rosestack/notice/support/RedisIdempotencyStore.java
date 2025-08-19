package io.github.rosestack.notice.support;

import io.github.rosestack.notice.spi.IdempotencyStore;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于 Redis 的幂等存储，采用 setIfAbsent + 过期时间。
 */
public class RedisIdempotencyStore implements IdempotencyStore {
    private final StringRedisTemplate redis;
    private final String keyPrefix;
    private final Duration ttl;

    public RedisIdempotencyStore(StringRedisTemplate redis) {
        this(redis, "rose:notification:idemp:", Duration.ofHours(1));
    }

    public RedisIdempotencyStore(StringRedisTemplate redis, String keyPrefix, Duration ttl) {
        this.redis = redis;
        this.keyPrefix = keyPrefix;
        this.ttl = ttl;
    }

    @Override
    public boolean exists(String requestId) {
        Boolean hasKey = redis.hasKey(keyPrefix + requestId);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public void put(String requestId) {
        redis.opsForValue().setIfAbsent(keyPrefix + requestId, "1", ttl);
    }
}
