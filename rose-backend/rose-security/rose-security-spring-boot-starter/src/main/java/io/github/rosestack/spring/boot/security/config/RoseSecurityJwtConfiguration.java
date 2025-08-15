package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.jwt.JwtTokenService;
import io.github.rosestack.spring.boot.security.jwt.TokenManagementHook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.security.token.jwt", name = "enabled", havingValue = "true")
public class RoseSecurityJwtConfiguration {
    private final RoseSecurityProperties properties;

    // JWT 开关：开启时注册 JwtTokenService 作为首选 TokenService
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisTemplate.class)
    public TokenService jwtTokenService(
            TokenManagementHook authenticationHook, RedisTemplate<String, Object> redisTemplate) {
        return new JwtTokenService(properties.getToken(), authenticationHook, redisTemplate);
    }
}
