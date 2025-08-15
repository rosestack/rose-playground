package io.github.rosestack.spring.boot.security.jwt.algorithm;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT算法工厂 - 负责算法映射和签名器/验证器创建
 * 
 * <p>职责：
 * <ul>
 *   <li>算法字符串到JWSAlgorithm的映射</li>
 *   <li>根据算法和密钥材料创建JWSSigner</li>
 *   <li>根据算法和密钥材料创建JWSVerifier</li>
 *   <li>支持HS256/384/512、RS256/384/512、ES256/384/512算法</li>
 * </ul>
 * </p>
 */
@Slf4j
public class JwtAlgorithmFactory {
    
    /** 算法映射表 */
    private static final Map<String, JWSAlgorithm> ALGORITHM_MAP;
    
    static {
        ALGORITHM_MAP = new HashMap<>();
        ALGORITHM_MAP.put("HS256", JWSAlgorithm.HS256);
        ALGORITHM_MAP.put("HS384", JWSAlgorithm.HS384);
        ALGORITHM_MAP.put("HS512", JWSAlgorithm.HS512);
        ALGORITHM_MAP.put("RS256", JWSAlgorithm.RS256);
        ALGORITHM_MAP.put("RS384", JWSAlgorithm.RS384);
        ALGORITHM_MAP.put("RS512", JWSAlgorithm.RS512);
        ALGORITHM_MAP.put("ES256", JWSAlgorithm.ES256);
        ALGORITHM_MAP.put("ES384", JWSAlgorithm.ES384);
        ALGORITHM_MAP.put("ES512", JWSAlgorithm.ES512);
    }
    
    /**
     * 获取JWS算法
     * @param algorithmName 算法名称，如"HS256"、"RS256"等
     * @return JWS算法对象
     */
    public static JWSAlgorithm getJwsAlgorithm(String algorithmName) {
        if (algorithmName == null) {
            return JWSAlgorithm.HS256;
        }
        return ALGORITHM_MAP.getOrDefault(algorithmName.toUpperCase(), JWSAlgorithm.HS256);
    }
    
    /**
     * 创建HMAC签名器
     */
    public static JWSSigner createHmacSigner(String secret) throws Exception {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("HMAC secret不能为空");
        }
        return new MACSigner(secret.getBytes());
    }
    
    /**
     * 创建HMAC验证器
     */
    public static JWSVerifier createHmacVerifier(String secret) throws Exception {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("HMAC secret不能为空");
        }
        return new MACVerifier(secret.getBytes());
    }
    
    /**
     * 创建RSA签名器
     */
    public static JWSSigner createRsaSigner(RSAPrivateKey privateKey) throws Exception {
        if (privateKey == null) {
            throw new IllegalArgumentException("RSA私钥不能为空");
        }
        return new RSASSASigner(privateKey);
    }
    
    /**
     * 创建RSA验证器
     */
    public static JWSVerifier createRsaVerifier(RSAPublicKey publicKey) throws Exception {
        if (publicKey == null) {
            throw new IllegalArgumentException("RSA公钥不能为空");
        }
        return new RSASSAVerifier(publicKey);
    }
    
    /**
     * 创建ECDSA签名器
     */
    public static JWSSigner createEcSigner(ECPrivateKey privateKey) throws Exception {
        if (privateKey == null) {
            throw new IllegalArgumentException("EC私钥不能为空");
        }
        return new ECDSASigner(privateKey);
    }
    
    /**
     * 创建ECDSA验证器
     */
    public static JWSVerifier createEcVerifier(ECPublicKey publicKey) throws Exception {
        if (publicKey == null) {
            throw new IllegalArgumentException("EC公钥不能为空");
        }
        return new ECDSAVerifier(publicKey);
    }
    
    /**
     * 从JWK创建验证器
     */
    public static JWSVerifier createVerifierFromJwk(JWK jwk) throws Exception {
        if (jwk == null) {
            throw new IllegalArgumentException("JWK不能为空");
        }
        
        if (jwk instanceof RSAKey) {
            return createRsaVerifier(((RSAKey) jwk).toRSAPublicKey());
        } else if (jwk instanceof ECKey) {
            return createEcVerifier(((ECKey) jwk).toECPublicKey());
        } else if (jwk instanceof OctetSequenceKey) {
            return new MACVerifier(((OctetSequenceKey) jwk).toByteArray());
        }
        
        throw new IllegalArgumentException("不支持的JWK类型: " + jwk.getClass().getSimpleName());
    }
    
    /**
     * 判断是否为HMAC算法
     * @param algorithmName 算法名称
     * @return 是否为HMAC算法
     */
    public static boolean isHmacAlgorithm(String algorithmName) {
        if (algorithmName == null) return false;
        String upper = algorithmName.toUpperCase();
        return "HS256".equals(upper) || "HS384".equals(upper) || "HS512".equals(upper);
    }
    
    /**
     * 判断是否为RSA算法
     * @param algorithmName 算法名称
     * @return 是否为RSA算法
     */
    public static boolean isRsaAlgorithm(String algorithmName) {
        if (algorithmName == null) return false;
        String upper = algorithmName.toUpperCase();
        return "RS256".equals(upper) || "RS384".equals(upper) || "RS512".equals(upper);
    }
    
    /**
     * 判断是否为ECDSA算法
     * @param algorithmName 算法名称
     * @return 是否为ECDSA算法
     */
    public static boolean isEcAlgorithm(String algorithmName) {
        if (algorithmName == null) return false;
        String upper = algorithmName.toUpperCase();
        return "ES256".equals(upper) || "ES384".equals(upper) || "ES512".equals(upper);
    }
    
    /**
     * 获取支持的所有算法名称
     * @return 算法名称集合
     */
    public static java.util.Set<String> getSupportedAlgorithms() {
        return ALGORITHM_MAP.keySet();
    }
}
