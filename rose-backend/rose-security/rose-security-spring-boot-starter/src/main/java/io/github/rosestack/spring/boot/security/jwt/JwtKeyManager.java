package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
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
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * JWT 密钥管理：支持 HS/RS/ES 与 JWK/Keystore
 * 说明：
 * - HS：本地 secret 签名/验签
 * - Keystore：本地 RSA/EC 私钥签名，证书公钥验签
 * - JWK：从远端 JWKS 获取公钥验签（按 kid/alg 匹配）
 */
@Slf4j
@RequiredArgsConstructor
public class JwtKeyManager {
    // JWKS 缓存
    private volatile Instant jwksFetchedAt;
    private volatile JWKSet cachedJwkSet;
    // Keystore 缓存
    private volatile Instant keystoreLoadedAt;
    private volatile PrivateKey cachedPrivateKey;
    private volatile PublicKey cachedPublicKey;
    // 可注入的 RestTemplate；若未设置，则使用默认的 SimpleClientHttpRequestFactory 构建
    private volatile RestTemplate restTemplate;

    private final RoseSecurityProperties properties;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private RestTemplate getRestTemplate() {
        RestTemplate rt = this.restTemplate;
        if (rt == null) {
            SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
            f.setConnectTimeout(properties.getJwt().getKey().getJwkConnectTimeoutMillis());
            f.setReadTimeout(properties.getJwt().getKey().getJwkReadTimeoutMillis());
            rt = new RestTemplate(f);
            this.restTemplate = rt;
        }
        return rt;
    }

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
        try {
            if (properties.getJwt().getKey().getType() == RoseSecurityProperties.Jwt.Key.KeyType.JWK) {
                // 返回当前 JWKS 中匹配算法的第一个 verifier（用于非指定 token 验证场景）
                JWKSet jwks = loadJwks();
                Object v = selectVerifierByAlg(jwks);
                if (isLoggingEnabled()) {
                    log.debug(
                            "Selected JWK verifier alg={}, keys={}",
                            algorithm().getName(),
                            jwks.getKeys().size());
                }
                return v;
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
        } catch (Exception ex) {
            if (isLoggingEnabled()) {
                log.warn("Create default verifier failed: {}", ex.getMessage());
            }
            if (properties.getJwt().isFallbackToSecretForVerify()) {
                return new MACVerifier(properties.getJwt().getSecret().getBytes());
            }
            throw ex;
        }
    }

    private PrivateKey loadPrivateKeyFromKeystore() throws Exception {
        RoseSecurityProperties.Jwt.Key key = properties.getJwt().getKey();
        String path = key.getKeystorePath();
        if (path == null) {
            throw new IllegalStateException("Keystore path not configured");
        }
        if (needReload(keystoreLoadedAt) || cachedPrivateKey == null) {
            KeyStore ks = tryLoadKeyStore(path, key.getKeystorePassword());
            String alias = key.getKeyAlias();
            if (alias == null) {
                throw new IllegalStateException("Key alias not configured");
            }
            cachedPrivateKey = (PrivateKey) ks.getKey(
                    alias,
                    key.getKeystorePassword() != null
                            ? key.getKeystorePassword().toCharArray()
                            : null);
            Certificate cert = ks.getCertificate(alias);
            if (cert != null) {
                cachedPublicKey = cert.getPublicKey();
            }
            keystoreLoadedAt = Instant.now();
        }
        return cachedPrivateKey;
    }

    private boolean isLoggingEnabled() {
        return properties.getObservability() == null
                || properties.getObservability().isStructuredLoggingEnabled();
    }

    private Object selectVerifierByAlg(JWKSet jwks) throws Exception {
        for (JWK k : jwks.getKeys()) {
            if (k.getAlgorithm() != null
                    && k.getAlgorithm().getName().equals(algorithm().getName())) {
                return toVerifier(k);
            }
        }
        // 无匹配则回退第一个可用
        if (!jwks.getKeys().isEmpty()) {
            return toVerifier(jwks.getKeys().get(0));
        }
        throw new IllegalStateException("JWKS 中未找到可用的 verifier");
    }

