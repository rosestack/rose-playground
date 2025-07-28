package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.redis.config.RoseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 基于 Redis 的滑动窗口限流器实现
 * 
 * <p>使用 Redis ZSet 存储请求时间戳，通过 Lua 脚本确保原子性操作。
 * 支持按目标（target）进行独立限流，每个目标维护独立的滑动窗口。
 * 
 * <p>滑动窗口算法特点：
 * <ul>
 *   <li>精确限流：严格按照时间窗口内的请求数量进行限制</li>
 *   <li>平滑处理：窗口随时间滑动，避免固定窗口的边界效应</li>
 *   <li>内存效率：自动清理过期的请求记录</li>
 * </ul>
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SlidingWindowRateLimiter implements RateLimiter {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseRedisProperties properties;
    
    /**
     * 滑动窗口算法 Lua 脚本
     * 
     * KEYS[1]: 滑动窗口的 Redis key (ZSet)
     * ARGV[1]: 窗口大小（毫秒）
     * ARGV[2]: 最大请求数
     * ARGV[3]: 当前时间戳（毫秒）
     * ARGV[4]: 请求唯一标识（用作 ZSet 的 member）
     * 
     * 返回值：1表示允许，0表示拒绝
     */
    private static final String SLIDING_WINDOW_SCRIPT = """
            local key = KEYS[1]
            local window_size = tonumber(ARGV[1])
            local max_requests = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local request_id = ARGV[4]
            
            -- 计算窗口开始时间
            local window_start = now - window_size
            
            -- 清理过期的请求记录
            redis.call('ZREMRANGEBYSCORE', key, 0, window_start)
            
            -- 获取当前窗口内的请求数量
            local current_requests = redis.call('ZCARD', key)
            
            -- 检查是否超过限制
            if current_requests < max_requests then
                -- 添加当前请求到窗口
                redis.call('ZADD', key, now, request_id)
                -- 设置过期时间（窗口大小 + 缓冲时间）
                redis.call('EXPIRE', key, math.ceil(window_size / 1000) + 60)
                return 1
            else
                return 0
            end
            """;
    
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, Long.class);
    
    @Override
    public boolean allow(SendRequest request) {
        if (!properties.getRateLimit().isEnabled()) {
            return true;
        }
        
        String key = buildKey(request);
        RoseRedisProperties.RateLimit config = properties.getRateLimit();
        
        try {
            // 生成请求唯一标识
            String requestId = generateRequestId(request);
            
            Long result = redisTemplate.execute(SCRIPT,
                Collections.singletonList(key),
                config.getWindowSize(),
                config.getMaxRequests(),
                System.currentTimeMillis(),
                requestId
            );
            
            boolean allowed = result != null && result == 1L;
            
            if (!allowed) {
                log.debug("Rate limit exceeded for target: {}, key: {}", request.getTarget(), key);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("Redis sliding window rate limiter error for key: {}", key, e);
            // 发生异常时根据配置决定是否允许通过
            return properties.getRateLimit().isFailOpen();
        }
    }
    
    @Override
    public void record(SendRequest request) {
        // 滑动窗口算法在 allow() 方法中已经记录了请求，这里不需要额外操作
        // 但可以记录一些统计信息
        if (log.isDebugEnabled()) {
            log.debug("Recorded sliding window usage for target: {}", request.getTarget());
        }
    }
    
    /**
     * 构建 Redis key
     * 
     * @param request 发送请求
     * @return Redis key
     */
    private String buildKey(SendRequest request) {
        String prefix = properties.getRateLimit().getKeyPrefix();
        String target = request.getTarget();
        return prefix + ":sliding_window:" + target;
    }
    
    /**
     * 生成请求唯一标识
     * 
     * @param request 发送请求
     * @return 请求唯一标识
     */
    private String generateRequestId(SendRequest request) {
        // 使用请求ID + 时间戳确保唯一性
        return request.getRequestId() + ":" + System.nanoTime();
    }
}