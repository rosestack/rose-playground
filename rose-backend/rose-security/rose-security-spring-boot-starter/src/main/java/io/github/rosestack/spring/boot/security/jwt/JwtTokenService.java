package io.github.rosestack.spring.boot.security.jwt;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.impl.RedisTokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtConfigurationException;
import io.github.rosestack.spring.boot.security.jwt.factory.JwtKeyManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * 基于JWT + Redis的混合Token服务
 *
 * <p>设计思路：
 * <ul>
 *   <li>继承RedisTokenService，复用成熟的存储和会话管理逻辑</li>
 *   <li>仅重写token生成逻辑，使用JWT格式</li>
 *   <li>RefreshToken和会话管理完全复用Redis实现</li>
 *   <li>简化设计，避免重复实现复杂逻辑</li>
 * </ul>
 * </p>
 */
@Slf4j
public class JwtTokenService extends RedisTokenService {

    private final JwtHelper jwtHelper;
    private final RoseSecurityProperties properties;

    public JwtTokenService(
            RoseSecurityProperties properties,
            AuthenticationHook authenticationHook,
            RedisTemplate<String, Object> redisTemplate) {
        super(properties.getToken(), authenticationHook, redisTemplate);

        this.properties = properties;

        if (properties.getToken().getJwt() == null || !properties.getToken().getJwt().isEnabled()) {
            throw new JwtConfigurationException("JWT配置未启用或缺失");
        }

        JwtKeyManager keyManager = JwtKeyManagerFactory.create(properties);
        String algorithmName = properties.getToken().getJwt().getAlgorithm().name();
        this.jwtHelper = new JwtHelper(keyManager, algorithmName);
    }

    @Override
    protected String generateAccessToken(String username) {
        Duration expiration = properties.getToken().getAccessTokenExpiredTime();
        return jwtHelper.generateToken(username, expiration);
    }

    @Override
    protected TokenInfo buildTokenInfo(String username) {
        TokenInfo baseTokenInfo = super.buildTokenInfo(username);
        baseTokenInfo.setTokenType(TOKEN_TYPE_JWT);
        return baseTokenInfo;
    }
}