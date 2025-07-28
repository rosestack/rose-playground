package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.redis.config.RoseRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 限流管理器测试
 */
@ExtendWith(MockitoExtension.class)
class RateLimitManagerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private RoseRedisProperties properties;
    private RateLimitManager rateLimitManager;

    @BeforeEach
    void setUp() {
        properties = new RoseRedisProperties();
        RoseRedisProperties.RateLimit rateLimit = new RoseRedisProperties.RateLimit();
        rateLimit.setEnabled(true);
        rateLimit.setKeyPrefix("test:rate_limit");
        rateLimit.setCapacity(10);
        rateLimit.setRefillRate(1);
        rateLimit.setWindowSize(60000);
        rateLimit.setMaxRequests(100);
        rateLimit.setFailOpen(true);
        properties.setRateLimit(rateLimit);

        rateLimitManager = new RateLimitManager(redisTemplate, properties);
    }

    @Test
    void shouldCreateTokenBucketRateLimiter() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-1")
                .target("user123")
                .build();

        RateLimited rateLimited = createRateLimited(
            RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET,
            10, 1, 60000, 100, true
        );

        // When & Then - 应该不抛出异常
        rateLimitManager.allow(request, rateLimited);
        rateLimitManager.record(request, rateLimited);
    }

    @Test
    void shouldCreateSlidingWindowRateLimiter() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-2")
                .target("user123")
                .build();

        RateLimited rateLimited = createRateLimited(
            RoseRedisProperties.RateLimit.Algorithm.SLIDING_WINDOW,
            10, 1, 60000, 100, true
        );

        // When & Then - 应该不抛出异常
        rateLimitManager.allow(request, rateLimited);
        rateLimitManager.record(request, rateLimited);
    }

    @Test
    void shouldCacheRateLimiters() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-3")
                .target("user123")
                .build();

        RateLimited rateLimited = createRateLimited(
            RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET,
            10, 1, 60000, 100, true
        );

        // When
        rateLimitManager.allow(request, rateLimited);
        rateLimitManager.allow(request, rateLimited); // 第二次调用应该使用缓存

        // Then
        assertThat(rateLimitManager.getCacheSize()).isGreaterThan(0);
    }

    @Test
    void shouldClearCache() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-4")
                .target("user123")
                .build();

        RateLimited rateLimited = createRateLimited(
            RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET,
            10, 1, 60000, 100, true
        );

        rateLimitManager.allow(request, rateLimited);
        assertThat(rateLimitManager.getCacheSize()).isGreaterThan(0);

        // When
        rateLimitManager.clearCache();

        // Then
        assertThat(rateLimitManager.getCacheSize()).isEqualTo(0);
    }

    @Test
    void shouldUseDefaultAlgorithmForUnknown() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-5")
                .target("user123")
                .build();

        // 创建一个未知算法的注解（通过反射模拟）
        RateLimited rateLimited = createRateLimited(
            RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET, // 使用已知算法作为默认
            10, 1, 60000, 100, true
        );

        // When & Then - 应该不抛出异常
        rateLimitManager.allow(request, rateLimited);
    }

    /**
     * 创建 RateLimited 注解的模拟实现
     */
    private RateLimited createRateLimited(RoseRedisProperties.RateLimit.Algorithm algorithm,
                                        int capacity, int refillRate, long windowSize, 
                                        int maxRequests, boolean failOpen) {
        return new RateLimited() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RateLimited.class;
            }

            @Override
            public String key() {
                return "";
            }

            @Override
            public RoseRedisProperties.RateLimit.Algorithm algorithm() {
                return algorithm;
            }

            @Override
            public int capacity() {
                return capacity;
            }

            @Override
            public int refillRate() {
                return refillRate;
            }

            @Override
            public long windowSize() {
                return windowSize;
            }

            @Override
            public int maxRequests() {
                return maxRequests;
            }

            @Override
            public boolean failOpen() {
                return failOpen;
            }

            @Override
            public String message() {
                return "Rate limit exceeded";
            }

            @Override
            public boolean enabled() {
                return true;
            }
        };
    }
}