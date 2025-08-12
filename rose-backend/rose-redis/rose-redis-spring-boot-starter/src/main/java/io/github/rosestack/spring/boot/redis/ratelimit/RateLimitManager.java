package io.github.rosestack.spring.boot.redis.ratelimit;

import io.github.rosestack.spring.boot.redis.config.RoseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流管理器
 *
 * <p>负责创建、管理和配置不同类型的限流器实例。 支持多种限流算法，提供统一的限流服务接口。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseRedisProperties properties;

    // 限流器实例缓存
    private final ConcurrentHashMap<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

    /**
     * 获取默认限流器
     *
     * @param key 限流键
     * @return 限流器实例
     */
    public RateLimiter getRateLimiter(String key) {
        RoseRedisProperties.RateLimit config = properties.getRateLimit();
        return getRateLimiter(
                key, config.getDefaultAlgorithm(), config.getDefaultRate(), config.getDefaultTimeWindow());
    }

    /**
     * 获取指定算法的限流器
     *
     * @param key       限流键
     * @param algorithm 限流算法
     * @return 限流器实例
     */
    public RateLimiter getRateLimiter(String key, RoseRedisProperties.RateLimit.Algorithm algorithm) {
        RoseRedisProperties.RateLimit config = properties.getRateLimit();
        return getRateLimiter(key, algorithm, config.getDefaultRate(), config.getDefaultTimeWindow());
    }

    /**
     * 获取指定配置的限流器
     *
     * @param key        限流键
     * @param algorithm  限流算法
     * @param rate       限流速率
     * @param timeWindow 时间窗口
     * @return 限流器实例
     */
    public RateLimiter getRateLimiter(
            String key, RoseRedisProperties.RateLimit.Algorithm algorithm, int rate, int timeWindow) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("限流键不能为空");
        }

        String cacheKey = buildCacheKey(key, algorithm, rate, timeWindow);

        return rateLimiterCache.computeIfAbsent(cacheKey, k -> {
            log.debug("创建新的限流器实例: {}, 算法: {}, 速率: {}, 时间窗口: {}", key, algorithm, rate, timeWindow);
            return createRateLimiter(algorithm, rate, timeWindow);
        });
    }

    /**
     * 尝试获取许可
     *
     * @param key 限流键
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key) {
        return getRateLimiter(key).tryAcquire(key);
    }

    /**
     * 尝试获取指定数量的许可
     *
     * @param key     限流键
     * @param permits 许可数量
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, int permits) {
        return getRateLimiter(key).tryAcquire(key, permits);
    }

    /**
     * 尝试获取许可（指定算法）
     *
     * @param key       限流键
     * @param algorithm 限流算法
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, RoseRedisProperties.RateLimit.Algorithm algorithm) {
        return getRateLimiter(key, algorithm).tryAcquire(key);
    }

    /**
     * 尝试获取许可（完整配置）
     *
     * @param key        限流键
     * @param algorithm  限流算法
     * @param rate       限流速率
     * @param timeWindow 时间窗口
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, RoseRedisProperties.RateLimit.Algorithm algorithm, int rate, int timeWindow) {
        return getRateLimiter(key, algorithm, rate, timeWindow).tryAcquire(key);
    }

    /**
     * 获取限流信息
     *
     * @param key 限流键
     * @return 限流信息
     */
    public RateLimiter.RateLimitInfo getInfo(String key) {
        return getRateLimiter(key).getInfo(key);
    }

    /**
     * 重置限流状态
     *
     * @param key 限流键
     */
    public void reset(String key) {
        getRateLimiter(key).reset(key);
    }

    /**
     * 获取当前缓存的限流器数量
     *
     * @return 限流器数量
     */
    public int getCachedRateLimiterCount() {
        return rateLimiterCache.size();
    }

    /**
     * 清理所有限流器实例
     */
    public void clearAllRateLimiters() {
        rateLimiterCache.clear();
        log.info("清理所有限流器实例");
    }

    /**
     * 创建限流器实例
     */
    private RateLimiter createRateLimiter(RoseRedisProperties.RateLimit.Algorithm algorithm, int rate, int timeWindow) {
        String keyPrefix = properties.getRateLimit().getKeyPrefix();

        switch (algorithm) {
            case TOKEN_BUCKET:
                int capacity = rate * 2; // 默认容量为速率的2倍
                return new TokenBucketRateLimiter(redisTemplate, rate, capacity, keyPrefix);

            case SLIDING_WINDOW:
                return new SlidingWindowRateLimiter(redisTemplate, rate, timeWindow, keyPrefix);

            case FIXED_WINDOW:
                // TODO: 实现固定窗口算法
                throw new UnsupportedOperationException("固定窗口算法暂未实现");

            default:
                throw new IllegalArgumentException("不支持的限流算法: " + algorithm);
        }
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(
            String key, RoseRedisProperties.RateLimit.Algorithm algorithm, int rate, int timeWindow) {
        return String.format("%s:%s:%d:%d", key, algorithm, rate, timeWindow);
    }
}
