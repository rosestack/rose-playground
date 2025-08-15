package io.github.rosestack.spring.boot.security.jwt.loader;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import io.github.rosestack.spring.boot.security.jwt.algorithm.JwtAlgorithmFactory;
import io.github.rosestack.spring.boot.security.jwt.cache.KeyCache;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Keystore密钥加载器
 *
 * <p>特点：
 * <ul>
 *   <li>支持本地密钥库文件（JKS、PKCS12）</li>
 *   <li>支持RSA和ECDSA算法</li>
 *   <li>私钥用于签名，公钥用于验证</li>
 *   <li>支持缓存和自动刷新</li>
 * </ul>
 * </p>
 */
@Slf4j
public class KeystoreKeyLoader implements KeyLoader {

    private final String algorithmName;
    private final String keystorePath;
    private final String keystorePassword;
    private final String keyAlias;
    private final KeyCache<KeyPair> keyCache;

    /**
     * 构造Keystore密钥加载器
     *
     * @param algorithmName 算法名称，如"RS256"、"ES256"
     * @param keystorePath 密钥库路径，支持classpath:、file:前缀
     * @param keystorePassword 密钥库密码
     * @param keyAlias 密钥别名
     * @param refreshInterval 缓存刷新间隔
     */
    public KeystoreKeyLoader(
            String algorithmName,
            String keystorePath,
            String keystorePassword,
            String keyAlias,
            Duration refreshInterval) {
        this.algorithmName = algorithmName;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keyAlias = keyAlias;
        this.keyCache = new KeyCache<>("Keystore", refreshInterval, false);
    }

    @Override
    public JWSSigner createSigner() throws Exception {
        KeyPair keyPair = loadKeyPair();
        PrivateKey privateKey = keyPair.privateKey;

        if (JwtAlgorithmFactory.isRsaAlgorithm(algorithmName)) {
            return JwtAlgorithmFactory.createRsaSigner((RSAPrivateKey) privateKey);
        } else if (JwtAlgorithmFactory.isEcAlgorithm(algorithmName)) {
            return JwtAlgorithmFactory.createEcSigner((ECPrivateKey) privateKey);
        }

        throw new IllegalStateException("Keystore不支持算法: " + algorithmName);
    }

    @Override
    public JWSVerifier createVerifier() throws Exception {
        KeyPair keyPair = loadKeyPair();
        PublicKey publicKey = keyPair.publicKey;

        if (JwtAlgorithmFactory.isRsaAlgorithm(algorithmName)) {
            return JwtAlgorithmFactory.createRsaVerifier((RSAPublicKey) publicKey);
        } else if (JwtAlgorithmFactory.isEcAlgorithm(algorithmName)) {
            return JwtAlgorithmFactory.createEcVerifier((ECPublicKey) publicKey);
        }

        throw new IllegalStateException("Keystore不支持算法: " + algorithmName);
    }

    @Override
    public boolean supportsSigning() {
        return true;
    }

    @Override
    public String getType() {
        return "Keystore";
    }

    /**
     * 加载密钥对（使用缓存）
     */
    private KeyPair loadKeyPair() throws Exception {
        return keyCache.get(() -> {
            try {
                return doLoadKeyPair();
            } catch (Exception e) {
                throw new RuntimeException("加载Keystore密钥失败", e);
            }
        });
    }

    /**
     * 实际加载密钥对的逻辑
     */
    private KeyPair doLoadKeyPair() throws Exception {
        if (keystorePath == null || keystorePath.trim().isEmpty()) {
            throw new IllegalStateException("Keystore路径未配置");
        }

        if (keyAlias == null || keyAlias.trim().isEmpty()) {
            throw new IllegalStateException("密钥别名未配置");
        }

        log.debug("加载Keystore: {}, 别名: {}", keystorePath, keyAlias);

        // 加载密钥库
        KeyStore keyStore = loadKeyStore(keystorePath, keystorePassword);

        // 加载私钥
        PrivateKey privateKey = (PrivateKey)
                keyStore.getKey(keyAlias, keystorePassword != null ? keystorePassword.toCharArray() : null);
        if (privateKey == null) {
            throw new IllegalStateException("未找到私钥，别名: " + keyAlias);
        }

        // 加载公钥（从证书中提取）
        Certificate certificate = keyStore.getCertificate(keyAlias);
        if (certificate == null) {
            throw new IllegalStateException("未找到证书，别名: " + keyAlias);
        }
        PublicKey publicKey = certificate.getPublicKey();

        log.info("Keystore密钥加载成功，算法: {}, 别名: {}", privateKey.getAlgorithm(), keyAlias);

        return new KeyPair(privateKey, publicKey);
    }

    /**
     * 加载密钥库文件
     */
    private KeyStore loadKeyStore(String location, String password) throws Exception {
        // 自动检测密钥库类型
        String lower = location.toLowerCase();
        String type = (lower.endsWith(".p12") || lower.endsWith(".pfx")) ? "PKCS12" : "JKS";

        KeyStore keyStore = KeyStore.getInstance(type);

        // 根据路径前缀确定资源位置
        InputStream inputStream;
        if (lower.startsWith("classpath:")) {
            String resourcePath = location.substring("classpath:".length());
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            inputStream = this.getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalStateException("Classpath资源未找到: " + location);
            }
        } else if (lower.startsWith("file:")) {
            inputStream = new FileInputStream(location.substring("file:".length()));
        } else {
            inputStream = new FileInputStream(location);
        }

        try {
            keyStore.load(inputStream, password != null ? password.toCharArray() : null);
        } finally {
            inputStream.close();
        }

        return keyStore;
    }

    /**
     * 密钥对封装
     */
    private static class KeyPair {
        final PrivateKey privateKey;
        final PublicKey publicKey;

        KeyPair(PrivateKey privateKey, PublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }
    }
}
