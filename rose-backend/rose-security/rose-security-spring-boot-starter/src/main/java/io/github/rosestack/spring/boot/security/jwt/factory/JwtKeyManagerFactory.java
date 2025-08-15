package io.github.rosestack.spring.boot.security.jwt.factory;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.jwt.JwtKeyManager;
import io.github.rosestack.spring.boot.security.jwt.algorithm.JwtAlgorithmFactory;
import io.github.rosestack.spring.boot.security.jwt.loader.JwksKeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.KeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.KeystoreKeyLoader;
import io.github.rosestack.spring.boot.security.jwt.loader.SecretKeyLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * JWT密钥管理器工厂
 *
 * <p>职责：
 * <ul>
 *   <li>根据配置创建JwtKeyManager</li>
 *   <li>处理配置对象到具体参数的转换</li>
 *   <li>提供多种创建方式（配置对象、构建器模式）</li>
 *   <li>支持配置验证和默认值设置</li>
 * </ul>
 * </p>
 */
@Slf4j
public class JwtKeyManagerFactory {

    /**
     * 从配置对象创建JWT密钥管理器
     *
     * @param properties 安全配置
     * @return JWT密钥管理器
     */
    public static JwtKeyManager create(RoseSecurityProperties properties) {
        validateConfiguration(properties);

        KeyLoader keyLoader = createKeyLoader(properties);
        return new JwtKeyManager(keyLoader);
    }

    /**
     * 创建HMAC密钥管理器
     *
     * @param algorithmName 算法名称（HS256、HS384、HS512）
     * @param secret        密钥
     * @return JWT密钥管理器
     */
    public static JwtKeyManager createHmac(String algorithmName, String secret) {
        validateHmacParameters(algorithmName, secret);

        KeyLoader keyLoader = new SecretKeyLoader(secret);
        return new JwtKeyManager(keyLoader);
    }

    /**
     * 创建Keystore密钥管理器
     *
     * @param algorithmName    算法名称（RS256、ES256等）
     * @param keystorePath     密钥库路径
     * @param keystorePassword 密钥库密码
     * @param keyAlias         密钥别名
     * @param refreshInterval  刷新间隔
     * @return JWT密钥管理器
     */
    public static JwtKeyManager createKeystore(String algorithmName, String keystorePath,
                                               String keystorePassword, String keyAlias,
                                               Duration refreshInterval) {
        validateKeystoreParameters(algorithmName, keystorePath, keyAlias);

        KeyLoader keyLoader = new KeystoreKeyLoader(
                algorithmName, keystorePath, keystorePassword, keyAlias, refreshInterval);
        return new JwtKeyManager(keyLoader);
    }

        /**
     * 创建JWKS密钥管理器
     *
     * @param algorithmName   算法名称
     * @param jwkSetUri       JWKS端点URI
     * @param maxRetries      最大重试次数
     * @param refreshInterval 刷新间隔
     * @param fallbackToCache 是否回退到缓存
     * @return JWT密钥管理器
     */
    public static JwtKeyManager createJwks(String algorithmName, String jwkSetUri,
                                            int maxRetries, Duration refreshInterval,
                                            boolean fallbackToCache) {
        validateJwksParameters(algorithmName, jwkSetUri);

        KeyLoader keyLoader = new JwksKeyLoader(
                algorithmName, jwkSetUri, maxRetries, refreshInterval, fallbackToCache);
        return new JwtKeyManager(keyLoader);
    }
    
    /**
     * 创建简单的JWKS密钥管理器（使用默认参数）
     *
     * @param algorithmName 算法名称
     * @param jwkSetUri     JWKS端点URI
     * @return JWT密钥管理器
     */
    public static JwtKeyManager createSimpleJwks(String algorithmName, String jwkSetUri) {
        return createJwks(algorithmName, jwkSetUri, 3, Duration.ofMinutes(5), true);
    }

    /**
     * 创建构建器
     *
     * @return JWT密钥管理器构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 根据配置创建密钥加载器
     */
    private static KeyLoader createKeyLoader(RoseSecurityProperties properties) {
        String algorithmName = properties.getJwt().getAlgorithm().name();
        RoseSecurityProperties.Jwt.Key keyConfig = properties.getJwt().getKey();

        if (keyConfig != null && keyConfig.getType() == RoseSecurityProperties.Jwt.Key.KeyType.JWK) {
            return new JwksKeyLoader(
                    algorithmName,
                    keyConfig.getJwkSetUri(),
                    keyConfig.getJwkMaxRetries(),
                    keyConfig.getRotationInterval(),
                    keyConfig.isJwkFallbackToCache()
            );
        } else if (keyConfig != null && keyConfig.getType() == RoseSecurityProperties.Jwt.Key.KeyType.KEYSTORE) {
            return new KeystoreKeyLoader(
                    algorithmName,
                    keyConfig.getKeystorePath(),
                    keyConfig.getKeystorePassword(),
                    keyConfig.getKeyAlias(),
                    keyConfig.getRotationInterval()
            );
        } else {
            String secret = properties.getJwt().getSecret();
            if (secret == null || secret.trim().isEmpty()) {
                throw new IllegalStateException("HMAC算法需要配置JWT密钥");
            }
            return new SecretKeyLoader(secret);
        }
    }

