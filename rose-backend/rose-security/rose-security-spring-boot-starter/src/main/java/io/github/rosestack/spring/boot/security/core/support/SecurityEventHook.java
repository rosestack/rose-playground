package io.github.rosestack.spring.boot.security.core.support;

public interface SecurityEventHook {
    default void onLockOut(String username) {}
    // 可扩展：onSuspiciousActivity, onPasswordChanged 等
}
