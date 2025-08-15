package io.github.rosestack.spring.boot.security.core.event;

import lombok.Getter;

@Getter
public class UserUnlockedEvent {
    private final String username;
    private final boolean manual;

    public UserUnlockedEvent(String username, boolean manual) {
        this.username = username;
        this.manual = manual;
    }
}


