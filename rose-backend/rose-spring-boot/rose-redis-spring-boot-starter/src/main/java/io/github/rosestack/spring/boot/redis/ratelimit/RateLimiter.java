package io.github.rosestack.spring.boot.redis.ratelimit;

/**
 * 限流器接口
 * <p>
 * 定义限流器的基本操作，支持多种限流算法的统一接口。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface RateLimiter {

    /**
     * 尝试获取许可
     *
     * @param key 限流键
     * @return 是否获取成功
     */
    boolean tryAcquire(String key);

    /**
     * 尝试获取指定数量的许可
     *
     * @param key     限流键
     * @param permits 许可数量
     * @return 是否获取成功
     */
    boolean tryAcquire(String key, int permits);

    /**
     * 获取当前可用的许可数量
     *
     * @param key 限流键
     * @return 可用许可数量
     */
    long getAvailablePermits(String key);

    /**
     * 获取限流器类型
     *
     * @return 限流器类型
     */
    String getType();

    /**
     * 重置限流状态
     *
     * @param key 限流键
     */
    void reset(String key);

    /**
     * 获取限流配置信息
     *
     * @param key 限流键
     * @return 配置信息
     */
    RateLimitInfo getInfo(String key);

    /**
     * 限流信息
     */
    class RateLimitInfo {
        private final String key;
        private final String type;
        private final int rate;
        private final int timeWindow;
        private final long availablePermits;
        private final long totalRequests;
        private final long rejectedRequests;

        public RateLimitInfo(String key, String type, int rate, int timeWindow, 
                           long availablePermits, long totalRequests, long rejectedRequests) {
            this.key = key;
            this.type = type;
            this.rate = rate;
            this.timeWindow = timeWindow;
            this.availablePermits = availablePermits;
            this.totalRequests = totalRequests;
            this.rejectedRequests = rejectedRequests;
        }

        public String getKey() { return key; }
        public String getType() { return type; }
        public int getRate() { return rate; }
        public int getTimeWindow() { return timeWindow; }
        public long getAvailablePermits() { return availablePermits; }
        public long getTotalRequests() { return totalRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
        public double getSuccessRate() { 
            return totalRequests > 0 ? (double)(totalRequests - rejectedRequests) / totalRequests : 1.0; 
        }
    }
}