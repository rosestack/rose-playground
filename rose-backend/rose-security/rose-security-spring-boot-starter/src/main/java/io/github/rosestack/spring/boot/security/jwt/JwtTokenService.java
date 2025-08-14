package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.extension.AuthenticationHook;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

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
        try {
            Instant now = Instant.now();
            Instant exp = now.plus(properties.getJwt().getExpiration());

            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .jwtID(UUID.randomUUID().toString());

            Map<String, Object> custom = claimMapper == null ? Map.of() : claimMapper.toClaims(user);
            custom.forEach(builder::claim);

            JwtKeyManager km = new JwtKeyManager(properties);
            JWSHeader header = new JWSHeader.Builder(km.algorithm())
                    .type(JOSEObjectType.JWT)
                    .build();
            SignedJWT jwt = new SignedJWT(header, builder.build());
            try {
                jwt.sign((JWSSigner) km.signer());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            String token = jwt.serialize();
            LocalDateTime expiresAt = LocalDateTime.ofInstant(exp, ZoneOffset.UTC);
            return TokenInfo.builder()
                    .accessToken(token)
                    .refreshToken(UUID.randomUUID().toString())
                    .tokenType("jwt")
                    .expiresAt(expiresAt)
                    .username(user.getUsername())
                    .createdAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC))
                    .build();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (revocationStore != null && revocationStore.isRevoked(token)) {
            return false;
        }
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean sig;
            try {
                sig = jwt.verify((JWSVerifier) new JwtKeyManager(properties).verifier());
            } catch (Exception ex) {
                return false;
            }
            if (!sig) return false;
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null
                    || exp.toInstant()
                            .isBefore(Instant.now().minus(properties.getJwt().getClockSkew()))) {
                return false;
            }
            // 标准声明可选校验
            if (properties.getJwt().getIssuer() != null
                    && !properties.getJwt().getIssuer().equals(claims.getIssuer())) {
                return false;
            }
            if (properties.getJwt().isRequireIssuedAt() && claims.getIssueTime() == null) {
                return false;
            }
            if (properties.getJwt().isRequireNotBefore() && claims.getNotBeforeTime() == null) {
                return false;
            }
            if (properties.getJwt().getAudience() != null
                    && !properties.getJwt().getAudience().isEmpty()) {
                java.util.List<String> aud = claims.getAudience();
                if (aud == null || aud.stream().noneMatch(properties.getJwt().getAudience()::contains)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.debug("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<UserDetails> getUserDetails(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean sig;
            try {
                sig = jwt.verify((JWSVerifier) new JwtKeyManager(properties).verifier());
            } catch (Exception ex) {
                return Optional.empty();
            }
            if (!sig) return Optional.empty();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null
                    || claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
                authenticationHook.onTokenExpired(token);
                return Optional.empty();
            }
            if (claimMapper != null) {
                return Optional.ofNullable(claimMapper.fromClaims(claims.getClaims()));
            }
            return Optional.of(User.withUsername(claims.getSubject())
                    .password("")
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TokenInfo> refreshToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            Instant issuedAt = jwt.getJWTClaimsSet().getIssueTime().toInstant();
            if (Instant.now()
                    .isAfter(issuedAt.plus(properties.getAuth().getToken().getRefreshWindow()))) {
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
