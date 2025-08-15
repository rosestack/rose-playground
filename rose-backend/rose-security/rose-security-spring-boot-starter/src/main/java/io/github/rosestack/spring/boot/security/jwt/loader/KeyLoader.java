package io.github.rosestack.spring.boot.security.jwt.loader;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;

/**
 * 密钥加载器接口
 *
 * <p>不同的密钥类型实现不同的加载策略：
 * <ul>
 *   <li>SecretKeyLoader：HMAC密钥加载</li>
 *   <li>KeystoreKeyLoader：本地密钥库加载</li>
 *   <li>JwksKeyLoader：远程JWKS加载</li>
 * </ul>
 * </p>
 */
public interface KeyLoader {

    /**
     * 创建签名器
     * @return JWT签名器
     * @throws Exception 加载失败时抛出异常
     */
    JWSSigner createSigner() throws Exception;

    /**
     * 创建验证器（通用）
     * @return JWT验证器
     * @throws Exception 加载失败时抛出异常
     */
    JWSVerifier createVerifier() throws Exception;

    /**
     * 为特定JWT创建验证器（主要用于JWKS场景）
     * @param jwt 待验证的JWT
     * @return JWT验证器
     * @throws Exception 加载失败时抛出异常
     */
    default JWSVerifier createVerifierFor(SignedJWT jwt) throws Exception {
        return createVerifier();
    }

    /**
     * 检查是否支持签名操作
     * @return 是否支持签名
     */
    boolean supportsSigning();

    /**
     * 获取加载器类型名称（用于日志和调试）
     * @return 类型名称
     */
    String getType();
}
