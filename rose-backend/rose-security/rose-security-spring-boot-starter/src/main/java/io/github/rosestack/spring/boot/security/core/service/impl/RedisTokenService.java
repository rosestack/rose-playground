package io.github.rosestack.spring.boot.security.core.service.impl;

import io.github.rosestack.core.util.JsonUtils;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.extension.AuthenticationHook;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Redis Token 服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class RedisTokenService implements TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseSecurityProperties properties;
    private final AuthenticationHook authenticationHook;

    @Override
    public TokenInfo createToken(UserDetails userDetails) {
        // 并发控制
        int max = properties.getAuth().getToken().getMaximumSessions();
        boolean prevent = properties.getAuth().getToken().isMaxSessionsPreventsLogin();
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long current = setOps.size(userTokensKey(userDetails.getUsername()));
        int count = current == null ? 0 : current.intValue();
        if (prevent && count >= max) {
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

        // 写入 Redis
        String key = tokenKey(token);
        redisTemplate.opsForValue().set(key, info);
        // 过期时间
        long seconds = properties.getAuth().getToken().getExpiration().toSeconds();
        redisTemplate.expire(key, java.time.Duration.ofSeconds(seconds));
        // 用户 token 集合
        setOps.add(userTokensKey(userDetails.getUsername()), token);
        // 为了自动清理，设置用户集合的过期时间为至少一个 token 的过期时长
        redisTemplate.expire(userTokensKey(userDetails.getUsername()), java.time.Duration.ofSeconds(seconds));

        log.debug("[Redis] 创建 token: {} for user: {}", token, userDetails.getUsername());
        return info;
    }

    @Override
    public boolean validateToken(String token) {
        Optional<TokenInfo> infoOpt = readToken(token);
        return infoOpt.filter(info -> !info.isExpired()).isPresent();
    }

    @Override
    public Optional<UserDetails> getUserDetails(String token) {
        Optional<TokenInfo> infoOpt = readToken(token);
        return infoOpt.filter(info -> !info.isExpired()).map(info -> User.withUsername(info.getUsername())
                .password("")
                .authorities("ROLE_USER")
                .build());
    }

    @Override
    public Optional<TokenInfo> refreshToken(String token) {
        Optional<TokenInfo> origOpt = readToken(token);
        if (origOpt.isEmpty()) return Optional.empty();
        TokenInfo orig = origOpt.get();
        if (LocalDateTime.now()
                .isAfter(
                        orig.getCreatedAt().plus(properties.getAuth().getToken().getRefreshWindow()))) {
            return Optional.empty();
        }

        // 创建新 token
        String newToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        TokenInfo newInfo = TokenInfo.builder()
                .accessToken(newToken)
                .refreshToken(UUID.randomUUID().toString())
                .tokenType(TOKEN_TYPE_SIMPLE)
                .expiresAt(now.plus(properties.getAuth().getToken().getExpiration()))
                .username(orig.getUsername())
                .createdAt(now)
                .build();

        // 写入新 token
        String newKey = tokenKey(newToken);
        redisTemplate.opsForValue().set(newKey, newInfo);

        long seconds = properties.getAuth().getToken().getExpiration().toSeconds();
        redisTemplate.expire(newKey, java.time.Duration.ofSeconds(seconds));

        // 替换用户集合中的 token
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.remove(userTokensKey(orig.getUsername()), token);
        setOps.add(userTokensKey(orig.getUsername()), newToken);
        redisTemplate.expire(userTokensKey(orig.getUsername()), java.time.Duration.ofSeconds(seconds));

        // 删除旧 token
        redisTemplate.delete(tokenKey(token));
        return Optional.of(newInfo);
    }

    @Override
    public void revokeToken(String token) {
        Optional<TokenInfo> infoOpt = readToken(token);
        if (infoOpt.isPresent()) {
            TokenInfo info = infoOpt.get();
            redisTemplate.delete(tokenKey(token));
            redisTemplate.opsForSet().remove(userTokensKey(info.getUsername()), token);

            authenticationHook.onTokenRevoked(token);
        }
    }

    @Override
    public void revokeAllTokens(String username) {
        String key = userTokensKey(username);
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        java.util.Set<Object> tokens = setOps.members(key);
        if (tokens != null && !tokens.isEmpty()) {
            for (Object t : tokens) {
                redisTemplate.delete(tokenKey((String) t));
            }
            redisTemplate.delete(key);

            authenticationHook.onRevoked(username);
        }
    }

    @Override
    public int getActiveTokenCount(String username) {
        Long size = redisTemplate.opsForSet().size(userTokensKey(username));
        return size == null ? 0 : size.intValue();
    }

    private String tokenKey(String token) {
        String prefix = properties.getAuth().getToken().getRedis().getKeyPrefix();
        if (!prefix.endsWith(":")) {
            prefix = prefix + ":";
        }
        return prefix + "token:" + token;
    }

    private String userTokensKey(String username) {
        String prefix = properties.getAuth().getToken().getRedis().getKeyPrefix();
        if (!prefix.endsWith(":")) {
            prefix = prefix + ":";
        }
        return prefix + "user:" + username + ":tokens";
    }

    private Optional<TokenInfo> readToken(String token) {
        try {
            TokenInfo info =
                    JsonUtils.fromString((String) redisTemplate.opsForValue().get(tokenKey(token)), TokenInfo.class);
            return Optional.of(info);
        } catch (Exception e) {
            log.warn("解析 Redis Token 失败: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
