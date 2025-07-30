package io.github.rosestack.redis.exception;

/**
 * 限流超出异常
 * <p>
 * 当请求超出限流阈值时抛出此异常。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class RateLimitExceededException extends RuntimeException {

    private final String rateLimitKey;
    private final int currentRate;
    private final int maxRate;

    public RateLimitExceededException(String rateLimitKey, int currentRate, int maxRate) {
        super(String.format("限流超出: key=%s, 当前速率=%d, 最大速率=%d", rateLimitKey, currentRate, maxRate));
        this.rateLimitKey = rateLimitKey;
        this.currentRate = currentRate;
        this.maxRate = maxRate;
    }

    public RateLimitExceededException(String message) {
        super(message);
        this.rateLimitKey = null;
        this.currentRate = 0;
        this.maxRate = 0;
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.rateLimitKey = null;
        this.currentRate = 0;
        this.maxRate = 0;
    }

    public String getRateLimitKey() {
        return rateLimitKey;
    }

    public int getCurrentRate() {
        return currentRate;
    }

    public int getMaxRate() {
        return maxRate;
    }
}