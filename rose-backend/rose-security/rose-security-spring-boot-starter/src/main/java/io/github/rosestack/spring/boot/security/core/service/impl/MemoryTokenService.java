package io.github.rosestack.spring.boot.security.core.service.impl;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.AbstractTokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;

/**
 * 内存 Token 服务实现
 *
 * <p>支持：
 * <ul>
 *   <li>并发会话控制（最大会话数限制）</li>
 *   <li>最早创建会话回收策略</li>
 *   <li>定期清理过期Token</li>
 *   <li>Token过期和刷新时的回调钩子</li>
 * </ul>
 */
@Slf4j
public class MemoryTokenService extends AbstractTokenService {

    public MemoryTokenService(RoseSecurityProperties.Token properties, AuthenticationHook authenticationHook) {
        super(properties, authenticationHook);
    }

    /**
     * 主Token存储：refreshToken -> TokenInfo映射
     * 这是唯一的TokenInfo存储位置，消除数据冗余
     */
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    /**
     * 快速访问索引：accessToken -> refreshToken映射
     * 用于通过accessToken快速定位到对应的refreshToken
     */
    private final Map<String, String> accessToRefreshIndex = new ConcurrentHashMap<>();

    /**
     * 用户会话索引：用户名 -> 按创建时间自动排序的TokenInfo集合（最早的在前面）
     * 用于支持"最早创建"会话回收策略和并发会话控制
     */
    private final Map<String, ConcurrentSkipListSet<TokenInfo>> userSessionsIndex = new ConcurrentHashMap<>();

    @Override
    protected void storeTokenInfo(TokenInfo tokenInfo) {
        String refreshToken = tokenInfo.getRefreshToken();
        String accessToken = tokenInfo.getAccessToken();
        String username = tokenInfo.getUsername();

        tokenStore.put(refreshToken, tokenInfo);
        accessToRefreshIndex.put(accessToken, refreshToken);

        // 添加TokenInfo到用户会话集合（自动按创建时间排序）
        userSessionsIndex
                .computeIfAbsent(username, k -> new ConcurrentSkipListSet<>())
                .add(tokenInfo);
    }

    @Override
    protected void removeTokenInfo(TokenInfo tokenInfo) {
        String refreshToken = tokenInfo.getRefreshToken();
        String accessToken = tokenInfo.getAccessToken();
        String username = tokenInfo.getUsername();

        // 从主存储移除
        tokenStore.remove(refreshToken);

        // 从访问索引移除
        accessToRefreshIndex.remove(accessToken);

        // 从用户会话索引移除
        ConcurrentSkipListSet<TokenInfo> sessions = userSessionsIndex.get(username);
        if (sessions != null) {
            sessions.remove(tokenInfo);
            // 如果用户没有其他会话，清空会话集合
            if (sessions.isEmpty()) {
                userSessionsIndex.remove(username);
            }
        }
    }

    @Override
    protected TokenInfo findTokenInfoByRefreshToken(String refreshToken) {
        return tokenStore.get(refreshToken);
    }

    @Override
    protected TokenInfo findTokenInfoByAccessToken(String accessToken) {
        String refreshToken = accessToRefreshIndex.get(accessToken);
        if (refreshToken == null) {
            return null;
        }
        return findTokenInfoByRefreshToken(refreshToken);
    }

    @Override
    protected ConcurrentSkipListSet<TokenInfo> findTokenInfosByUsername(String username) {
        if (username == null) {
            return null;
        }
        return userSessionsIndex.get(username);
    }

    @Override
    protected ConcurrentSkipListSet<TokenInfo> findAllTokenInfos() {
        return new ConcurrentSkipListSet<>(tokenStore.values());
    }
}
