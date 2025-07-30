package io.github.rosestack.redis.annotation;

import io.github.rosestack.redis.config.RoseRedisProperties;

import java.lang.annotation.*;

/**
 * 限流注解
 * <p>
 * 用于方法级别的限流控制。支持多种限流算法、动态限流键、灵活的配置等。
 * 可以通过 SpEL 表达式动态生成限流键名称。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {

    /**
     * 限流键名称
     * <p>
     * 支持 SpEL 表达式，可以使用方法参数、返回值等。
     * 例如：'user:' + #userId 或 'api:' + #request.path
     * </p>
     *
     * @return 限流键名称
     */
    String value();

    /**
     * 限流键名称（别名）
     * <p>
     * 与 value() 作用相同，提供更语义化的属性名。
     * </p>
     *
     * @return 限流键名称
     */
    String key() default "";

    /**
     * 限流速率（每秒允许的请求数）
     * <p>
     * 设置为 -1 表示使用默认配置。
     * </p>
     *
     * @return 限流速率
     */
    int rate() default -1;

    /**
     * 时间窗口大小（秒）
     * <p>
     * 用于滑动窗口算法。设置为 -1 表示使用默认配置。
     * </p>
     *
     * @return 时间窗口大小
     */
    int timeWindow() default -1;

    /**
     * 令牌桶容量
     * <p>
     * 用于令牌桶算法。设置为 -1 表示使用默认配置（通常为 rate 的 2 倍）。
     * </p>
     *
     * @return 令牌桶容量
     */
    int capacity() default -1;

    /**
     * 限流算法
     *
     * @return 限流算法
     */
    RoseRedisProperties.RateLimit.Algorithm algorithm() default RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET;

    /**
     * 超出限流时的处理策略
     *
     * @return 处理策略
     */
    FailStrategy failStrategy() default FailStrategy.EXCEPTION;

    /**
     * 超出限流时的自定义异常类
     * <p>
     * 当 failStrategy 为 CUSTOM_EXCEPTION 时使用。
     * 异常类必须有一个接受 String 参数的构造函数。
     * </p>
     *
     * @return 异常类
     */
    Class<? extends RuntimeException> customException() default RuntimeException.class;

    /**
     * 超出限流时的错误消息
     *
     * @return 错误消息
     */
    String failMessage() default "请求过于频繁，请稍后再试";

    /**
     * 限流作用域
     * <p>
     * 用于区分不同业务场景的限流。
     * </p>
     *
     * @return 限流作用域
     */
    String scope() default "";

    /**
     * 是否启用限流
     * <p>
     * 可以用于动态开关限流功能。
     * </p>
     *
     * @return 是否启用
     */
    boolean enabled() default true;

    /**
     * 超出限流时的处理策略枚举
     */
    enum FailStrategy {
        /**
         * 抛出异常（默认）
         */
        EXCEPTION,

        /**
         * 返回 null
         */
        RETURN_NULL,

        /**
         * 跳过方法执行，返回默认值
         */
        SKIP,

        /**
         * 抛出自定义异常
         */
        CUSTOM_EXCEPTION,

        /**
         * 记录日志并继续执行
         */
        LOG_AND_CONTINUE
    }
}