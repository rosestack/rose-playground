package io.github.rosestack.spring.boot.security.core.token;

import java.util.Optional;

public interface TokenService {
    String issue(String username);

    Optional<String> resolveUsername(String token);

    boolean revoke(String token);

    long getExpiresInSeconds();
}


