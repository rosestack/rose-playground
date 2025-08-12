package io.github.rosestack.spring.boot.redis.ratelimit;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 滑动窗口限流器
 *
 * <p>基于滑动窗口算法的限流实现。在指定的时间窗口内统计请求数量， 超过阈值则拒绝请求。相比固定窗口，滑动窗口能更平滑地处理流量。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SlidingWindowRateLimiter implements RateLimiter {

    // Lua 脚本：滑动窗口算法
    private static final String SLIDING_WINDOW_SCRIPT = "local key = KEYS[1] "
            + "local window = tonumber(ARGV[1]) * 1000 "
            + // 转换为毫秒
            "local limit = tonumber(ARGV[2]) "
            + "local now = tonumber(ARGV[3]) "
            + "local requested = tonumber(ARGV[4]) "
            + "-- 清理过期的记录 "
            + "local cutoff = now - window "
            + "redis.call('ZREMRANGEBYSCORE', key, 0, cutoff) "
            + "-- 获取当前窗口内的请求数 "
            + "local current = redis.call('ZCARD', key) "
            + "local allowed = 0 "
            + "if current + requested <= limit then "
            + "  -- 添加当前请求到窗口 "
            + "  for i = 1, requested do "
            + "    redis.call('ZADD', key, now, now .. ':' .. i) "
            + "  end "
            + "  allowed = 1 "
            + "end "
            + "-- 设置过期时间 "
            + "redis.call('EXPIRE', key, math.ceil(window / 1000) + 1) "
            + "return {allowed, current, limit - current}";
    private final RedisTemplate<String, Object> redisTemplate;
    private final int rate; // 限流速率（时间窗口内最大请求数）
    private final int timeWindow; // 时间窗口大小（秒）
    private final String keyPrefix;

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

            DefaultRedisScript<Object> script = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, Object.class);
            Object result =
                    redisTemplate.execute(script, Collections.singletonList(fullKey), timeWindow, rate, now, permits);

            if (result instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) result;
                Long allowed = (Long) list.get(0);
                Long current = (Long) list.get(1);
                Long remaining = (Long) list.get(2);

                boolean success = allowed != null && allowed == 1;

                if (log.isDebugEnabled()) {
                    log.debug(
                            "滑动窗口限流 - key: {}, 请求数: {}, 当前计数: {}, 剩余配额: {}, 结果: {}",
                            key,
                            permits,
                            current,
                            remaining,
                            success ? "通过" : "拒绝");
                }

                return success;
            }

            return false;
        } catch (Exception e) {
            log.error("滑动窗口限流执行失败: {}", key, e);
            // 发生异常时允许请求通过，避免影响业务
            return true;
        }
    }

    @Override
    public long getAvailablePermits(String key) {
        try {
            String fullKey = buildKey(key);
            long now = System.currentTimeMillis();
            long cutoff = now - (timeWindow * 1000L);

            // 清理过期记录
            redisTemplate.opsForZSet().removeRangeByScore(fullKey, 0, cutoff);

            // 获取当前计数
            Long current = redisTemplate.opsForZSet().count(fullKey, cutoff, now);
            return Math.max(0, rate - (current != null ? current : 0));
        } catch (Exception e) {
            log.error("获取可用配额失败: {}", key, e);
            return rate;
        }
    }

    @Override
    public String getType() {
        return "SLIDING_WINDOW";
    }

    @Override
    public void reset(String key) {
        try {
            String fullKey = buildKey(key);
            redisTemplate.delete(fullKey);
            log.debug("重置滑动窗口状态: {}", key);
        } catch (Exception e) {
            log.error("重置滑动窗口状态失败: {}", key, e);
        }
    }

    @Override
    public RateLimitInfo getInfo(String key) {
        try {
            String fullKey = buildKey(key);
            long now = System.currentTimeMillis();
            long cutoff = now - (timeWindow * 1000L);

            // 清理过期记录
            redisTemplate.opsForZSet().removeRangeByScore(fullKey, 0, cutoff);

            // 获取当前计数
            Long current = redisTemplate.opsForZSet().count(fullKey, cutoff, now);
            long currentCount = current != null ? current : 0;
            long availablePermits = Math.max(0, rate - currentCount);

            return new RateLimitInfo(key, getType(), rate, timeWindow, availablePermits, 0, 0);
        } catch (Exception e) {
            log.error("获取滑动窗口信息失败: {}", key, e);
            return new RateLimitInfo(key, getType(), rate, timeWindow, rate, 0, 0);
        }
    }

    /**
     * 构建完整的 Redis 键名
     */
    private String buildKey(String key) {
        return keyPrefix + "sliding_window:" + key;
    }
}
