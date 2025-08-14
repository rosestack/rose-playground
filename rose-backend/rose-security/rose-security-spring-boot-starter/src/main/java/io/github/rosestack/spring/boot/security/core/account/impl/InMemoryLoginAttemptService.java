package io.github.rosestack.spring.boot.security.core.account.impl;

import io.github.rosestack.spring.boot.security.core.account.LoginAttemptService;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

/**
 * 基于内存的登录失败计数与锁定实现
 */
@RequiredArgsConstructor
public class InMemoryLoginAttemptService implements LoginAttemptService {

    private final RoseSecurityProperties properties;

    private static class State {
        int failures;
        Instant lockedUntil;
    }

    private final Map<String, State> states = new ConcurrentHashMap<>();

    @Override
    public boolean isLocked(String username) {
        if (!properties.getAuth().getAccount().getLockOut().isEnabled()) {
            return false;
        }
        State s = states.get(username);
        if (s == null || s.lockedUntil == null) return false;
        if (Instant.now().isAfter(s.lockedUntil)) {
            // 自动解锁
            s.lockedUntil = null;
            s.failures = 0;
            return false;
        }
        return true;
    }

    @Override
    public void recordSuccess(String username) {
        states.remove(username);
    }

    @Override
    public void recordFailure(String username) {
        RoseSecurityProperties.Auth.Account.LockOut cfg =
                properties.getAuth().getAccount().getLockOut();
        if (!cfg.isEnabled()) return;
        State s = states.computeIfAbsent(username, k -> new State());
        s.failures++;
        if (s.failures >= cfg.getMaxAttempts()) {
            s.lockedUntil = Instant.now().plus(cfg.getLockDuration());
        }
    }

    @Override
    public Duration getRemainingLockDuration(String username) {
        State s = states.get(username);
        if (s == null || s.lockedUntil == null) return Duration.ZERO;
        Duration d = Duration.between(Instant.now(), s.lockedUntil);
        return d.isNegative() ? Duration.ZERO : d;
    }

    @Override
    public int getFailureCount(String username) {
        State s = states.get(username);
        return s == null ? 0 : s.failures;
    }
}
