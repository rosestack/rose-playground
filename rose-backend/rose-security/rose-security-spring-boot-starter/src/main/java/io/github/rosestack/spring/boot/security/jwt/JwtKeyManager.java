package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.jwt.loader.CacheableKeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.KeyLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtKeyManager {

    private final KeyLoader keyLoader;

    /**
     * 构造JWT密钥管理器（推荐）
     *
     * @param keyLoader 密钥加载器
     */
    public JwtKeyManager(KeyLoader keyLoader) {
        this.keyLoader = keyLoader;
    }

    /**
     * 创建JWT签名器
     */
    public JWSSigner createSigner() throws Exception {
        if (!keyLoader.supportsSigning()) {
            throw new UnsupportedOperationException(keyLoader.getType() + " 密钥加载器不支持签名操作");
        }

        try {
            JWSSigner signer = keyLoader.createSigner();
            log.debug("JWT签名器创建成功，类型: {}", keyLoader.getType());
            return signer;
        } catch (Exception e) {
            log.error("创建JWT签名器失败，类型: {}, 错误: {}", keyLoader.getType(), e.getMessage());
            throw e;
        }
    }

    /**
     * 创建JWT验证器（通用）
     */
    public JWSVerifier createVerifier() throws Exception {
        try {
            JWSVerifier verifier = keyLoader.createVerifier();
            log.debug("JWT验证器创建成功，类型: {}", keyLoader.getType());
            return verifier;
        } catch (Exception e) {
            log.warn("创建JWT验证器失败，类型: {}, 错误: {}", keyLoader.getType(), e.getMessage());
            throw e;
        }
    }

    /**
     * 为特定JWT创建验证器（用于密钥轮换场景）
     */
    public JWSVerifier createVerifierFor(SignedJWT jwt) throws Exception {
        try {
            JWSVerifier verifier = keyLoader.createVerifierFor(jwt);
            log.debug(
                    "为JWT创建验证器成功，类型: {}, kid: {}",
                    keyLoader.getType(),
                    jwt.getHeader().getKeyID());
            return verifier;
        } catch (Exception e) {
            log.warn("为JWT创建验证器失败，类型: {}, 错误: {}", keyLoader.getType(), e.getMessage());

            // 回退到通用验证器
            return createVerifier();
        }
    }

    /**
     * 触发密钥轮换（强制刷新密钥缓存）
     * 主要用于密钥轮换场景，比如：
     * - 定期轮换JWKS密钥
     * - 更新Keystore密钥
     * - 刷新HMAC密钥
     */
    public void rotateKeys() {
        log.info("开始密钥轮换，密钥类型: {}", keyLoader.getType());
        try {
            // 如果KeyLoader实现了缓存刷新接口，则调用刷新方法
            if (keyLoader instanceof CacheableKeyLoader) {
                ((CacheableKeyLoader) keyLoader).refreshCache();
                log.debug("缓存刷新成功");
            } else {
                // 对于不支持缓存的加载器（如HMAC），直接重新创建实例验证可用性
                if (keyLoader.supportsSigning()) {
                    keyLoader.createSigner();
                    log.debug("签名密钥验证成功");
                }
                keyLoader.createVerifier();
                log.debug("验证密钥验证成功");
            }
            log.info("密钥轮换完成");
        } catch (Exception e) {
            log.error("密钥轮换失败: {}", e.getMessage(), e);
            throw new RuntimeException("密钥轮换失败", e);
        }
    }

    /**
     * 获取密钥加载器状态信息（用于监控和调试）
     */
    public String getKeyLoaderStatus() {
        return String.format(
                "KeyLoader{type=%s, supportsSigning=%s}", keyLoader.getType(), keyLoader.supportsSigning());
    }
}
