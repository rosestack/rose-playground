package io.github.rosestack.spring.boot.redis.ratelimit;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 令牌桶限流器
 *
 * <p>基于令牌桶算法的限流实现。令牌桶以固定速率生成令牌，请求需要消耗令牌才能通过。 支持突发流量处理，桶容量决定了能处理的最大突发请求数。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int rate; // 令牌生成速率（每秒）
    private final int capacity; // 桶容量
    private final String keyPrefix;

    // Lua 脚本：令牌桶算法
    private static final String TOKEN_BUCKET_SCRIPT = "local key = KEYS[1] "
            + "local capacity = tonumber(ARGV[1]) "
            + "local rate = tonumber(ARGV[2]) "
            + "local requested = tonumber(ARGV[3]) "
            + "local now = tonumber(ARGV[4]) "
            + "local bucket = redis.call('HMGET', key, 'tokens', 'last_refill') "
            + "local tokens = tonumber(bucket[1]) or capacity "
            + "local last_refill = tonumber(bucket[2]) or now "
            + "-- 计算需要添加的令牌数 "
            + "local elapsed = math.max(0, now - last_refill) "
            + "local tokens_to_add = math.floor(elapsed * rate / 1000) "
            + "tokens = math.min(capacity, tokens + tokens_to_add) "
            + "local allowed = 0 "
            + "if tokens >= requested then "
            + "  tokens = tokens - requested "
            + "  allowed = 1 "
            + "end "
            + "-- 更新桶状态 "
            + "redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now) "
            + "redis.call('EXPIRE', key, 3600) "
            + "return {allowed, tokens}";

    @Override
    public boolean tryAcquire(String key) {
        return tryAcquire(key, 1);
    }

    @Override
    public boolean tryAcquire(String key, int permits) {
        if (permits <= 0) {
            return true;
        }

        try {
            String fullKey = buildKey(key);
            long now = System.currentTimeMillis();

            DefaultRedisScript<Object> script = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, Object.class);
            Object result =
                    redisTemplate.execute(script, Collections.singletonList(fullKey), capacity, rate, permits, now);

            if (result instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) result;
                Long allowed = (Long) list.get(0);
                Long remainingTokens = (Long) list.get(1);

                boolean success = allowed != null && allowed == 1;

                if (log.isDebugEnabled()) {
                    log.debug(
                            "令牌桶限流 - key: {}, 请求令牌: {}, 剩余令牌: {}, 结果: {}",
                            key,
                            permits,
                            remainingTokens,
                            success ? "通过" : "拒绝");
                }

                return success;
            }

            return false;
        } catch (Exception e) {
            log.error("令牌桶限流执行失败: {}", key, e);
            // 发生异常时允许请求通过，避免影响业务
            return true;
        }
    }

    @Override
    public long getAvailablePermits(String key) {
        try {
            String fullKey = buildKey(key);
            Object tokens = redisTemplate.opsForHash().get(fullKey, "tokens");
            return tokens != null ? Long.parseLong(tokens.toString()) : capacity;
        } catch (Exception e) {
            log.error("获取可用令牌数失败: {}", key, e);
            return capacity;
        }
    }

    @Override
    public String getType() {
        return "TOKEN_BUCKET";
    }

    @Override
    public void reset(String key) {
        try {
            String fullKey = buildKey(key);
            redisTemplate.delete(fullKey);
            log.debug("重置令牌桶状态: {}", key);
        } catch (Exception e) {
            log.error("重置令牌桶状态失败: {}", key, e);
        }
    }

    @Override
    public RateLimitInfo getInfo(String key) {
        try {
            String fullKey = buildKey(key);
            Object tokens = redisTemplate.opsForHash().get(fullKey, "tokens");
            long availablePermits = tokens != null ? Long.parseLong(tokens.toString()) : capacity;

            // 这里简化处理，实际项目中可以维护更详细的统计信息
            return new RateLimitInfo(key, getType(), rate, 1, availablePermits, 0, 0);
        } catch (Exception e) {
            log.error("获取令牌桶信息失败: {}", key, e);
            return new RateLimitInfo(key, getType(), rate, 1, capacity, 0, 0);
        }
    }

    /** 构建完整的 Redis 键名 */
    private String buildKey(String key) {
        return keyPrefix + "token_bucket:" + key;
    }
}
