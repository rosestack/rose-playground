package io.github.rosestack.spring.boot.security.jwt;

/**
 * Token 撤销/黑名单 SPI
 */
public interface TokenRevocationStore {
    void revoke(String accessToken);

    boolean isRevoked(String accessToken);
}
