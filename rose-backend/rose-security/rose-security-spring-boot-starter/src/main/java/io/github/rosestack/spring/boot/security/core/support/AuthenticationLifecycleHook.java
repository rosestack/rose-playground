package io.github.rosestack.spring.boot.security.core.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationLifecycleHook {
    default boolean beforeLogin(String username, String password) {
        return true;
    }

    default void onLoginSuccess(String username, Authentication authentication) {}

    default void onLoginFailure(String username, AuthenticationException exception) {}

    default boolean beforeLogout(String username) {
        return true;
    }

    default void onLogoutSuccess(String username) {}
}
