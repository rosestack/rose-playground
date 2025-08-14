package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.ParseException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 Nimbus 的 HS256 JWT Token 服务（最小可用版本）
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {
    private final RoseSecurityProperties properties;
    private final AuthenticationHook authenticationHook;
    private final ClaimMapper claimMapper;
    private final TokenRevocationStore revocationStore;

    @Override
    public TokenInfo createToken(UserDetails user) {
        Map<String, Object> custom = claimMapper == null ? Map.of() : claimMapper.toClaims(user);
        return JwtSupport.createToken(user.getUsername(), custom, properties);
    }

    @Override
    public boolean validateToken(String token) {
        if (revocationStore != null && revocationStore.isRevoked(token)) {
            return false;
        }
        try {
            JwtSupport.parseAndValidate(token, properties);
            return true;
        } catch (JwtTokenExpiredException ex) {
            authenticationHook.onTokenExpired(token);
            return false;
        } catch (JwtValidationException ex) {
            log.debug("JWT 验证失败: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public Optional<UserDetails> getUserDetails(String token) {
        try {
            JWTClaimsSet claims = JwtSupport.parseAndValidate(token, properties);
            if (claimMapper != null) {
                return Optional.ofNullable(claimMapper.fromClaims(claims.getClaims()));
            }
            return Optional.of(JwtSupport.defaultUserDetails(claims));
        } catch (JwtTokenExpiredException ex) {
            authenticationHook.onTokenExpired(token);
            return Optional.empty();
        } catch (JwtValidationException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TokenInfo> refreshToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            Instant issuedAt = jwt.getJWTClaimsSet().getIssueTime().toInstant();
            if (Instant.now().isAfter(issuedAt.plus(properties.getAuth().getToken().getRefreshWindow()))) {
                return Optional.empty();
            }
            String subject = jwt.getJWTClaimsSet().getSubject();
            UserDetails user = User.withUsername(subject)
                    .password("")
                    .authorities("ROLE_USER")
                    .build();
            return Optional.of(createToken(user));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    @Override
    public void revokeToken(String token) {
        if (revocationStore != null) {
            revocationStore.revoke(token);
        }
        authenticationHook.onTokenRevoked(token);
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
