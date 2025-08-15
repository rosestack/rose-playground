package io.github.rosestack.spring.boot.security.core.event;

import java.time.Instant;

public class TokenIssuedEvent {
    private final String username;
    private final String token;
    private final Instant issuedAt;

    public TokenIssuedEvent(String username, String token) {
        this.username = username;
        this.token = token;
        this.issuedAt = Instant.now();
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}


