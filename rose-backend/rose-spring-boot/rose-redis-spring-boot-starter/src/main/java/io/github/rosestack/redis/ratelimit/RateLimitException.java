package io.github.rosestack.redis.ratelimit;

/**
 * 限流异常
 * 
 * <p>当请求超过限流阈值时抛出此异常。
 * 
 * @author chensoul
 * @since 1.0.0
 */
public class RateLimitException extends RuntimeException {
    
    private final String key;
    private final String algorithm;
    
    public RateLimitException(String message) {
        super(message);
        this.key = null;
        this.algorithm = null;
    }
    
    public RateLimitException(String message, String key, String algorithm) {
        super(message);
        this.key = key;
        this.algorithm = algorithm;
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
        this.key = null;
        this.algorithm = null;
    }
    
    public RateLimitException(String message, String key, String algorithm, Throwable cause) {
        super(message, cause);
        this.key = key;
        this.algorithm = algorithm;
    }
    
    /**
     * 获取限流 key
     * 
     * @return 限流 key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * 获取限流算法
     * 
     * @return 限流算法
     */
    public String getAlgorithm() {
        return algorithm;
    }
}