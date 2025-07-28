package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.redis.config.RoseRedisProperties;

import java.lang.annotation.*;

/**
 * 限流注解
 * 
 * <p>用于方法级别的限流控制，支持多种限流算法和灵活的配置。
 * 可以基于方法参数、用户信息等动态生成限流 key。
 * 
 * <p>使用示例：
 * <pre>{@code
 * @RateLimited(key = "sendSms", capacity = 10, refillRate = 1)
 * public void sendSms(String phone) {
 *     // 发送短信逻辑
 * }
 * 
 * @RateLimited(key = "user:#{#userId}", algorithm = Algorithm.SLIDING_WINDOW, maxRequests = 100)
 * public void userAction(@Param("userId") String userId) {
 *     // 用户操作逻辑
 * }
 * }</pre>
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    
    /**
     * 限流 key，支持 SpEL 表达式
     * 
     * <p>可以使用以下变量：
     * <ul>
     *   <li>#methodName - 方法名</li>
     *   <li>#className - 类名</li>
     *   <li>#参数名 - 方法参数值</li>
     *   <li>#target - 目标对象</li>
     *   <li>#args - 参数数组</li>
     * </ul>
     * 
     * @return 限流 key
     */
    String key() default "";
    
    /**
     * 限流算法
     * 
     * @return 限流算法类型
     */
    RoseRedisProperties.RateLimit.Algorithm algorithm() default RoseRedisProperties.RateLimit.Algorithm.TOKEN_BUCKET;
    
    /**
     * 令牌桶容量（仅对令牌桶算法有效）
     * 
     * @return 桶容量
     */
    int capacity() default -1;
    
    /**
     * 令牌补充速率（仅对令牌桶算法有效）
     * 
     * @return 每秒补充的令牌数
     */
    int refillRate() default -1;
    
    /**
     * 滑动窗口大小，单位毫秒（仅对滑动窗口算法有效）
     * 
     * @return 窗口大小
     */
    long windowSize() default -1;
    
    /**
     * 滑动窗口内最大请求数（仅对滑动窗口算法有效）
     * 
     * @return 最大请求数
     */
    int maxRequests() default -1;
    
    /**
     * 异常时是否允许通过
     * 
     * @return true 表示异常时允许通过，false 表示异常时拒绝
     */
    boolean failOpen() default true;
    
    /**
     * 限流失败时的错误消息
     * 
     * @return 错误消息
     */
    String message() default "Rate limit exceeded";
    
    /**
     * 是否启用限流（可用于动态开关）
     * 
     * @return true 表示启用，false 表示禁用
     */
    boolean enabled() default true;
}