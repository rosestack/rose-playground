package io.github.rosestack.spring.boot.security.core.listener;

import io.github.rosestack.spring.boot.security.core.event.LoginFailureEvent;
import io.github.rosestack.spring.boot.security.core.event.LoginSuccessEvent;
import io.github.rosestack.spring.boot.security.core.event.LogoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class SecurityEventListener {
    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);

    @EventListener
    public void onLoginSuccess(LoginSuccessEvent e) {
        log.info("Login success: {}", e.getUsername());
    }

    @EventListener
    public void onLoginFailure(LoginFailureEvent e) {
        log.warn("Login failure: {}, reason: {}", e.getUsername(), e.getReason());
    }

    @EventListener
    public void onLogout(LogoutEvent e) {
        log.info("Logout: {}", e.getUsername());
    }
}