    private Object toVerifier(JWK k) throws Exception {
        if (k instanceof RSAKey) {
            return new RSASSAVerifier(((RSAKey) k).toRSAPublicKey());
        } else if (k instanceof ECKey) {
            return new ECDSAVerifier(((ECKey) k).toECPublicKey());
        } else if (k instanceof OctetSequenceKey) {
            return new MACVerifier(((OctetSequenceKey) k).toByteArray());
        }
        throw new IllegalStateException("不支持的 JWK 类型: " + k.getClass().getSimpleName());
    }

    private PublicKey loadCertificatePublicKey() throws Exception {
        RoseSecurityProperties.Jwt.Key key = properties.getJwt().getKey();
        String path = key.getKeystorePath();
        if (path == null) {
            throw new IllegalStateException("Keystore path not configured");
        }
        if (needReload(keystoreLoadedAt) || cachedPublicKey == null) {
            KeyStore ks = tryLoadKeyStore(path, key.getKeystorePassword());
            String alias = key.getKeyAlias();
            Certificate cert = ks.getCertificate(alias);
            if (cert == null) {
                throw new IllegalStateException("Certificate not found for alias: " + alias);
            }
            cachedPublicKey = cert.getPublicKey();
            keystoreLoadedAt = Instant.now();
        }
        return cachedPublicKey;
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
            is = new FileInputStream(location.substring("file:".length()));
        } else {
            is = new FileInputStream(location);
        }
        ks.load(is, password != null ? password.toCharArray() : null);
        return ks;
    }

    private JWKSet loadJwks() throws Exception {
        String uri = properties.getJwt().getKey().getJwkSetUri();
        if (uri == null || uri.isEmpty()) {
            throw new IllegalStateException("JWK Set URI 未配置");
        }
        if (cachedJwkSet == null || needReload(jwksFetchedAt)) {
            cachedJwkSet = fetchJwksWithRetry();
            jwksFetchedAt = Instant.now();
        }
        return cachedJwkSet;
    }

    private JWKSet fetchJwksWithRetry() throws Exception {
        String uri = properties.getJwt().getKey().getJwkSetUri();
        int max = Math.max(1, properties.getJwt().getKey().getJwkMaxRetries());
        int attempt = 0;
        Exception last = null;
        while (attempt < max) {
            attempt++;
            try {
                String json = getRestTemplate().getForObject(uri, String.class);
                if (json == null || json.isEmpty()) {
                    throw new IllegalStateException("Empty JWKS response");
                }
                JWKSet set = JWKSet.parse(json);
                return set;
            } catch (Exception ex) {
                last = ex;
                Thread.sleep(Math.min(500L * attempt, 1500L));
            }
        }
        if (properties.getJwt().getKey().isJwkFallbackToCache() && cachedJwkSet != null && !needReload(jwksFetchedAt)) {
            return cachedJwkSet;
        }
        if (last != null) {
            if (isLoggingEnabled()) {
                log.warn("Fetch JWKS failed after {} attempts: {}", attempt, last.getMessage());
            }
            throw last;
        }
        throw new IllegalStateException("JWKS 获取失败且无可用缓存");
    }

    private boolean needReload(Instant lastLoaded) {
        Duration interval = properties.getJwt().getKey().getRotationInterval();
        if (lastLoaded == null) return true;
        return Instant.now().isAfter(lastLoaded.plus(interval));
    }

    // 基于 JWKS 的 verifier 选择：优先 kid，其次按算法匹配
    public Object getVerifierFor(SignedJWT jwt) throws Exception {
        if (properties.getJwt().getKey().getType() != RoseSecurityProperties.Jwt.Key.KeyType.JWK) {
            return verifier();
        }
        JWKSet jwks = loadJwks();

        String kid = jwt.getHeader().getKeyID();
        JWK match = kid != null ? jwks.getKeyByKeyId(kid) : null;
        if (match == null) {
            for (JWK k : jwks.getKeys()) {
                if (k.getAlgorithm() != null
                        && k.getAlgorithm().getName().equals(algorithm().getName())) {
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
