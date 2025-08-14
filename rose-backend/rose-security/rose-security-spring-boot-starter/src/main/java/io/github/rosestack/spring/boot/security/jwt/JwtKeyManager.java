package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import io.github.rosestack.spring.boot.security.properties.RoseSecurityProperties;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * JWT 密钥管理：支持 HS/RS/ES 与 JWK/Keystore
 * 最小实现：
 * - HS：直接使用 secret
 * - JWK：远程获取（不缓存）
 * - Keystore：加载私钥用于签名（RSA/EC），公钥校验可由解析端完成
 */
@RequiredArgsConstructor
public class JwtKeyManager {

    private final RoseSecurityProperties properties;

    public JWSAlgorithm algorithm() {
        switch (properties.getJwt().getAlgorithm()) {
            case RS256:
                return JWSAlgorithm.RS256;
            case RS384:
                return JWSAlgorithm.RS384;
            case RS512:
                return JWSAlgorithm.RS512;
            case ES256:
                return JWSAlgorithm.ES256;
            case ES384:
                return JWSAlgorithm.ES384;
            case ES512:
                return JWSAlgorithm.ES512;
            case HS384:
                return JWSAlgorithm.HS384;
            case HS512:
                return JWSAlgorithm.HS512;
            case HS256:
            default:
                return JWSAlgorithm.HS256;
        }
    }

    public Object signer() throws Exception {
        switch (properties.getJwt().getAlgorithm()) {
            case RS256:
            case RS384:
            case RS512:
                RSAPrivateKey rsa = (RSAPrivateKey) loadPrivateKeyFromKeystore();
                return new RSASSASigner(rsa);
            case ES256:
            case ES384:
            case ES512:
                ECPrivateKey ec = (ECPrivateKey) loadPrivateKeyFromKeystore();
                return new ECDSASigner(ec);
            case HS384:
            case HS512:
            case HS256:
            default:
                return new MACSigner(properties.getJwt().getSecret().getBytes());
        }
    }

    public Object verifier() throws Exception {
        switch (properties.getJwt().getAlgorithm()) {
            case RS256:
            case RS384:
            case RS512: {
                // 解析端通常用公钥，这里简化：若 keystore 中包含证书可取公钥
                PublicKey pub = loadCertificatePublicKey();
                return new RSASSAVerifier((RSAPublicKey) pub);
            }
            case ES256:
            case ES384:
            case ES512: {
                java.security.PublicKey pub = loadCertificatePublicKey();
                return new com.nimbusds.jose.crypto.ECDSAVerifier((java.security.interfaces.ECPublicKey) pub);
            }
            case HS384:
            case HS512:
            case HS256:
            default:
                return new MACVerifier(properties.getJwt().getSecret().getBytes());
        }
    }

    private PrivateKey loadPrivateKeyFromKeystore() throws Exception {
        RoseSecurityProperties.Jwt.Key key = properties.getJwt().getKey();
        String path = key.getKeystorePath();
        if (path == null) throw new IllegalStateException("Keystore path not configured");
        KeyStore ks = tryLoadKeyStore(path, key.getKeystorePassword());
        String alias = key.getKeyAlias();
        if (alias == null) throw new IllegalStateException("Key alias not configured");
        return (PrivateKey) ks.getKey(alias, key.getKeystorePassword() != null ? key.getKeystorePassword().toCharArray() : null);
    }

    private java.security.PublicKey loadCertificatePublicKey() throws Exception {
        RoseSecurityProperties.Jwt.Key key = properties.getJwt().getKey();
        String path = key.getKeystorePath();
        if (path == null) throw new IllegalStateException("Keystore path not configured");
        KeyStore ks = tryLoadKeyStore(path, key.getKeystorePassword());
        String alias = key.getKeyAlias();
        java.security.cert.Certificate cert = ks.getCertificate(alias);
        if (cert == null) throw new IllegalStateException("Certificate not found for alias: " + alias);
        return cert.getPublicKey();
    }

    private KeyStore tryLoadKeyStore(String location, String password) throws Exception {
        String lower = location.toLowerCase();
        String type = lower.endsWith(".p12") || lower.endsWith(".pfx") ? "PKCS12" : "JKS";
        KeyStore ks = KeyStore.getInstance(type);
        InputStream is;
        if (lower.startsWith("classpath:")) {
            String p = location.substring("classpath:".length());
            is = this.getClass().getResourceAsStream(p.startsWith("/") ? p : "/" + p);
        } else if (lower.startsWith("file:")) {
            is = new java.io.FileInputStream(location.substring("file:".length()));
        } else {
            is = new java.io.FileInputStream(location);
        }
        ks.load(is, password != null ? password.toCharArray() : null);
        return ks;
    }
}

