package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.redis.config.RoseRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Redis 滑动窗口限流器测试
 */
@ExtendWith(MockitoExtension.class)
class SlidingWindowRateLimiterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private RoseRedisProperties properties;
    private SlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        properties = new RoseRedisProperties();
        RoseRedisProperties.RateLimit rateLimit = new RoseRedisProperties.RateLimit();
        rateLimit.setEnabled(true);
        rateLimit.setKeyPrefix("test:rate_limit");
        rateLimit.setWindowSize(60000); // 1分钟
        rateLimit.setMaxRequests(100);
        rateLimit.setFailOpen(true);
        properties.setRateLimit(rateLimit);

        rateLimiter = new SlidingWindowRateLimiter(redisTemplate, properties);
    }

    @Test
    void shouldAllowRequestWhenWithinLimit() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-1")
                .target("user123")
                .build();

        // Mock Redis 返回成功（在限制范围内）
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(1L);

        // When
        boolean allowed = rateLimiter.allow(request);

        // Then
        assertThat(allowed).isTrue();
    }

    @Test
    void shouldRejectRequestWhenExceedsLimit() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-2")
                .target("user123")
                .build();

        // Mock Redis 返回失败（超过限制）
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(0L);

        // When
        boolean allowed = rateLimiter.allow(request);

        // Then
        assertThat(allowed).isFalse();
    }

    @Test
    void shouldAllowRequestWhenRedisFailsAndFailOpenEnabled() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-3")
                .target("user123")
                .build();

        // Mock Redis 抛出异常
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        boolean allowed = rateLimiter.allow(request);

        // Then
        assertThat(allowed).isTrue(); // fail-open 模式下应该允许通过
    }

    @Test
    void shouldRejectRequestWhenRedisFailsAndFailOpenDisabled() {
        // Given
        properties.getRateLimit().setFailOpen(false);
        rateLimiter = new SlidingWindowRateLimiter(redisTemplate, properties);

        SendRequest request = SendRequest.builder()
                .requestId("test-request-4")
                .target("user123")
                .build();

        // Mock Redis 抛出异常
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        boolean allowed = rateLimiter.allow(request);

        // Then
        assertThat(allowed).isFalse(); // fail-close 模式下应该拒绝
    }

    @Test
    void shouldSkipLimitingWhenDisabled() {
        // Given
        properties.getRateLimit().setEnabled(false);
        rateLimiter = new SlidingWindowRateLimiter(redisTemplate, properties);

        SendRequest request = SendRequest.builder()
                .requestId("test-request-5")
                .target("user123")
                .build();

        // When
        boolean allowed = rateLimiter.allow(request);

        // Then
        assertThat(allowed).isTrue(); // 禁用时应该直接允许
    }

    @Test
    void shouldRecordSuccessfully() {
        // Given
        SendRequest request = SendRequest.builder()
                .requestId("test-request-6")
                .target("user123")
                .build();

        // When & Then - 应该不抛出异常
        rateLimiter.record(request);
    }
}