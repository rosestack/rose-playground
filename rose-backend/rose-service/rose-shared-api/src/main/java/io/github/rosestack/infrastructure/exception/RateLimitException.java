package io.github.rosestack.infrastructure.exception;

/**
 * 限流异常类
 * <p>
 * 用于表示请求频率超限的异常，如API调用频率超限、并发请求过多等。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>错误码：429</li>
 *   <li>可重试（延迟后）</li>
 *   <li>支持国际化消息</li>
 * </ul>
 *
 * @author chensoul
 * @since 1.0.0
 */
public class RateLimitException extends BaseException {

    /**
     * 重试间隔（秒）
     */
    private final int retryAfterSeconds;

    /**
     * 构造限流异常
     *
     * @param messageKey 国际化消息键
     */
    public RateLimitException(String messageKey) {
        this(messageKey, 60); // 默认60秒后重试
    }

    /**
     * 构造限流异常
     *
     * @param messageKey        国际化消息键
     * @param retryAfterSeconds 重试间隔（秒）
     */
    public RateLimitException(String messageKey, int retryAfterSeconds) {
        super(messageKey);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 构造限流异常
     *
     * @param messageKey        国际化消息键
     * @param retryAfterSeconds 重试间隔（秒）
     * @param cause             原始异常
     */
    public RateLimitException(String messageKey, int retryAfterSeconds, Throwable cause) {
        super(messageKey, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 构造限流异常
     *
     * @param messageKey        国际化消息键
     * @param retryAfterSeconds 重试间隔（秒）
     * @param args              消息参数
     */
    public RateLimitException(String messageKey, int retryAfterSeconds, Object... args) {
        super(messageKey, args);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 构造限流异常
     *
     * @param messageKey        国际化消息键
     * @param retryAfterSeconds 重试间隔（秒）
     * @param cause             原始异常
     * @param args              消息参数
     */
    public RateLimitException(String messageKey, int retryAfterSeconds, Throwable cause, Object... args) {
        super(messageKey, cause, args);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 获取重试间隔
     *
     * @return 重试间隔（秒）
     */
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}