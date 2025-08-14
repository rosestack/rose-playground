package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis 的 Token 撤销黑名单实现
 *
 * 设计：将被撤销的 token 作为 key，值为 "1"，并设置过期时间为 token 剩余存活时间，
 * 使黑名单自动过期，无需额外清理。
 */
@RequiredArgsConstructor
public class RedisRevocationStore implements TokenRevocationStore {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "rose:security:jwt:blacklist:";

    @Override
    public void revoke(String accessToken) {
        try {
            Instant now = Instant.now();
            Instant exp = extractExpiry(accessToken);
            long ttlSeconds = exp != null ? Math.max(0, exp.getEpochSecond() - now.getEpochSecond()) : Duration.ofDays(1).getSeconds();
            redisTemplate.opsForValue().set(key(accessToken), "1", ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 最坏情况下不设置 TTL，以确保撤销生效
            redisTemplate.opsForValue().set(key(accessToken), "1");
        }
    }

    @Override
    public boolean isRevoked(String accessToken) {
        Boolean has = redisTemplate.hasKey(key(accessToken));
        return has != null && has;
    }

    private String key(String accessToken) {
        return PREFIX + accessToken;
    }

    private Instant extractExpiry(String accessToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);
            if (jwt.getJWTClaimsSet().getExpirationTime() == null) return null;
            return jwt.getJWTClaimsSet().getExpirationTime().toInstant();
        } catch (Exception e) {
            return null;
        }
    }
}

