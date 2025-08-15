package io.github.rosestack.spring.boot.security.jwt;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.impl.RedisTokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于JWT + Redis的混合Token服务
 *
 * <p>结合JWT和Redis的优势：
 * <ul>
 *   <li>AccessToken使用JWT格式，无状态验证</li>
 *   <li>RefreshToken存储在Redis中，支持撤销和会话管理</li>
 *   <li>用户会话信息存储在Redis中，支持并发会话限制</li>
 * </ul>
 * </p>
 */
@Slf4j
public class JwtTokenService extends RedisTokenService {
    private final TokenRevocationStore revocationStore;

    public JwtTokenService(
            TokenRevocationStore revocationStore,
            RoseSecurityProperties.Auth.Token properties,
            AuthenticationHook authenticationHook,
            RedisTemplate<String, Object> redisTemplate) {
        super(properties, authenticationHook, redisTemplate);
        this.revocationStore = revocationStore;
    }


    @Override
    protected String generateAccessToken() {
        return super.generateAccessToken();
    }
}
