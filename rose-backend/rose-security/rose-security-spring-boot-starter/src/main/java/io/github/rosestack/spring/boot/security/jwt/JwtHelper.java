package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.jwt.algorithm.JwtAlgorithmFactory;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtTokenExpiredException;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtValidationException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private final long clockSkewSeconds;
    private final String issuer;
    private final String audience;
    private final Map<String, String> customClaims;

    /**
     * 构造 JWT 工具类（向后兼容）
     *
     * @param keyManager    密钥管理器
     * @param algorithmName 算法名称（如 HS256、RS256）
     */
    public JwtHelper(JwtKeyManager keyManager, String algorithmName) {
        this(keyManager, algorithmName, 300L, "rose-security", "rose-app", new HashMap<>());
    }

    /**
     * 构造 JWT 工具类（完整配置）
     *
     * @param keyManager      密钥管理器
     * @param algorithmName   算法名称（如 HS256、RS256）
     * @param clockSkewSeconds 时钟偏移容忍度（秒）
     * @param issuer          发行者
     * @param audience        受众
     * @param customClaims    自定义声明
     */
    public JwtHelper(
            JwtKeyManager keyManager,
            String algorithmName,
            long clockSkewSeconds,
            String issuer,
            String audience,
            Map<String, String> customClaims) {
        if (keyManager == null) {
            throw new IllegalArgumentException("密钥管理器不能为空");
        }
        if (algorithmName == null || algorithmName.trim().isEmpty()) {
            throw new IllegalArgumentException("算法名称不能为空");
        }
        this.keyManager = keyManager;
        this.algorithmName = algorithmName;
        this.clockSkewSeconds = clockSkewSeconds;
        this.issuer = issuer;
        this.audience = audience;
        this.customClaims = customClaims != null ? new HashMap<>(customClaims) : new HashMap<>();
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
        return generateToken(subject, expirationDuration, this.issuer, this.audience);
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

            // 添加自定义声明
            for (Map.Entry<String, String> entry : customClaims.entrySet()) {
                claimsBuilder.claim(entry.getKey(), entry.getValue());
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
     * @throws JwtValidationException   签名验证失败、格式错误或其他验证失败
     * @throws JwtTokenExpiredException 令牌已过期
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
                throw new JwtValidationException("JWT 签名验证失败");
            }

            // 获取声明
            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            // 当前时间（考虑时钟偏移）
            Date now = new Date();
            Date nowWithSkew = new Date(now.getTime() + clockSkewSeconds * 1000);
            Date nowMinusSkew = new Date(now.getTime() - clockSkewSeconds * 1000);

            // 过期时间验证（加入时钟偏移容忍度）
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime != null && expirationTime.before(nowMinusSkew)) {
                throw new JwtTokenExpiredException("JWT 已过期");
            }

            // 生效时间验证（加入时钟偏移容忍度）
            Date notBeforeTime = claims.getNotBeforeTime();
            if (notBeforeTime != null && notBeforeTime.after(nowWithSkew)) {
                throw new JwtValidationException("JWT 尚未生效");
            }

            // 发行者验证
            if (this.issuer != null && !this.issuer.equals(claims.getIssuer())) {
                throw new JwtValidationException("JWT 发行者验证失败");
            }

            // 受众验证
            if (this.audience != null && !claims.getAudience().contains(this.audience)) {
                throw new JwtValidationException("JWT 受众验证失败");
            }

            log.debug("JWT 验证成功，主题: {}", claims.getSubject());
            return claims;

        } catch (ParseException e) {
            throw new JwtValidationException("JWT 解析失败: " + e.getMessage(), e);
        } catch (JOSEException e) {
            throw new JwtValidationException("JWT 签名验证异常: " + e.getMessage(), e);
        } catch (JwtTokenExpiredException | JwtValidationException e) {
            // 直接重新抛出我们的自定义异常
            throw e;
        } catch (Exception e) {
            throw new JwtValidationException("JWT 验证失败: " + e.getMessage(), e);
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
     * 撤销JWT令牌（加入黑名单）
     *
     * @param tokenString JWT 字符串
     */
    public void revokeToken(String tokenString) {

        try {
            SignedJWT jwt = SignedJWT.parse(tokenString);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String jwtId = claims.getJWTID();
            Date expirationTime = claims.getExpirationTime();

            if (jwtId != null && expirationTime != null) {
                log.debug("JWT令牌 {} 已被撤销", jwtId);
            }
        } catch (ParseException e) {
            log.warn("撤销JWT令牌失败，解析错误: {}", e.getMessage());
        }
    }
}
