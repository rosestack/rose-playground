package io.github.rosestack.spring.boot.security.core.service;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.jwt.TokenManagementHook;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Token服务抽象基类
 *
 * <p>提取TokenService实现类的公共逻辑，包括：
 * <ul>
 *   <li>统一的回调hook方法调用</li>
 *   <li>通用的Token生成和时间计算工具</li>
 *   <li>标准的参数验证逻辑</li>
 *   <li>常量定义和配置访问</li>
 * </ul>
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTokenService implements TokenService {
    String TOKEN_TYPE_SIMPLE = "simple";

    /**
     * 配置属性
     */
    protected final RoseSecurityProperties.Token properties;

    /**
     * 认证钩子
     */
    protected final TokenManagementHook authenticationHook;

    /**
     * 标准的Token创建流程
     */
    @Override
    public TokenInfo createToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails不能为空");
        }
        if (userDetails.getUsername() == null
                || userDetails.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        String username = userDetails.getUsername();

        // 会话数量控制
        int max = properties.getMaximumSessions();
        if (max > 0) {
            Set<String> activeTokens = getActiveTokens(username);
            int currentSessions = activeTokens.size();

            if (currentSessions >= max) {
                if (properties.isMaxSessionsPreventsLogin()) {
                    throw new IllegalStateException("超过最大并发会话数");
                } else {
                    // 回收最早创建的会话
                    ConcurrentSkipListSet<TokenInfo> tokenInfos = findTokenInfosByUsername(username);
                    if (tokenInfos != null) {
                        TokenInfo evicted = tokenInfos.pollFirst();
                        if (evicted != null) {
                            log.debug("为用户 {} 回收最早会话: {}", username, evicted.getAccessToken());
                            removeTokenInfo(evicted);
                            authenticationHook.onTokenRevoked(evicted.getUsername(), evicted.getAccessToken());
                        }
                    }
                }
            }
        }

        // 创建和存储Token
        TokenInfo tokenInfo = buildTokenInfo(username);
        storeTokenInfo(tokenInfo);

        return tokenInfo;
    }

    @Override
    public UserDetails getUserDetails(String accessToken) {
        TokenInfo tokenInfo = findTokenInfoByAccessToken(accessToken);
        if (tokenInfo == null) {
            return null;
        }

        if (tokenInfo.isExpired()) {
            authenticationHook.onTokenExpired(accessToken);
            return null;
        }

        return User.withUsername(tokenInfo.getUsername())
                .password("")
                .authorities("ROLE_USER")
                .build();
    }

    /**
     * Token验证的通用逻辑模板
     * 子类可以重写此方法来自定义验证流程
     */
    @Override
    public boolean validateToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("accessToken不能为空");
        }

        TokenInfo tokenInfo = this.findTokenInfoByAccessToken(accessToken);
        if (tokenInfo == null) {
            return false;
        }

        if (tokenInfo.isExpired()) {
            authenticationHook.onTokenExpired(accessToken);
            return false;
        }

        // 调用子类的额外验证逻辑
        return additionalValidation(accessToken, tokenInfo);
    }

    /**
     * Token刷新的通用逻辑模板
     * 子类可以重写此方法来自定义刷新流程
     */
    @Override
    public TokenInfo refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("refreshToken不能为空");
        }

        // 前置检查
        if (!authenticationHook.beforeTokenRefresh(refreshToken)) {
            throw new IllegalArgumentException("Token刷新被拦截");
        }

        TokenInfo oldTokenInfo = findTokenInfoByRefreshToken(refreshToken);
        if (oldTokenInfo == null) {
            throw new IllegalArgumentException("refreshToken 不存在");
        }

        // 检查refreshToken是否过期
        if (oldTokenInfo.isRefreshExpired()) {
            throw new IllegalArgumentException("refreshToken 已过期");
        }

        // 检查是否需要刷新（仅当accessToken临近过期或已过期时允许刷新）
        LocalDateTime accessTokenExpireTime = oldTokenInfo.getExpiresAt();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refreshWindowStart = accessTokenExpireTime.minus(properties.getRefreshWindow());

        if (now.isBefore(refreshWindowStart) && !oldTokenInfo.isExpired()) {
            return oldTokenInfo; // 太早，不刷新
        }

        // 构建新Token
        String username = oldTokenInfo.getUsername();
        TokenInfo newTokenInfo = buildTokenInfo(username);

        // 原子替换Token：先移除旧Token，再存储新Token
        removeTokenInfo(oldTokenInfo);
        storeTokenInfo(newTokenInfo);

        authenticationHook.onTokenRefreshSuccess(newTokenInfo.getUsername(), newTokenInfo.getAccessToken());
        return newTokenInfo;
    }

    @Override
    public void revokeToken(String accessToken) {
        TokenInfo tokenInfo = findTokenInfoByAccessToken(accessToken);
        if (tokenInfo != null) {
            removeTokenInfo(tokenInfo);
        }
    }

    @Override
    public void revokeAllTokens(String username) {
        Set<TokenInfo> tokenInfos = findTokenInfosByUsername(username);

        if (tokenInfos != null && !tokenInfos.isEmpty()) {
            for (TokenInfo tokenInfo : tokenInfos) {
                removeTokenInfo(tokenInfo);
                authenticationHook.onTokenRevoked(username, tokenInfo.getAccessToken());
            }
        }
    }

    @Override
    public Set<String> getActiveTokens(String username) {
        Set<TokenInfo> tokenInfos = findTokenInfosByUsername(username);

        if (tokenInfos == null || tokenInfos.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> actives = new HashSet<>();
        for (TokenInfo tokenInfo : tokenInfos) {
            if (tokenInfo.isExpired()) {
                removeTokenInfo(tokenInfo);
                authenticationHook.onTokenRevoked(username, tokenInfo.getAccessToken());
            } else {
                actives.add(tokenInfo.getAccessToken());
            }
        }
        return actives;
    }

    /**
     * 额外的 Token 验证逻辑
     *
     * <p>
     * 子类可以重写此方法来添加特定的验证逻辑，如 JWT 签名验证等。
     * 此方法在基础验证（非空检查、存储查找、过期时间检查）通过后调用。
     * </p>
     *
     * @param accessToken 访问令牌
     * @param tokenInfo   Token 信息对象
     * @return 验证是否通过
     */
    protected boolean additionalValidation(String accessToken, TokenInfo tokenInfo) {
        return true; // 默认通过额外验证
    }

    /**
     * 生成访问Token字符串
     * 子类可以重写以使用不同的生成策略（如JWT格式）
     */
    protected String generateAccessToken(String username) {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成刷新Token字符串
     * 子类可以重写以使用不同的生成策略
     */
    protected String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * 构建TokenInfo对象
     * 子类可以重写以自定义Token构建逻辑（如设置不同的tokenType）
     */
    protected TokenInfo buildTokenInfo(String username) {
        LocalDateTime now = LocalDateTime.now();
        String accessToken = generateAccessToken(username);
        String refreshToken = generateRefreshToken();

        log.debug("创建 accessToken: {} for user: {}", accessToken, username);

        LocalDateTime accessExpiresAt = LocalDateTime.now().plus(properties.getAccessTokenExpiredTime());
        LocalDateTime refreshExpiresAt = LocalDateTime.now().plus(properties.getRefreshTokenExpiredTime());

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_SIMPLE)
                .username(username)
                .expiresAt(accessExpiresAt)
                .refreshExpiresAt(refreshExpiresAt)
                .createdAt(now)
                .build();
    }

    public void cleanupExpiredTokens() {
        ConcurrentSkipListSet<TokenInfo> allTokens = findAllTokenInfos();
        for (TokenInfo tokenInfo : allTokens) {
            if (tokenInfo.isExpired()) {
                removeTokenInfo(tokenInfo);
            }
        }
    }

    protected abstract TokenInfo findTokenInfoByAccessToken(String accessToken);

    protected abstract TokenInfo findTokenInfoByRefreshToken(String refreshToken);

    protected abstract ConcurrentSkipListSet<TokenInfo> findTokenInfosByUsername(String username);

    protected abstract ConcurrentSkipListSet<TokenInfo> findAllTokenInfos();

    /**
     * 存储TokenInfo到具体的存储介质
     */
    protected abstract void storeTokenInfo(TokenInfo tokenInfo);

    /**
     * 从存储中完全移除Token
     */
    protected abstract void removeTokenInfo(TokenInfo tokenInfo);
}
