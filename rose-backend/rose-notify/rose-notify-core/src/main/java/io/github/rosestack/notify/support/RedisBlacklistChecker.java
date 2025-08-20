package io.github.rosestack.notify.support;

import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notify.spi.BlacklistChecker;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于 Redis 的黑名单检查，使用 Set 维护。
 */
public class RedisBlacklistChecker implements BlacklistChecker {
    private final StringRedisTemplate redis;
    private final String setKey;

    public RedisBlacklistChecker(StringRedisTemplate redis) {
        this(redis, "rose:notice:blacklist");
    }

    public RedisBlacklistChecker(StringRedisTemplate redis, String setKey) {
        this.redis = redis;
        this.setKey = setKey;
    }

    @Override
    public boolean isBlacklisted(SendRequest request) {
        String target = request.getTarget();
        Boolean member = redis.opsForSet().isMember(setKey, target);
        return Boolean.TRUE.equals(member);
    }
}
