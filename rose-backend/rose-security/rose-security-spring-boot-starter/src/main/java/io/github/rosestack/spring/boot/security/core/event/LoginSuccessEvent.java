package io.github.rosestack.spring.boot.security.core.event;

import lombok.Getter;

@Getter
public class LoginSuccessEvent {
    private final String username;

    public LoginSuccessEvent(String username) {
        this.username = username;
    }

}