    /**
     * 验证配置
     */
    private static void validateConfiguration(RoseSecurityProperties properties) {
        if (properties == null || properties.getJwt() == null) {
            throw new IllegalArgumentException("JWT配置不能为空");
        }

        if (properties.getJwt().getAlgorithm() == null) {
            throw new IllegalArgumentException("JWT算法不能为空");
        }
    }

    private static void validateHmacParameters(String algorithmName, String secret) {
        if (!JwtAlgorithmFactory.isHmacAlgorithm(algorithmName)) {
            throw new IllegalArgumentException("无效的HMAC算法: " + algorithmName);
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("HMAC密钥不能为空");
        }
    }

    private static void validateKeystoreParameters(String algorithmName, String keystorePath, String keyAlias) {
        if (!JwtAlgorithmFactory.isRsaAlgorithm(algorithmName) &&
                !JwtAlgorithmFactory.isEcAlgorithm(algorithmName)) {
            throw new IllegalArgumentException("Keystore仅支持RSA和ECDSA算法: " + algorithmName);
        }
        if (keystorePath == null || keystorePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Keystore路径不能为空");
        }
        if (keyAlias == null || keyAlias.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥别名不能为空");
        }
    }

    private static void validateJwksParameters(String algorithmName, String jwkSetUri) {
        if (algorithmName == null || algorithmName.trim().isEmpty()) {
            throw new IllegalArgumentException("算法名称不能为空");
        }
        if (jwkSetUri == null || jwkSetUri.trim().isEmpty()) {
            throw new IllegalArgumentException("JWKS URI不能为空");
        }
    }

    /**
     * JWT密钥管理器构建器
     */
    public static class Builder {
        private String algorithmName;
        private String keyType;
        private String secret;
        private String keystorePath;
        private String keystorePassword;
        private String keyAlias;
        private String jwkSetUri;
        private int maxRetries = 3;
        private Duration refreshInterval = Duration.ofMinutes(5);
        private boolean fallbackToCache = true;
        private RestTemplate restTemplate;

        public Builder algorithm(String algorithmName) {
            this.algorithmName = algorithmName;
            return this;
        }

        public Builder hmac(String secret) {
            this.keyType = "HMAC";
            this.secret = secret;
            return this;
        }

        public Builder keystore(String keystorePath, String keystorePassword, String keyAlias) {
            this.keyType = "KEYSTORE";
            this.keystorePath = keystorePath;
            this.keystorePassword = keystorePassword;
            this.keyAlias = keyAlias;
            return this;
        }

        public Builder jwks(String jwkSetUri) {
            this.keyType = "JWKS";
            this.jwkSetUri = jwkSetUri;
            return this;
        }

        public Builder maxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }

        public Builder refreshInterval(Duration interval) {
            this.refreshInterval = interval;
            return this;
        }

        public Builder fallbackToCache(boolean fallback) {
            this.fallbackToCache = fallback;
            return this;
        }

        public Builder restTemplate(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
            return this;
        }

        public JwtKeyManager build() {
            if (algorithmName == null) {
                throw new IllegalStateException("算法名称必须设置");
            }

            KeyLoader keyLoader;
            switch (keyType) {
                case "HMAC":
                    keyLoader = new SecretKeyLoader(secret);
                    break;
                case "KEYSTORE":
                    keyLoader = new KeystoreKeyLoader(
                            algorithmName, keystorePath, keystorePassword, keyAlias, refreshInterval);
                    break;
                case "JWKS":
                    JwksKeyLoader jwksLoader = new JwksKeyLoader(
                            algorithmName, jwkSetUri,
                            maxRetries, refreshInterval, fallbackToCache);
                    if (restTemplate != null) {
                        jwksLoader.setRestTemplate(restTemplate);
                    }
                    keyLoader = jwksLoader;
                    break;
                default:
                    throw new IllegalStateException("必须指定密钥类型：hmac()、keystore()或jwks()");
            }

            return new JwtKeyManager(keyLoader);
        }
    }
}
