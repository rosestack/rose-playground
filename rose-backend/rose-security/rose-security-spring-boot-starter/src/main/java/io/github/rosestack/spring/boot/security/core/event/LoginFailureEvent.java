package io.github.rosestack.spring.boot.security.core.event;

import lombok.Getter;

@Getter
public class LoginFailureEvent {
    private final String username;
    private final String reason;

    public LoginFailureEvent(String username, String reason) {
        this.username = username;
        this.reason = reason;
    }
}
