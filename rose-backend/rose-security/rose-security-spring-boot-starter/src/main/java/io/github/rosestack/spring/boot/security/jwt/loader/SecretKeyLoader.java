package io.github.rosestack.spring.boot.security.jwt.loader;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import io.github.rosestack.spring.boot.security.jwt.algorithm.JwtAlgorithmFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HMAC密钥加载器
 * 
 * <p>特点：
 * <ul>
 *   <li>使用共享密钥进行签名和验证</li>
 *   <li>无需复杂的密钥管理</li>
 *   <li>适用于单体应用或简单场景</li>
 *   <li>支持HS256/HS384/HS512算法</li>
 * </ul>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class SecretKeyLoader implements KeyLoader {
    
    private final String secret;
    
    @Override
    public JWSSigner createSigner() throws Exception {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置");
        }
        
        log.debug("创建HMAC签名器");
        return JwtAlgorithmFactory.createHmacSigner(secret);
    }
    
    @Override
    public JWSVerifier createVerifier() throws Exception {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置");
        }
        
        log.debug("创建HMAC验证器");
        return JwtAlgorithmFactory.createHmacVerifier(secret);
    }
    
    @Override
    public boolean supportsSigning() {
        return true;
    }
    
    @Override
    public String getType() {
        return "HMAC";
    }
}
