package io.github.rosestack.spring.boot.security.jwt;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.UserTokenInfo;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtTokenExpiredException;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * 基于 Nimbus 的 HS256 JWT Token 服务（最小可用版本）
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {
    private final RoseSecurityProperties properties;
    private final AuthenticationHook authenticationHook;
    private final TokenRevocationStore revocationStore;
    private final JwtTokenProcessor jwtTokenProcessor;

    @Override
    public UserTokenInfo createToken(UserDetails user) {
        return null;
    }

    @Override
    public boolean validateToken(String accessToken) {
        if (revocationStore != null && revocationStore.isRevoked(accessToken)) {
            return false;
        }
        try {
            jwtTokenProcessor.parseAndValidate(accessToken);
            return true;
        } catch (JwtTokenExpiredException ex) {
            authenticationHook.onTokenExpired(accessToken);
            return false;
        } catch (JwtValidationException ex) {
            log.debug("JWT 验证失败: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public UserDetails getUserDetails(String accessToken) {
        try {
            UserDetails userDetails = jwtTokenProcessor.parseToken(accessToken);
            return userDetails;
        } catch (JwtTokenExpiredException ex) {
            authenticationHook.onTokenExpired(accessToken);
            return null;
        } catch (JwtValidationException ex) {
            return null;
        }
    }

    @Override
    public UserTokenInfo refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public void revokeToken(String accessToken) {
        if (revocationStore != null) {
            revocationStore.revoke(accessToken);
        }
        authenticationHook.onTokenRevoked(accessToken);
    }

    @Override
    public void revokeAllTokens(String username) {
        // 简化：针对 JWT 无集中存储，调用钩子留给业务侧实现
        authenticationHook.onRevoked(username);
    }

    @Override
    public int getActiveTokenCount(String username) {
        // JWT 无状态默认返回 0 或无法统计的标识，这里返回 0
        return 0;
    }
}
