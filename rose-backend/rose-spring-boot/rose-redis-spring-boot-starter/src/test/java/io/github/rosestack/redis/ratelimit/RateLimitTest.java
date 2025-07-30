package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.redis.config.RoseRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 限流功能测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RateLimitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private RoseRedisProperties properties;
    private RateLimitManager rateLimitManager;

    @BeforeEach
    void setUp() {
        properties = new RoseRedisProperties();
        properties.getRateLimit().setKeyPrefix("test:rate-limit:");
        properties.getRateLimit().setDefaultRate(10);
        properties.getRateLimit().setDefaultTimeWindow(60);
        properties.getRateLimit().setDefaultAlgorithm(RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET);
        
        rateLimitManager = new RateLimitManager(redisTemplate, properties);
    }

    @Test
    void testGetTokenBucketRateLimiter() {
        // 测试获取令牌桶限流器
        RateLimiter rateLimiter = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, 10, 60);
        
        assertNotNull(rateLimiter);
        assertEquals("TOKEN_BUCKET", rateLimiter.getType());
    }

    @Test
    void testGetSlidingWindowRateLimiter() {
        // 测试获取滑动窗口限流器
        RateLimiter rateLimiter = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.SLIDING_WINDOW, 10, 60);
        
        assertNotNull(rateLimiter);
        assertEquals("SLIDING_WINDOW", rateLimiter.getType());
    }

    @Test
    void testTokenBucketTryAcquireSuccess() {
        // 模拟令牌桶获取成功
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(1L, 9L)); // 获取成功，剩余9个令牌

        RateLimiter rateLimiter = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, 10, 60);
        
        boolean acquired = rateLimiter.tryAcquire("test-key");
        assertTrue(acquired);
    }

    @Test
    void testTokenBucketTryAcquireFailed() {
        // 模拟令牌桶获取失败
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(0L, 0L)); // 获取失败，没有令牌

        RateLimiter rateLimiter = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, 10, 60);
        
        boolean acquired = rateLimiter.tryAcquire("test-key");
        assertFalse(acquired);
    }

    @Test
    void testSlidingWindowTryAcquireSuccess() {
        // 模拟滑动窗口获取成功
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(1L, 5L, 5L)); // 获取成功，当前5个请求，剩余5个配额

        RateLimiter rateLimiter = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.SLIDING_WINDOW, 10, 60);
        
        boolean acquired = rateLimiter.tryAcquire("test-key");
        assertTrue(acquired);
    }

    @Test
    void testSlidingWindowTryAcquireFailed() {
        // 模拟滑动窗口获取失败
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(0L, 10L, 0L)); // 获取失败，已达上限

        RateLimiter rateLimiter = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.SLIDING_WINDOW, 10, 60);
        
        boolean acquired = rateLimiter.tryAcquire("test-key");
        assertFalse(acquired);
    }

    @Test
    void testRateLimitManagerTryAcquire() {
        // 测试管理器的 tryAcquire 方法
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(1L, 9L));

        boolean acquired = rateLimitManager.tryAcquire("test-key");
        assertTrue(acquired);
    }

    @Test
    void testRateLimitManagerWithCustomConfig() {
        // 测试自定义配置
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(1L, 19L));

        boolean acquired = rateLimitManager.tryAcquire("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, 20, 30);
        assertTrue(acquired);
    }

    @Test
    void testRateLimiterCaching() {
        // 测试限流器实例缓存
        RateLimiter rateLimiter1 = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, 10, 60);
        RateLimiter rateLimiter2 = rateLimitManager.getRateLimiter("test-key", 
                RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, 10, 60);
        
        assertSame(rateLimiter1, rateLimiter2); // 应该返回同一个实例
        assertEquals(1, rateLimitManager.getCachedRateLimiterCount());
    }

    @Test
    void testUnsupportedAlgorithm() {
        // 测试不支持的算法
        assertThrows(UnsupportedOperationException.class, () -> {
            rateLimitManager.getRateLimiter("test-key", 
                    RoseRedisProperties.RateLimit.Algorithm.FIXED_WINDOW, 10, 60);
        });
    }
}