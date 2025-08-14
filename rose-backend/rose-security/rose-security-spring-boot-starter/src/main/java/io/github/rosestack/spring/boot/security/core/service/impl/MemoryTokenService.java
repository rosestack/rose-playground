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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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

    private final Map<String, Set<String>> usernameToAccessTokensMap = new ConcurrentHashMap<>();

    private final Map<String, String> accessTokenToRefreshTokenMap = new ConcurrentHashMap<>();

    @Override
    public UserTokenInfo createToken(UserDetails userDetails) {
        // 并发控制
        int max = properties.getAuth().getToken().getMaximumSessions();
        Set<String> userTokenInfos =
                usernameToAccessTokensMap.getOrDefault(userDetails.getUsername(), new TreeSet<>());
        if (userTokenInfos.size() >= max && properties.getAuth().getToken().isMaxSessionsPreventsLogin()) {
            throw new IllegalStateException("超过最大并发会话数");
        }

        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        log.debug("创建 accessToken: {} for user: {}", accessToken, userDetails.getUsername());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiresAt = now.plus(properties.getAuth().getToken().getAccessTokenExpiredTime());
        LocalDateTime refreshExpiresAt = now.plus(properties.getAuth().getToken().getRefreshTokenExpiredTime());

        TokenInfo tokenInfo = TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(accessExpiresAt)
                .build();

        UserTokenInfo userTokenInfo = UserTokenInfo.builder()
                .tokenInfo(tokenInfo)
                .username(userDetails.getUsername())
                .build();

        refreshIndex.put(refreshToken, userTokenInfo);
        refreshExpiry.put(refreshToken, refreshExpiresAt);
        usernameToAccessTokensMap
                .computeIfAbsent(userDetails.getUsername(), k -> new TreeSet<>())
                .add(accessToken);
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

        LocalDateTime refreshTokenExpiredTime = refreshExpiry.get(refreshToken);
        if (refreshTokenExpiredTime == null || LocalDateTime.now().isAfter(refreshTokenExpiredTime)) {
            throw new IllegalArgumentException("refreshToken 已过期");
        }

        LocalDateTime accessTokenExpireTime = userTokenInfo.getTokenInfo().getExpiresAt();

        LocalDateTime now = LocalDateTime.now();
        // 仅当 accessToken 临近过期（在窗口内）或已过期时允许刷新
        LocalDateTime near = accessTokenExpireTime.minus(properties.getAuth().getToken().getRefreshWindow());
        if (now.isBefore(near) && !userTokenInfo.getTokenInfo().isExpired()) {
            return userTokenInfo; // 太早，不刷新
        }

        String newAccessToken = UUID.randomUUID().toString();
        String newRefreshToken = UUID.randomUUID().toString();
        LocalDateTime newAccessExpiresAt =
                now.plus(properties.getAuth().getToken().getAccessTokenExpiredTime());
        LocalDateTime newRefreshExpiresAt =
                now.plus(properties.getAuth().getToken().getRefreshTokenExpiredTime());

        String oldAccess = userTokenInfo.getTokenInfo().getAccessToken();
        String oldRefresh = refreshToken;

        // 删除旧映射
        refreshIndex.remove(oldRefresh);
        refreshExpiry.remove(oldRefresh);
        accessTokenToRefreshTokenMap.remove(oldAccess);
        usernameToAccessTokensMap.get(userTokenInfo.getUsername()).remove(oldAccess);

        TokenInfo tokenInfo = TokenInfo.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(newAccessExpiresAt)
                .build();

        userTokenInfo.setTokenInfo(tokenInfo);

        // 生成新 token 并写新映射
        userTokenInfo.setTokenInfo(tokenInfo);
        refreshIndex.put(newRefreshToken, userTokenInfo);
        refreshExpiry.put(newRefreshToken, newRefreshExpiresAt);
        accessTokenToRefreshTokenMap.put(newAccessToken, newRefreshToken);
        usernameToAccessTokensMap.computeIfAbsent(userTokenInfo.getUsername(), k -> new TreeSet<>()).add(newAccessToken);

        return userTokenInfo;
    }

    @Override
    public void revokeToken(String accessToken) {
        String refreshToken = accessTokenToRefreshTokenMap.get(accessToken);
        if (refreshToken != null) {
            UserTokenInfo userTokenInfo = refreshIndex.remove(refreshToken);
            if (userTokenInfo != null) {
                Set<String> tokenInfos = usernameToAccessTokensMap.get(userTokenInfo.getUsername());
                if (tokenInfos != null) {
                    tokenInfos.remove(accessToken);
                }
                authenticationHook.onTokenRevoked(accessToken);
            }
            accessTokenToRefreshTokenMap.remove(accessToken);
            refreshExpiry.remove(refreshToken);
        }
    }

    @Override
    public void revokeAllTokens(String username) {
        Set<String> accessTokens = usernameToAccessTokensMap.remove(username);

        if (accessTokens != null) {
            authenticationHook.onRevoked(username);

            accessTokens.forEach(accessToken -> {
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
                        .getOrDefault(username, new TreeSet<>())
                        .size());
    }
}
