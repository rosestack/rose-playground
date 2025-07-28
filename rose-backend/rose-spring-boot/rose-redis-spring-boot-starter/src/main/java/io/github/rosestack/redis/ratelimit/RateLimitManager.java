package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.redis.config.RoseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 限流管理器
 * 
 * <p>统一管理不同类型的限流器，支持多种限流算法。
 * 根据配置动态选择合适的限流器实现。
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitManager {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseRedisProperties properties;
    
    /**
     * 限流器缓存，避免重复创建
     */
    private final ConcurrentMap<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();
    
    /**
     * 检查是否允许请求通过
     * 
     * @param request 发送请求
     * @param rateLimited 限流注解配置
     * @return true 表示允许，false 表示拒绝
     */
    public boolean allow(SendRequest request, RateLimited rateLimited) {
        RateLimiter rateLimiter = getRateLimiter(rateLimited);
        return rateLimiter.allow(request);
    }
    
    /**
     * 记录一次请求
     * 
     * @param request 发送请求
     * @param rateLimited 限流注解配置
     */
    public void record(SendRequest request, RateLimited rateLimited) {
        RateLimiter rateLimiter = getRateLimiter(rateLimited);
        rateLimiter.record(request);
    }
    
    /**
     * 获取限流器实例
     * 
     * @param rateLimited 限流注解配置
     * @return 限流器实例
     */
    private RateLimiter getRateLimiter(RateLimited rateLimited) {
        // 构建缓存 key
        String cacheKey = buildCacheKey(rateLimited);
        
        return rateLimiterCache.computeIfAbsent(cacheKey, key -> {
            RoseRedisProperties.RateLimit.Algorithm algorithm = rateLimited.algorithm();
            
            switch (algorithm) {
                case TOKEN_BUCKET:
                    return createTokenBucketRateLimiter(rateLimited);
                case SLIDING_WINDOW:
                    return createSlidingWindowRateLimiter(rateLimited);
                default:
                    log.warn("Unknown rate limit algorithm: {}, using default TOKEN_BUCKET", algorithm);
                    return createTokenBucketRateLimiter(rateLimited);
            }
        });
    }
    
    /**
     * 创建令牌桶限流器
     */
    private RateLimiter createTokenBucketRateLimiter(RateLimited rateLimited) {
        // 创建自定义配置的属性对象
        RoseRedisProperties customProperties = createCustomProperties(rateLimited);
        return new RedisRateLimiter(redisTemplate, customProperties);
    }
    
    /**
     * 创建滑动窗口限流器
     */
    private RateLimiter createSlidingWindowRateLimiter(RateLimited rateLimited) {
        // 创建自定义配置的属性对象
        RoseRedisProperties customProperties = createCustomProperties(rateLimited);
        return new SlidingWindowRateLimiter(redisTemplate, customProperties);
    }
    
    /**
     * 创建自定义配置的属性对象
     */
    private RoseRedisProperties createCustomProperties(RateLimited rateLimited) {
        RoseRedisProperties customProperties = new RoseRedisProperties();
        RoseRedisProperties.RateLimit rateLimit = new RoseRedisProperties.RateLimit();
        
        // 复制全局配置
        rateLimit.setEnabled(properties.getRateLimit().isEnabled());
        rateLimit.setKeyPrefix(properties.getRateLimit().getKeyPrefix());
        rateLimit.setAlgorithm(rateLimited.algorithm());
        
        // 应用注解配置，如果注解中指定了值则使用注解值，否则使用全局配置
        rateLimit.setCapacity(rateLimited.capacity() > 0 ? rateLimited.capacity() : properties.getRateLimit().getCapacity());
        rateLimit.setRefillRate(rateLimited.refillRate() > 0 ? rateLimited.refillRate() : properties.getRateLimit().getRefillRate());
        rateLimit.setWindowSize(rateLimited.windowSize() > 0 ? rateLimited.windowSize() : properties.getRateLimit().getWindowSize());
        rateLimit.setMaxRequests(rateLimited.maxRequests() > 0 ? rateLimited.maxRequests() : properties.getRateLimit().getMaxRequests());
        rateLimit.setFailOpen(rateLimited.failOpen());
        
        customProperties.setRateLimit(rateLimit);
        return customProperties;
    }
    
    /**
     * 构建缓存 key
     */
    private String buildCacheKey(RateLimited rateLimited) {
        return String.format("%s:%d:%d:%d:%d:%s", 
            rateLimited.algorithm().name(),
            rateLimited.capacity(),
            rateLimited.refillRate(),
            rateLimited.windowSize(),
            rateLimited.maxRequests(),
            rateLimited.failOpen()
        );
    }
    
    /**
     * 清理限流器缓存
     */
    public void clearCache() {
        rateLimiterCache.clear();
        log.info("Rate limiter cache cleared");
    }
    
    /**
     * 获取缓存统计信息
     */
    public int getCacheSize() {
        return rateLimiterCache.size();
    }
}