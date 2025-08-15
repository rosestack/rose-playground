package io.github.rosestack.spring.boot.security.core.service.impl;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.AbstractTokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis Token 服务实现
 *
 * <p>基于Redis的Token存储，支持分布式部署和数据持久化。
 * 使用Redis的不同数据结构来映射内存版本的HashMap关系：
 * - Hash: 存储TokenInfo对象 (refreshToken -> TokenInfo)
 * - String: 存储访问索引 (accessToken -> refreshToken)
 * - ZSet: 存储用户会话 (username -> TokenInfo按创建时间排序)
 * </p>
 */
@Slf4j
public class RedisTokenService extends AbstractTokenService {

    protected final RedisTemplate<String, Object> redisTemplate;

    // Redis Key前缀
    private static final String TOKEN_KEY_PREFIX = "rose:token:";
    private static final String ACCESS_INDEX_PREFIX = "rose:access_index:";
    private static final String USER_SESSIONS_PREFIX = "rose:user_sessions:";

    public RedisTokenService(
            RoseSecurityProperties.Auth.Token properties,
            AuthenticationHook authenticationHook,
            RedisTemplate<String, Object> redisTemplate) {
        super(properties, authenticationHook);
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void removeTokenInfo(TokenInfo tokenInfo) {
        String refreshToken = tokenInfo.getRefreshToken();
        String accessToken = tokenInfo.getAccessToken();
        String username = tokenInfo.getUsername();

        // 使用 Redis Pipeline 保证原子性
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // 1. 删除主Token存储
            String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);

            // 2. 删除访问索引
            String accessIndexKey = ACCESS_INDEX_PREFIX + accessToken;
            redisTemplate.delete(accessIndexKey);

            // 3. 从用户会话ZSet中移除
            String userSessionsKey = USER_SESSIONS_PREFIX + username;
            redisTemplate.opsForZSet().remove(userSessionsKey, refreshToken);

            return null;
        });

        log.debug("从Redis完全移除Token，refreshToken: {}, accessToken: {}, username: {}",
                refreshToken, accessToken, username);
    }

    @Override
    protected TokenInfo findTokenInfoByAccessToken(String accessToken) {
        String accessIndexKey = ACCESS_INDEX_PREFIX + accessToken;
        String refreshToken = (String) redisTemplate.opsForValue().get(accessIndexKey);
        
        if (refreshToken == null) {
            return null;
        }
        
        return findTokenInfoByRefreshToken(refreshToken);
    }

    @Override
    protected TokenInfo findTokenInfoByRefreshToken(String refreshToken) {
        String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
        Object tokenObj = redisTemplate.opsForValue().get(tokenKey);
        return tokenObj instanceof TokenInfo ? (TokenInfo) tokenObj : null;
    }

    @Override
    protected ConcurrentSkipListSet<TokenInfo> findTokenInfosByUsername(String username) {
        String userSessionsKey = USER_SESSIONS_PREFIX + username;
        
        // 获取用户所有的refreshToken
        Set<Object> refreshTokenObjs = redisTemplate.opsForZSet().range(userSessionsKey, 0, -1);
        if (refreshTokenObjs == null || refreshTokenObjs.isEmpty()) {
            return new ConcurrentSkipListSet<>();
        }

        // 获取对应的TokenInfo并按创建时间排序
        ConcurrentSkipListSet<TokenInfo> tokenInfos = new ConcurrentSkipListSet<>();
        for (Object refreshTokenObj : refreshTokenObjs) {
            if (refreshTokenObj instanceof String) {
                String refreshToken = (String) refreshTokenObj;
                TokenInfo tokenInfo = findTokenInfoByRefreshToken(refreshToken);
                if (tokenInfo != null) {
                    tokenInfos.add(tokenInfo);
                }
            }
        }
        
        return tokenInfos;
    }

    @Override
    protected ConcurrentSkipListSet<TokenInfo> findAllTokenInfos() {
        // Redis模式下，为了性能考虑，通过模式匹配获取所有token
        Set<String> tokenKeys = redisTemplate.keys(TOKEN_KEY_PREFIX + "*");
        if (tokenKeys == null || tokenKeys.isEmpty()) {
            return new ConcurrentSkipListSet<>();
        }

        ConcurrentSkipListSet<TokenInfo> allTokens = new ConcurrentSkipListSet<>();
        for (String tokenKey : tokenKeys) {
            Object tokenObj = redisTemplate.opsForValue().get(tokenKey);
            if (tokenObj instanceof TokenInfo) {
                allTokens.add((TokenInfo) tokenObj);
            }
        }
        
        return allTokens;
    }

    @Override
    protected void storeTokenInfo(TokenInfo tokenInfo) {
        String refreshToken = tokenInfo.getRefreshToken();
        String accessToken = tokenInfo.getAccessToken();
        String username = tokenInfo.getUsername();

        // 使用 Redis Pipeline 保证原子性
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // 1. 存储 TokenInfo (refreshToken -> TokenInfo)
            String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
            redisTemplate.opsForValue().set(tokenKey, tokenInfo);

            // 2. 设置 refreshToken 过期时间
            long refreshTtl = calculateRedisTtl(tokenInfo.getRefreshExpiresAt());
            redisTemplate.expire(tokenKey, refreshTtl, TimeUnit.SECONDS);

            // 3. 存储访问索引 (accessToken -> refreshToken)
            String accessIndexKey = ACCESS_INDEX_PREFIX + accessToken;
            redisTemplate.opsForValue().set(accessIndexKey, refreshToken);

            // 4. 设置 accessToken 索引过期时间
            long accessTtl = calculateRedisTtl(tokenInfo.getExpiresAt());
            redisTemplate.expire(accessIndexKey, accessTtl, TimeUnit.SECONDS);

            // 5. 添加到用户会话ZSet (按创建时间排序)
            String userSessionsKey = USER_SESSIONS_PREFIX + username;
            double score = tokenInfo.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toEpochSecond();
            redisTemplate.opsForZSet().add(userSessionsKey, refreshToken, score);

            // 6. 设置用户会话集合过期时间（使用较长的时间）
            redisTemplate.expire(userSessionsKey, refreshTtl, TimeUnit.SECONDS);

            return null;
        });

        log.debug("存储Token到Redis，refreshToken: {}, accessToken: {}, username: {}",
                refreshToken, accessToken, username);
    }

    /**
     * 计算Redis TTL（秒）
     */
    private long calculateRedisTtl(LocalDateTime expireTime) {
        long seconds = java.time.Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        return Math.max(1, seconds); // 至少1秒
    }
}
