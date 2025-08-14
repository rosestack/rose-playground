package io.github.rosestack.spring.boot.security.core.service.impl;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.domain.UserTokenInfo;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存 Token 服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class MemoryTokenService implements TokenService {

    private final RoseSecurityProperties properties;
    private final AuthenticationHook authenticationHook;

    /**
     * refreshToken -> accessToken 映射，便于通过 refreshToken 刷新
     */
    private final Map<String, UserTokenInfo> refreshIndex = new ConcurrentHashMap<>();

    private final Map<String, LocalDateTime> refreshExpiry = new ConcurrentHashMap<>();

    private final Map<String, List<UserTokenInfo>> usernameToAccessTokensMap = new ConcurrentHashMap<>();

    private final Map<String, String> accessTokenToRefreshTokenMap = new ConcurrentHashMap<>();

    @Override
    public UserTokenInfo createToken(UserDetails userDetails) {
        // 并发控制
        int max = properties.getAuth().getToken().getMaximumSessions();
        List<UserTokenInfo> userTokenInfos =
                usernameToAccessTokensMap.getOrDefault(userDetails.getUsername(), new ArrayList<>());
        if (userTokenInfos.size() >= max && properties.getAuth().getToken().isMaxSessionsPreventsLogin()) {
            throw new IllegalStateException("超过最大并发会话数");
        }

        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        log.debug("创建 accessToken: {} for user: {}", accessToken, userDetails.getUsername());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiresAt = now.plus(properties.getAuth().getToken().getAccessTokenExpiredTime());
        LocalDateTime refreshExpiresAt =
                now.plus(properties.getAuth().getToken().getRefreshTokenExpiredTime());

        TokenInfo tokenInfo = TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(accessExpiresAt)
                .build();

        UserTokenInfo userTokenInfo = UserTokenInfo.builder()
                .tokenInfo(tokenInfo).username(userDetails.getUsername()).build();

        refreshIndex.put(refreshToken, userTokenInfo);
        refreshExpiry.put(refreshToken, refreshExpiresAt);
        usernameToAccessTokensMap.computeIfAbsent(userDetails.getUsername(), k -> new ArrayList<>()).add(userTokenInfo);
        accessTokenToRefreshTokenMap.put(accessToken, refreshToken);
        return userTokenInfo;
    }

    @Override
    public boolean validateToken(String accessToken) {
        String refreshToken = accessTokenToRefreshTokenMap.get(accessToken);
        if (refreshToken == null) {
            return false;
        }
        UserTokenInfo userTokenInfo = refreshIndex.get(refreshToken);

        return userTokenInfo != null && !userTokenInfo.getTokenInfo().isExpired();
    }

    @Override
    public UserDetails getUserDetails(String accessToken) {
        String refreshToken = accessTokenToRefreshTokenMap.get(accessToken);
        if (refreshToken == null) {
            return null;
        }

        UserTokenInfo userTokenInfo = refreshIndex.get(refreshToken);
        if (userTokenInfo == null) {
            return null;
        }

        return User.withUsername(userTokenInfo.getUsername())
                .password("")
                .authorities("ROLE_USER")
                .build();
    }

    @Override
    public UserTokenInfo refreshAccessToken(String refreshToken) {
        UserTokenInfo userTokenInfo = refreshIndex.get(refreshToken);
        if (userTokenInfo == null) {
            throw new IllegalArgumentException("refreshToken 不存在");
        }

        boolean refreshTokenExpired =
                refreshExpiry.getOrDefault(refreshToken, LocalDateTime.now()).isAfter(LocalDateTime.now());
        if (refreshTokenExpired) {
            throw new IllegalArgumentException("refreshToken 已过期");
        }

        LocalDateTime accessTokenExpireTime = userTokenInfo.getTokenInfo().getExpiresAt();

        LocalDateTime now = LocalDateTime.now();
        // 仅当 accessToken 临近过期（在窗口内）或已过期时允许刷新
        LocalDateTime nearThreshold = accessTokenExpireTime.minusMinutes(2);
        if (now.isBefore(nearThreshold)) {
            return userTokenInfo;
        }

        String newAccessToken = UUID.randomUUID().toString();
        String newRefreshToken = UUID.randomUUID().toString();
        LocalDateTime newAccessExpiresAt =
                now.plus(properties.getAuth().getToken().getAccessTokenExpiredTime());
        LocalDateTime newRefreshExpiresAt =
                now.plus(properties.getAuth().getToken().getRefreshTokenExpiredTime());

        // 更新 refresh 映射与过期
        refreshIndex.remove(refreshToken);
        refreshExpiry.remove(refreshToken);

        refreshIndex.put(refreshToken, userTokenInfo);
        refreshExpiry.put(refreshToken, newRefreshExpiresAt);
        usernameToAccessTokensMap.computeIfAbsent(userTokenInfo.getUsername(), k -> new ArrayList<>()).add(userTokenInfo);
        accessTokenToRefreshTokenMap.put(newAccessToken, refreshToken);

        TokenInfo tokenInfo = TokenInfo.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(newAccessExpiresAt)
                .build();

        return UserTokenInfo.builder().tokenInfo(tokenInfo).username(userTokenInfo.getUsername()).build();
    }

    @Override
    public void revokeToken(String accessToken) {
        String refreshToken = accessTokenToRefreshTokenMap.get(accessToken);
        if (refreshToken != null) {
            UserTokenInfo userTokenInfo = refreshIndex.remove(refreshToken);
            usernameToAccessTokensMap.remove(userTokenInfo.getUsername());
            accessTokenToRefreshTokenMap.remove(refreshToken);
            refreshExpiry.remove(refreshToken);
        }
    }

    @Override
    public void revokeAllTokens(String username) {
        List<UserTokenInfo> userTokenInfos = usernameToAccessTokensMap.remove(username);

        if (userTokenInfos != null) {
            authenticationHook.onRevoked(username);

            userTokenInfos.forEach(e -> {
                String accessToken = e.getTokenInfo().getAccessToken();
                String refreshToken = accessTokenToRefreshTokenMap.remove(accessToken);
                refreshIndex.remove(refreshToken);
                refreshExpiry.remove(refreshToken);
            });
        }
    }

    @Override
    public int getActiveTokenCount(String username) {
        return Math.max(
                0,
                usernameToAccessTokensMap
                        .getOrDefault(username, new ArrayList<>())
                        .size());
    }
}
