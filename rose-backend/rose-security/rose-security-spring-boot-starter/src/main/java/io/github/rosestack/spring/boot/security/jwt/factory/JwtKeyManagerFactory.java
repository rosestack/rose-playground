package io.github.rosestack.spring.boot.security.jwt.factory;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.jwt.JwtKeyManager;
import io.github.rosestack.spring.boot.security.jwt.exception.JwtException;
import io.github.rosestack.spring.boot.security.jwt.loader.JwksKeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.KeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.KeystoreKeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.SecretKeyLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT密钥管理器工厂
 *
 * <p>简化版工厂，专注于从配置创建密钥管理器
 */
@Slf4j
public class JwtKeyManagerFactory {

    /**
     * 从配置对象创建JWT密钥管理器
     *
     * @param properties 安全配置
     * @return JWT密钥管理器
     */
    public static JwtKeyManager create(RoseSecurityProperties.Token properties) {
        if (properties == null || properties.getJwt() == null) {
            throw new JwtException("JWT配置不能为空");
        }

        KeyLoader keyLoader = createKeyLoader(properties);
        return new JwtKeyManager(keyLoader);
    }

    /**
     * 根据配置创建密钥加载器
     */
    private static KeyLoader createKeyLoader(RoseSecurityProperties.Token properties) {
        RoseSecurityProperties.Token.Jwt jwtConfig = properties.getJwt();
        String algorithmName = jwtConfig.getAlgorithm().name();
        RoseSecurityProperties.Token.Jwt.Key keyConfig = jwtConfig.getKey();

        switch (keyConfig.getType()) {
            case JWK:
                if (keyConfig.getJwkSetUri() == null
                        || keyConfig.getJwkSetUri().trim().isEmpty()) {
                    throw new JwtException("JWK类型需要配置jwkSetUri");
                }
                return new JwksKeyLoader(
                        algorithmName,
                        keyConfig.getJwkSetUri(),
                        keyConfig.getJwkMaxRetries(),
                        keyConfig.getRotationInterval(),
                        keyConfig.isJwkFallbackToCache());

            case KEYSTORE:
                if (keyConfig.getKeystorePath() == null
                        || keyConfig.getKeystorePath().trim().isEmpty()) {
                    throw new JwtException("KEYSTORE类型需要配置keystorePath");
                }
                if (keyConfig.getKeyAlias() == null
                        || keyConfig.getKeyAlias().trim().isEmpty()) {
                    throw new JwtException("KEYSTORE类型需要配置keyAlias");
                }
                return new KeystoreKeyLoader(
                        algorithmName,
                        keyConfig.getKeystorePath(),
                        keyConfig.getKeystorePassword(),
                        keyConfig.getKeyAlias(),
                        keyConfig.getRotationInterval());

            case SECRET:
            default:
                String secret = jwtConfig.getSecret();
                if (secret == null || secret.trim().isEmpty()) {
                    throw new JwtException("SECRET类型需要配置secret");
                }
                return new SecretKeyLoader(secret);
        }
    }
}
