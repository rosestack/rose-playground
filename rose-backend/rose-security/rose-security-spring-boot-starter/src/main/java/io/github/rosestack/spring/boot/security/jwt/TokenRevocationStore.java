package io.github.rosestack.spring.boot.security.jwt;

/**
 * Token 撤销/黑名单 SPI
 */
public interface TokenRevocationStore {
    void revoke(String token);

    boolean isRevoked(String token);
}
