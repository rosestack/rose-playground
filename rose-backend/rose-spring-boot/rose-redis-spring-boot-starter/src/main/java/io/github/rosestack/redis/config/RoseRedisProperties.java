package io.github.rosestack.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rose Redis 配置属性
 * <p>
 * 提供 Redis 增强功能的配置选项，包括分布式锁、缓存、限流、消息队列、会话管理等。
 * 优先使用 Spring Boot 原生 Redis 配置，本配置类仅提供 Rose 特有的扩展配置。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.redis")
public class RoseRedisProperties {

    /**
     * 是否启用 Rose Redis 增强功能
     */
    private boolean enabled = true;

    /**
     * 分布式锁配置
     */
    private Lock lock = new Lock();

    /**
     * 缓存增强配置
     */
    private Cache cache = new Cache();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 消息队列配置
     */
    private Message message = new Message();

    /**
     * 会话管理配置
     */
    private Session session = new Session();

    /**
     * 数据结构操作配置
     */
    private DataStructure dataStructure = new DataStructure();

    /**
     * 分布式锁配置
     */
    @Data
    public static class Lock {
        /**
         * 是否启用分布式锁
         */
        private boolean enabled = true;

        /**
         * 默认锁超时时间（毫秒）
         */
        private long defaultTimeout = 30000L;

        /**
         * 是否启用自动续期
         */
        private boolean autoRenewal = true;

        /**
         * 续期间隔时间（毫秒）
         */
        private long renewalInterval = 10000L;

        /**
         * 锁 key 前缀
         */
        private String keyPrefix = "rose:lock:";

        /**
         * 最大等待时间（毫秒）
         */
        private long maxWaitTime = 5000L;
    }

    /**
     * 缓存增强配置
     */
    @Data
    public static class Cache {
        /**
         * 是否启用缓存增强功能
         */
        private boolean enabled = true;

        /**
         * 是否启用多级缓存
         */
        private boolean multiLevel = true;

        /**
         * 是否启用缓存穿透防护
         */
        private boolean penetrationProtection = true;

        /**
         * 是否启用缓存预热
         */
        private boolean warmupEnabled = false;

        /**
         * 缓存 key 前缀
         */
        private String keyPrefix = "rose:cache:";

        /**
         * 布隆过滤器预期插入数量
         */
        private long bloomFilterExpectedInsertions = 1000000L;

        /**
         * 布隆过滤器误判率
         */
        private double bloomFilterFalsePositiveProbability = 0.01;
    }

    /**
     * 限流配置
     */
    @Data
    public static class RateLimit {
        /**
         * 是否启用限流功能
         */
        private boolean enabled = true;

        /**
         * 默认限流算法
         */
        private Algorithm defaultAlgorithm = Algorithm.TOKEN_BUCKET;

        /**
         * 默认限流速率（每秒请求数）
         */
        private int defaultRate = 100;

        /**
         * 默认时间窗口（秒）
         */
        private int defaultTimeWindow = 60;

        /**
         * 限流 key 前缀
         */
        private String keyPrefix = "rose:rate-limit:";

        /**
         * 限流算法枚举
         */
        public enum Algorithm {
            /**
             * 令牌桶算法
             */
            TOKEN_BUCKET,

            /**
             * 滑动窗口算法
             */
            SLIDING_WINDOW,

            /**
             * 固定窗口算法
             */
            FIXED_WINDOW
        }
    }

    /**
     * 消息队列配置
     */
    @Data
    public static class Message {
        /**
         * 是否启用消息队列功能
         */
        private boolean enabled = true;

        /**
         * 是否启用 Redis Stream
         */
        private boolean streamEnabled = true;

        /**
         * 是否启用延迟消息
         */
        private boolean delayedEnabled = true;

        /**
         * 消息 key 前缀
         */
        private String keyPrefix = "rose:msg:";

        /**
         * 默认消费者组名
         */
        private String defaultConsumerGroup = "rose-group";

        /**
         * 死信队列 key 前缀
         */
        private String deadLetterPrefix = "rose:dlq:";

        /**
         * 最大重试次数
         */
        private int maxRetryCount = 3;
    }

    /**
     * 会话管理配置
     */
    @Data
    public static class Session {
        /**
         * 是否启用会话管理
         */
        private boolean enabled = true;

        /**
         * 会话超时时间（秒）
         */
        private int timeout = 1800;

        /**
         * 会话 key 前缀
         */
        private String keyPrefix = "rose:session:";

        /**
         * 是否启用会话事件监听
         */
        private boolean eventListenerEnabled = true;
    }

    /**
     * 数据结构操作配置
     */
    @Data
    public static class DataStructure {
        /**
         * 是否启用数据结构操作工具
         */
        private boolean enabled = true;

        /**
         * 默认批量操作大小
         */
        private int defaultBatchSize = 1000;

        /**
         * 操作超时时间（毫秒）
         */
        private long operationTimeout = 5000L;
    }
}