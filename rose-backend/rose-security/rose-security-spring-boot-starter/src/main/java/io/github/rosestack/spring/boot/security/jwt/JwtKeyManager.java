package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * JWT 密钥管理：支持 HS/RS/ES 与 JWK/Keystore
 * 说明：
 * - HS：本地 secret 签名/验签
 * - Keystore：本地 RSA/EC 私钥签名，证书公钥验签
 * - JWK：从远端 JWKS 获取公钥验签（按 kid/alg 匹配）
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
        // 如果配置为 JWK，优先使用 JWKS 公钥
        if (properties.getJwt().getKey().getType() == RoseSecurityProperties.Jwt.Key.KeyType.JWK) {
            return null; // 调用方（JwtTokenService）将通过 getVerifierFor(SignedJWT) 选择具体 verifier
        }
        switch (properties.getJwt().getAlgorithm()) {
            case RS256:
            case RS384:
            case RS512: {
                PublicKey pub = loadCertificatePublicKey();
                return new RSASSAVerifier((RSAPublicKey) pub);
            }
            case ES256:
            case ES384:
            case ES512: {
                PublicKey pub = loadCertificatePublicKey();
                return new ECDSAVerifier((ECPublicKey) pub);
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
        return (PrivateKey) ks.getKey(
                alias,
                key.getKeystorePassword() != null ? key.getKeystorePassword().toCharArray() : null);
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

    // 基于 JWKS 的 verifier 选择：优先 kid，其次按算法匹配
    public Object getVerifierFor(SignedJWT jwt) throws Exception {
        if (properties.getJwt().getKey().getType() != RoseSecurityProperties.Jwt.Key.KeyType.JWK) {
            return verifier();
        }
        String uri = properties.getJwt().getKey().getJwkSetUri();
        if (uri == null || uri.isEmpty()) {
            throw new IllegalStateException("JWK Set URI 未配置");
        }
        JWKSet jwks = JWKSet.load(new java.net.URL(uri));

        String kid = jwt.getHeader().getKeyID();
        JWK match = kid != null ? jwks.getKeyByKeyId(kid) : null;
        if (match == null) {
            for (JWK k : jwks.getKeys()) {
                if (k.getAlgorithm() != null && k.getAlgorithm().getName().equals(algorithm().getName())) {
                    match = k;
                    break;
                }
            }
        }
        if (match == null && !jwks.getKeys().isEmpty()) {
            match = jwks.getKeys().get(0);
        }
        if (match instanceof RSAKey) {
            return new RSASSAVerifier(((RSAKey) match).toRSAPublicKey());
        } else if (match instanceof ECKey) {
            return new ECDSAVerifier(((ECKey) match).toECPublicKey());
        } else if (match instanceof OctetSequenceKey) {
            return new MACVerifier(((OctetSequenceKey) match).toByteArray());
        }
        throw new IllegalStateException("未在 JWKS 中找到可用的 verifier");
    }

}
