package io.github.rosestack.spring.boot.security.jwt.loader;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.rosestack.spring.boot.security.jwt.algorithm.JwtAlgorithmFactory;
import io.github.rosestack.spring.boot.security.jwt.cache.KeyCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * JWKS密钥加载器
 *
 * <p>特点：
 * <ul>
 *   <li>从远程JWKS端点获取公钥</li>
 *   <li>支持多密钥和密钥轮换</li>
 *   <li>优先按kid匹配，其次按算法匹配</li>
 *   <li>仅支持验证操作，不支持签名</li>
 *   <li>支持缓存和回退策略</li>
 * </ul>
 * </p>
 */
@Slf4j
public class JwksKeyLoader implements KeyLoader {

    private final String algorithmName;
    private final String jwkSetUri;
    private final int maxRetries;
    private final KeyCache<JWKSet> jwksCache;
    private volatile RestTemplate restTemplate;

    /**
     * 构造JWKS密钥加载器
     *
     * @param algorithmName   算法名称，如"RS256"、"ES256"
     * @param jwkSetUri       JWKS端点URI
     * @param maxRetries      最大重试次数
     * @param refreshInterval 缓存刷新间隔
     * @param fallbackToCache 是否回退到缓存
     */
    public JwksKeyLoader(String algorithmName, String jwkSetUri,
                         int maxRetries,
                         Duration refreshInterval, boolean fallbackToCache) {
        this.algorithmName = algorithmName;
        this.jwkSetUri = jwkSetUri;
        this.maxRetries = maxRetries;
        this.jwksCache = new KeyCache<>("JWKS", refreshInterval, fallbackToCache);
    }

    /**
     * 设置自定义RestTemplate
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public JWSSigner createSigner() throws Exception {
        throw new UnsupportedOperationException("JWKS密钥加载器不支持签名操作");
    }

    @Override
    public JWSVerifier createVerifier() throws Exception {
        JWKSet jwkSet = loadJwkSet();
        JWK jwk = selectJwkByAlgorithm(jwkSet);

        if (jwk == null) {
            throw new IllegalStateException("JWKS中未找到匹配的密钥，算法: " + algorithmName);
        }

        return JwtAlgorithmFactory.createVerifierFromJwk(jwk);
    }

    @Override
    public JWSVerifier createVerifierFor(SignedJWT jwt) throws Exception {
        JWKSet jwkSet = loadJwkSet();

        // 优先根据kid查找
        String kid = jwt.getHeader().getKeyID();
        JWK jwk = null;

        if (kid != null) {
            jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk != null) {
                log.debug("根据kid找到匹配密钥: {}", kid);
            }
        }

        // 如果没找到，按算法匹配
        if (jwk == null) {
            jwk = selectJwkByAlgorithm(jwkSet);
            if (jwk != null) {
                log.debug("根据算法找到匹配密钥: {}", algorithmName);
            }
        }

        // 最后尝试使用第一个可用密钥
        if (jwk == null && !jwkSet.getKeys().isEmpty()) {
            jwk = jwkSet.getKeys().get(0);
            log.debug("使用第一个可用密钥，类型: {}", jwk.getKeyType());
        }

        if (jwk == null) {
            throw new IllegalStateException("JWKS中未找到可用的验证密钥");
        }

        return JwtAlgorithmFactory.createVerifierFromJwk(jwk);
    }

    @Override
    public boolean supportsSigning() {
        return false;
    }

    @Override
    public String getType() {
        return "JWKS";
    }

    /**
     * 加载JWKS（使用缓存）
     */
    private JWKSet loadJwkSet() throws Exception {
        return jwksCache.get(() -> {
            try {
                return fetchJwkSetWithRetry();
            } catch (Exception e) {
                throw new RuntimeException("获取JWKS失败", e);
            }
        });
    }

    /**
     * 带重试机制的JWKS获取
     */
    private JWKSet fetchJwkSetWithRetry() throws Exception {
        if (jwkSetUri == null || jwkSetUri.trim().isEmpty()) {
            throw new IllegalStateException("JWKS URI未配置");
        }

        int maxRetriesValue = Math.max(1, maxRetries);
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetriesValue; attempt++) {
            try {
                log.debug("获取JWKS，尝试第{}次: {}", attempt, jwkSetUri);

                String jsonResponse = getRestTemplate().getForObject(jwkSetUri, String.class);
                if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                    throw new IllegalStateException("JWKS响应为空");
                }

                JWKSet jwkSet = JWKSet.parse(jsonResponse);
                log.info("JWKS获取成功，密钥数量: {}", jwkSet.getKeys().size());
                return jwkSet;

            } catch (Exception e) {
                lastException = e;
                log.warn("JWKS获取失败，尝试第{}次: {}", attempt, e.getMessage());

                // 最后一次尝试时不再等待
                if (attempt < maxRetriesValue) {
                    try {
                        long delay = Math.min(500L * attempt, 1500L);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("JWKS获取被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("JWKS获取失败，已重试" + maxRetriesValue + "次", lastException);
    }

    /**
     * 根据算法选择JWK
     */
    private JWK selectJwkByAlgorithm(JWKSet jwkSet) {
        String algorithmNameForJwk = JwtAlgorithmFactory.getJwsAlgorithm(algorithmName).getName();

        for (JWK jwk : jwkSet.getKeys()) {
            if (jwk.getAlgorithm() != null &&
                    jwk.getAlgorithm().getName().equals(algorithmNameForJwk)) {
                return jwk;
            }
        }

        return null;
    }

    /**
     * 获取RestTemplate（懒加载）
     */
    private RestTemplate getRestTemplate() {
        RestTemplate rt = this.restTemplate;
        if (rt == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            rt = new RestTemplate(factory);
            this.restTemplate = rt;
        }
        return rt;
    }
}
