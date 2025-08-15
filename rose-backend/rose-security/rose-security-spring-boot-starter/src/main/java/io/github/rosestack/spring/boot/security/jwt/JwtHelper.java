package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.jwt.algorithm.JwtAlgorithmFactory;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 工具类
 *
 * <p>提供 JWT 令牌的生成和验证功能：
 * <ul>
 *   <li>生成 JWT 令牌</li>
 *   <li>验证 JWT 令牌</li>
 *   <li>提取 JWT 声明</li>
 * </ul>
 * </p>
 */
@Slf4j
public class JwtHelper {

    private final JwtKeyManager keyManager;
    private final String algorithmName;

    /**
     * 构造 JWT 工具类
     *
     * @param keyManager 密钥管理器
     * @param algorithmName 算法名称（如 HS256、RS256）
     */
    public JwtHelper(JwtKeyManager keyManager, String algorithmName) {
        if (keyManager == null) {
            throw new IllegalArgumentException("密钥管理器不能为空");
        }
        if (algorithmName == null || algorithmName.trim().isEmpty()) {
            throw new IllegalArgumentException("算法名称不能为空");
        }
        this.keyManager = keyManager;
        this.algorithmName = algorithmName;
    }

    /**
     * 构造 JWT 工具类（使用默认 HS256 算法）
     *
     * @param keyManager 密钥管理器
     */
    public JwtHelper(JwtKeyManager keyManager) {
        this(keyManager, "HS256");
    }

    /**
     * 生成 JWT 令牌
     *
     * @param subject            主题（用户名）
     * @param expirationDuration 过期时间
     * @return JWT 字符串
     */
    public String generateToken(String subject, Duration expirationDuration) {
        return generateToken(subject, expirationDuration, null, null);
    }

    /**
     * 生成 JWT 令牌（带签发者和受众）
     *
     * @param subject            主题（用户名）
     * @param expirationDuration 过期时间
     * @param issuer             签发者
     * @param audience           受众
     * @return JWT 字符串
     */
    public String generateToken(String subject, Duration expirationDuration, String issuer, String audience) {
        try {
            // 创建 JWT 声明
            Instant now = Instant.now();
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(expirationDuration)))
                    .jwtID(UUID.randomUUID().toString());

            if (issuer != null) {
                claimsBuilder.issuer(issuer);
            }

            if (audience != null) {
                claimsBuilder.audience(audience);
            }

            JWTClaimsSet claims = claimsBuilder.build();

            // 创建 JWT 头部
            JWSAlgorithm algorithm = JwtAlgorithmFactory.getJwsAlgorithm(algorithmName);
            JWSHeader header = new JWSHeader.Builder(algorithm).build();

            // 创建签名 JWT
            SignedJWT jwt = new SignedJWT(header, claims);

            // 签名
            JWSSigner signer = keyManager.createSigner();
            jwt.sign(signer);

            String tokenString = jwt.serialize();
            log.debug("JWT 生成成功，主题: {}", subject);
            return tokenString;

        } catch (Exception e) {
            log.error("JWT 生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("JWT 生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 JWT 令牌
     *
     * @param tokenString JWT 字符串
     * @return JWT 声明集合
     * @throws IllegalArgumentException 令牌格式无效或为空
     * @throws RuntimeException         签名验证失败、令牌过期或其他验证失败
     */
    public JWTClaimsSet validateToken(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT 令牌不能为空");
        }

        try {
            SignedJWT jwt = SignedJWT.parse(tokenString);

            // 签名验证
            JWSVerifier verifier = keyManager.createVerifierFor(jwt);
            if (!jwt.verify(verifier)) {
                throw new RuntimeException("JWT 签名验证失败");
            }

            // 获取声明
            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            // 过期时间验证
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                throw new RuntimeException("JWT 已过期");
            }

            // 生效时间验证
            Date notBeforeTime = claims.getNotBeforeTime();
            if (notBeforeTime != null && notBeforeTime.after(new Date())) {
                throw new RuntimeException("JWT 尚未生效");
            }

            log.debug("JWT 验证成功，主题: {}", claims.getSubject());
            return claims;

        } catch (ParseException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            throw new IllegalArgumentException("JWT 解析失败: " + e.getMessage(), e);
        } catch (JOSEException e) {
            log.warn("JWT 签名验证异常: {}", e.getMessage());
            throw new RuntimeException("JWT 签名验证异常: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("JWT 验证未知异常: {}", e.getMessage(), e);
            throw new RuntimeException("JWT 验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取用户名
     *
     * @param tokenString JWT 字符串
     * @return 用户名
     * @throws IllegalArgumentException 令牌格式无效或为空
     * @throws RuntimeException         验证失败
     */
    public String extractUsername(String tokenString) {
        JWTClaimsSet claims = validateToken(tokenString);
        return claims.getSubject();
    }

    /**
     * 检查令牌是否有效（不抛异常的验证方法）
     *
     * @param tokenString JWT 字符串
     * @return 是否有效
     */
    public boolean isTokenValid(String tokenString) {
        try {
            validateToken(tokenString);
            return true;
        } catch (Exception e) {
            log.debug("JWT 令牌无效: {}", e.getMessage());
            return false;
        }
    }
}
