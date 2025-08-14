package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token 处理器实现类
 *
 * <p>实现JWT的创建、解析、验证等核心功能，集成JwtKeyManager的密钥管理能力，
 * 消除JwtSupport和JwtTokenService的重复逻辑。</p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProcessorImpl implements JwtTokenProcessor {

    private final RoseSecurityProperties properties;
    private final JwtKeyManager keyManager;
    private final ClaimMapper claimMapper;

    @Override
    public TokenInfo createToken(UserDetails userDetails) {
        Map<String, Object> customClaims = claimMapper == null ? Map.of() : claimMapper.toClaims(userDetails);
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = buildClaims(userDetails.getUsername(), customClaims, properties.getJwt().getExpiration(), now);

            JWSHeader header = new JWSHeader.Builder(keyManager.algorithm())
                    .type(JOSEObjectType.JWT).build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign((JWSSigner) keyManager.signer());
            String accessToken = jwt.serialize();

            LocalDateTime expiresAt = LocalDateTime.ofInstant(claims.getExpirationTime().toInstant(), ZoneOffset.UTC);

            return TokenInfo.builder()
                    .accessToken(accessToken)
                    .refreshToken(UUID.randomUUID().toString())
                    .tokenType("jwt")
                    .expiresAt(expiresAt)
                    .username(userDetails.getUsername())
                    .createdAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC))
                    .build();
        } catch (Exception e) {
            log.error("创建JWT Token失败，用户名：{}", userDetails.getAuthorities(), e);
            throw new JwtValidationException("创建JWT Token失败", e);
        }
    }

    @Override
    public UserDetails parseToken(String accessToken) {
        return null;
    }

    @Override
    public JWTClaimsSet parseAndValidate(String accessToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);

            // 验证签名
            Object verifier = keyManager.getVerifierFor(jwt);
            boolean signatureValid = jwt.verify((JWSVerifier) (verifier != null ? verifier : keyManager.verifier()));
            if (!signatureValid) {
                throw new JwtValidationException("Invalid signature");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            // 验证标准声明
            if (!checkStandardClaims(claims)) {
                throw new JwtValidationException("Standard claims invalid");
            }

            return claims;
        } catch (JwtTokenExpiredException | JwtValidationException e) {
            throw e;
        } catch (Exception e) {
            log.debug("JWT Token解析验证失败: {}", e.getMessage());
            throw new JwtValidationException("Token parse/validate error", e);
        }
    }

    /**
     * 构建标准JWT声明集合
     */
    private JWTClaimsSet buildClaims(String username, Map<String, Object> customClaims,
                                     Duration expiration, Instant now) {
        Instant exp = now.plus(expiration);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString());

        // 添加自定义声明
        if (customClaims != null && !customClaims.isEmpty()) {
            customClaims.forEach(builder::claim);
        }

        return builder.build();
    }

    /**
     * 验证标准JWT声明
     */
    private boolean checkStandardClaims(JWTClaimsSet claims) {
        // 检查发行者
        if (properties.getJwt().getIssuer() != null &&
                !properties.getJwt().getIssuer().equals(claims.getIssuer())) {
            return false;
        }

        // 检查发行时间
        if (properties.getJwt().isRequireIssuedAt() && claims.getIssueTime() == null) {
            return false;
        }

        // 检查生效时间
        if (properties.getJwt().isRequireNotBefore() && claims.getNotBeforeTime() == null) {
            return false;
        }

        // 检查受众
        if (properties.getJwt().getAudience() != null && !properties.getJwt().getAudience().isEmpty()) {
            List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(properties.getJwt().getAudience()::contains)) {
                return false;
            }
        }

        return true;
    }
}
