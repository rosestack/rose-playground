package io.github.rosestack.spring.boot.security.auth.service.impl;

import io.github.rosestack.spring.boot.security.auth.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.auth.service.TokenService;
import io.github.rosestack.spring.boot.security.extension.AuthenticationHook;
import io.github.rosestack.spring.boot.security.properties.RoseSecurityProperties;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 内存 Token 服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class MemoryTokenService implements TokenService {

    private final RoseSecurityProperties properties;
    private final AuthenticationHook authenticationHook;

    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    private final Map<String, Integer> userTokenCount = new ConcurrentHashMap<>();

    @Override
    public TokenInfo createToken(UserDetails userDetails) {
        // 并发控制
        int max = properties.getAuth().getToken().getMaximumSessions();
        int count = userTokenCount.getOrDefault(userDetails.getUsername(), 0);
        if (count >= max && properties.getAuth().getToken().isMaxSessionsPreventsLogin()) {
            throw new IllegalStateException("超过最大并发会话数");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(properties.getAuth().getToken().getExpiration());

        TokenInfo info = TokenInfo.builder()
                .accessToken(token)
                .refreshToken(UUID.randomUUID().toString())
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(expiresAt)
                .username(userDetails.getUsername())
                .createdAt(now)
                .build();

        tokens.put(token, info);
        userTokenCount.merge(userDetails.getUsername(), 1, Integer::sum);
        log.debug("创建 token: {} for user: {}", token, userDetails.getUsername());
        return info;
    }

    @Override
    public boolean validateToken(String token) {
        TokenInfo info = tokens.get(token);
        return info != null && !info.isExpired();
    }

    @Override
    public Optional<UserDetails> getUserDetails(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null || info.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(User.withUsername(info.getUsername())
                .password("")
                .authorities("ROLE_USER")
                .build());
    }

    @Override
    public Optional<TokenInfo> refreshToken(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null) return Optional.empty();
        if (LocalDateTime.now()
                .isAfter(
                        info.getCreatedAt().plus(properties.getAuth().getToken().getRefreshWindow()))) {
            return Optional.empty();
        }
        String newToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        TokenInfo newInfo = TokenInfo.builder()
                .accessToken(newToken)
                .refreshToken(UUID.randomUUID().toString())
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(now.plus(properties.getAuth().getToken().getExpiration()))
                .username(info.getUsername())
                .createdAt(now)
                .build();
        tokens.put(newToken, newInfo);
        tokens.remove(token);
        return Optional.of(newInfo);
    }

    @Override
    public void revokeToken(String token) {
        TokenInfo info = tokens.remove(token);
        if (info != null) {
            userTokenCount.merge(info.getUsername(), -1, Integer::sum);
            authenticationHook.onTokenRevoked(token);
        }
    }

    @Override
    public void revokeAllTokens(String username) {
        tokens.entrySet().removeIf(e -> {
            boolean match = username.equals(e.getValue().getUsername());
            if (match) {
                userTokenCount.merge(username, -1, Integer::sum);
            }
            return match;
        });
        authenticationHook.onRevoked(username);
    }

    @Override
    public int getActiveTokenCount(String username) {
        return Math.max(0, userTokenCount.getOrDefault(username, 0));
    }
}
