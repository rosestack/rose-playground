package io.github.rosestack.spring.boot.security.jwt;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.impl.RedisTokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于JWT + Redis的混合Token服务
 */
@Slf4j
public class JwtTokenService extends RedisTokenService {
    public JwtTokenService(
            RoseSecurityProperties.Auth.Token properties,
            AuthenticationHook authenticationHook,
            RedisTemplate<String, Object> redisTemplate) {
        super(properties, authenticationHook, redisTemplate);
    }

    @Override
    protected String generateAccessToken() {
        //TODO 实现JWT生成逻辑
        return null;
    }

    @Override
    protected TokenInfo buildTokenInfo(String username) {
        TokenInfo baseTokenInfo = super.buildTokenInfo(username);
        baseTokenInfo.setTokenType(TOKEN_TYPE_JWT);
        return baseTokenInfo;
    }
}
