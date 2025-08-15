package io.github.rosestack.spring.boot.security.jwt;

public interface TokenManagementHook {
    default void onTokenExpired(String token) {}

    default void onTokenRevoked(String username, String token) {}

    default boolean beforeTokenRefresh(String refreshToken) {
        return true;
    }

    default void onTokenRefreshSuccess(String username, String newAccessToken) {}
}
