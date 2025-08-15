package io.github.rosestack.spring.boot.security.jwt;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.impl.RedisTokenService;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtTokenExpiredException;
import io.github.rosestack.spring.boot.security.jwt.factory.JwtKeyManagerFactory;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

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

    public JwtTokenService(
            RoseSecurityProperties.Token properties,
            TokenManagementHook authenticationHook,
            RedisTemplate<String, Object> redisTemplate) {
        super(properties, authenticationHook, redisTemplate);

        JwtKeyManager keyManager = JwtKeyManagerFactory.create(properties);
        String algorithmName = properties.getJwt().getAlgorithm().name();
        this.jwtHelper = new JwtHelper(keyManager, algorithmName);
    }

    @Override
    protected String generateAccessToken(String username) {
        Duration expiration = properties.getAccessTokenExpiredTime();
        return jwtHelper.generateToken(username, expiration);
    }

    @Override
    protected TokenInfo buildTokenInfo(String username) {
        TokenInfo baseTokenInfo = super.buildTokenInfo(username);
        baseTokenInfo.setTokenType(TOKEN_TYPE_JWT);
        return baseTokenInfo;
    }

    @Override
    protected boolean additionalValidation(String accessToken, TokenInfo tokenInfo) {
        try {
            jwtHelper.validateToken(accessToken);
            log.debug("JWT token 验证通过: {}", StringUtils.abbreviate(accessToken, 8));
            return true;
        } catch (JwtTokenExpiredException e) {
            // 特殊处理：JWT token 过期，调用 hook 回调
            authenticationHook.onTokenExpired(accessToken);
            log.warn("JWT token 已过期: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("JWT token 验证失败: {}", e.getMessage());
            return false;
        }
    }
}
