package io.github.rosestack.spring.boot.security.account;

import io.github.rosestack.spring.boot.security.config.SecurityProperties;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginLockoutService {

    private final SecurityProperties.Account.LoginLock props;
    private final Map<String, State> stateByUser = new ConcurrentHashMap<>();

    public LoginLockoutService(SecurityProperties properties) {
        this.props = properties.getAccount().getLoginLock();
    }

    public boolean isEnabled() {
        return props.isEnabled();
    }

    public boolean isLocked(String username) {
        State s = stateByUser.get(username);
        if (s == null || s.lockUntil == null) return false;
        if (Instant.now().isAfter(s.lockUntil)) {
            // auto unlock
            s.lockUntil = null;
            s.failures = 0;
            return false;
        }
        return true;
    }

    public void onFailure(String username) {
        if (!isEnabled()) {
            return;
        }
        State s = stateByUser.computeIfAbsent(username, k -> new State());
        if (s.lockUntil != null && Instant.now().isBefore(s.lockUntil)) {
            return; // still locked
        }
        s.failures++;
        if (s.failures >= props.getMaxFailures()) {
            s.lockUntil = Instant.now().plus(props.getCooldown());
            s.failures = 0;
        }
    }

    public void onSuccess(String username) {
        if (!isEnabled()) {
            return;
        }
        State s = stateByUser.get(username);
        if (s != null) {
            s.failures = 0;
            s.lockUntil = null;
        }
    }

    private static final class State {
        int failures;
        Instant lockUntil; // null if not locked
    }
}
