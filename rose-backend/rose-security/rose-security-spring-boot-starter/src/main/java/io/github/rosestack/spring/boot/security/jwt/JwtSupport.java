package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 集中化 JWT 常用操作，减少各处重复代码
 */
public final class JwtSupport {
    private JwtSupport() {
    }

    // 可选注入的 RestTemplate（供 JwtKeyManager 使用）
    private static volatile org.springframework.web.client.RestTemplate restTemplate;

    public static void setRestTemplate(org.springframework.web.client.RestTemplate rt) {
        restTemplate = rt;
    }

    // 内部集中管理 JwtKeyManager，后续若迁移实现，外部无需感知
    private static JwtKeyManager getKeyManager(RoseSecurityProperties properties) {
        JwtKeyManager km = new JwtKeyManager(properties);
        if (restTemplate != null) {
            try {
                km.setRestTemplate(restTemplate);
            } catch (Throwable ignore) {
            }
        }
        return km;
    }

    /**
     * 构建标准 claims 并合入自定义 claims
     */
    public static JWTClaimsSet buildClaims(String username,
                                           Map<String, Object> custom,
                                           Duration expiration,
                                           Instant now) {
        Instant exp = now.plus(expiration);
        JWTClaimsSet.Builder b = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString());
        if (custom != null && !custom.isEmpty()) {
            custom.forEach(b::claim);
        }
        return b.build();
    }

    /**
     * 验证签名并返回 claims（包含标准声明校验）
     */
    public static Optional<JWTClaimsSet> verifyAndGetClaims(String token, RoseSecurityProperties properties) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            Object verifier = getKeyManager(properties).getVerifierFor(jwt);
            boolean sig = jwt.verify((JWSVerifier) (verifier != null ? verifier : getKeyManager(properties).verifier()));
            if (!sig) {
                return Optional.empty();
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!checkStandardClaims(properties, claims)) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 标准声明校验（过期、issuer、iat、nbf、aud）
     */
    public static boolean checkStandardClaims(RoseSecurityProperties properties, JWTClaimsSet claims) {
        Instant now = Instant.now();
        if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(now.minus(properties.getJwt().getClockSkew()))) {
            return false;
        }
        if (properties.getJwt().getIssuer() != null && !properties.getJwt().getIssuer().equals(claims.getIssuer())) {
            return false;
        }
        if (properties.getJwt().isRequireIssuedAt() && claims.getIssueTime() == null) {
            return false;
        }
        if (properties.getJwt().isRequireNotBefore() && claims.getNotBeforeTime() == null) {
            return false;
        }
        if (properties.getJwt().getAudience() != null && !properties.getJwt().getAudience().isEmpty()) {
            java.util.List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(properties.getJwt().getAudience()::contains)) {
                return false;
            }
        }
        return true;
    }

    public static TokenInfo createToken(String username, Map<String, Object> custom, RoseSecurityProperties props) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = buildClaims(username, custom, props.getJwt().getExpiration(), now);
            JwtKeyManager km = getKeyManager(props);
            JWSHeader header = new JWSHeader.Builder(km.algorithm())
                    .type(JOSEObjectType.JWT)
                    .build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign((JWSSigner) km.signer());
            String token = jwt.serialize();

            LocalDateTime expiresAt = LocalDateTime.ofInstant(claims.getExpirationTime().toInstant(), ZoneOffset.UTC);

            return TokenInfo.builder()
                    .accessToken(token)
                    .refreshToken(UUID.randomUUID().toString())
                    .tokenType("jwt")
                    .expiresAt(expiresAt)
                    .username(username)
                    .createdAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 抛出式校验：返回 claims，过期抛 JwtTokenExpiredException，其它失败抛 JwtValidationException
    public static JWTClaimsSet parseAndValidate(String token, RoseSecurityProperties props) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            Object verifier = getKeyManager(props).getVerifierFor(jwt);
            boolean sig = jwt.verify((com.nimbusds.jose.JWSVerifier) (verifier != null ? verifier : getKeyManager(props).verifier()));
            if (!sig) throw new JwtValidationException("Invalid signature");
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Instant now = Instant.now();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(now.minus(props.getJwt().getClockSkew()))) {
                throw new JwtTokenExpiredException("Token expired");
            }
            if (!checkStandardClaims(props, claims)) throw new JwtValidationException("Standard claims invalid");
            return claims;
        } catch (JwtTokenExpiredException e) {
            throw e;
        } catch (JwtValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtValidationException("Token parse/validate error", e);
        }
    }

    // 便捷静态方法：解析 subject（验签 + 标准校验）
    public static Optional<String> parseSubject(String token, RoseSecurityProperties props) {
        try {
            return Optional.of(parseAndValidate(token, props).getSubject());
        } catch (JwtTokenExpiredException | JwtValidationException e) {
            return Optional.empty();
        }
    }

    // 便捷静态方法：校验 token（验签 + 标准校验）
    public static boolean validate(String token, RoseSecurityProperties props) {
        try {
            parseAndValidate(token, props);
            return true;
        } catch (JwtTokenExpiredException | JwtValidationException e) {
            return false;
        }
    }

    /**
     * 根据 claims 构造默认 UserDetails
     */
    public static UserDetails defaultUserDetails(JWTClaimsSet claims) {
        return User.withUsername(claims.getSubject())
                .password("")
                .authorities("ROLE_USER")
                .build();
    }
}

