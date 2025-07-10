package io.github.rose.user.service.impl;

import io.github.rose.user.service.JwtBlacklistService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "jwt:blacklist:";
    public JwtBlacklistServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Override
    public void add(String token) {
        // 黑名单有效期与 token 保持一致（1天）
        redisTemplate.opsForValue().set(PREFIX + token, "1", 1, TimeUnit.DAYS);
    }
    @Override
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(PREFIX + token);
    }
}
