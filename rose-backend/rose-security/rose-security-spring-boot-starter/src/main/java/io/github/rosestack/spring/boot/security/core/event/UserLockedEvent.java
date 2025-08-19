package io.github.rosestack.spring.boot.security.core.event;

import lombok.Getter;

@Getter
public class UserLockedEvent {
    private final String username;
    private final boolean manual;

    public UserLockedEvent(String username, boolean manual) {
        this.username = username;
        this.manual = manual;
    }
}
