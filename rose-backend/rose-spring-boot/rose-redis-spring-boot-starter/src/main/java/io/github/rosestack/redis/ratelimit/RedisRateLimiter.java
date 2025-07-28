package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.redis.config.RoseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 基于 Redis 的令牌桶限流器实现
 * 
 * <p>使用 Redis Hash 存储令牌桶状态，通过 Lua 脚本确保原子性操作。
 * 支持按目标（target）进行独立限流，每个目标维护独立的令牌桶。
 * 
 * <p>令牌桶算法特点：
 * <ul>
 *   <li>允许突发流量：桶中有足够令牌时可以立即处理多个请求</li>
 *   <li>平滑限流：令牌以固定速率补充，长期平均速率受限</li>
 *   <li>容量控制：桶容量限制了最大突发请求数</li>
 * </ul>
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseRedisProperties properties;
    
    /**
     * 令牌桶算法 Lua 脚本
     * 
     * KEYS[1]: 令牌桶的 Redis key
     * ARGV[1]: 桶容量（最大令牌数）
     * ARGV[2]: 补充速率（每秒补充的令牌数）
     * ARGV[3]: 请求的令牌数（通常为1）
     * ARGV[4]: 当前时间戳（毫秒）
     * 
     * 返回值：1表示允许，0表示拒绝
     */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refill_rate = tonumber(ARGV[2])
            local requested = tonumber(ARGV[3])
            local now = tonumber(ARGV[4])
            
            -- 获取当前桶状态
            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local tokens = tonumber(bucket[1]) or capacity
            local last_refill = tonumber(bucket[2]) or now
            
            -- 计算需要补充的令牌数
            local time_passed = math.max(0, now - last_refill)
            local tokens_to_add = math.floor(time_passed / 1000 * refill_rate)
            tokens = math.min(capacity, tokens + tokens_to_add)
            
            -- 检查是否有足够的令牌
            if tokens >= requested then
                tokens = tokens - requested
                -- 更新桶状态
                redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
                redis.call('EXPIRE', key, 3600)  -- 1小时过期
                return 1
            else
                -- 即使拒绝也要更新时间戳，避免时间漂移
                redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
                redis.call('EXPIRE', key, 3600)
                return 0
            end
            """;
    
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, Long.class);
    
    @Override
    public boolean allow(SendRequest request) {
        if (!properties.getRateLimit().isEnabled()) {
            return true;
        }
        
        String key = buildKey(request);
        RoseRedisProperties.RateLimit config = properties.getRateLimit();
        
        try {
            Long result = redisTemplate.execute(SCRIPT, 
                Collections.singletonList(key),
                config.getCapacity(),
                config.getRefillRate(),
                1, // 请求1个令牌
                System.currentTimeMillis()
            );
            
            boolean allowed = result != null && result == 1L;
            
            if (!allowed) {
                log.debug("Rate limit exceeded for target: {}, key: {}", request.getTarget(), key);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("Redis rate limiter error for key: {}", key, e);
            // 发生异常时根据配置决定是否允许通过
            return properties.getRateLimit().isFailOpen();
        }
    }
    
    @Override
    public void record(SendRequest request) {
        // 令牌桶算法在 allow() 方法中已经消费了令牌，这里不需要额外操作
        // 但可以记录一些统计信息
        if (log.isDebugEnabled()) {
            log.debug("Recorded rate limit usage for target: {}", request.getTarget());
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
        return prefix + ":token_bucket:" + target;
    }
}