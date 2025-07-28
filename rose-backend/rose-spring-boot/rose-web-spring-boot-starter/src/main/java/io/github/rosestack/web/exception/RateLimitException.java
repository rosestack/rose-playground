package io.github.rosestack.web.exception;

/**
 * 限流异常类
 * <p>
 * 用于表示请求频率超限，触发限流保护
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public class RateLimitException extends BaseException {

    /**
     * 构造限流异常
     *
     * @param messageKey 国际化消息键
     */
    public RateLimitException(String messageKey) {
        super(messageKey);
    }

    /**
     * 构造限流异常
     *
     * @param messageKey 国际化消息键
     * @param cause 原始异常
     */
    public RateLimitException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    /**
     * 构造限流异常
     *
     * @param messageKey 国际化消息键
     * @param args 消息参数
     */
    public RateLimitException(String messageKey, Object... args) {
        super(messageKey, args);
    }

    /**
     * 构造限流异常
     *
     * @param messageKey 国际化消息键
     * @param cause 原始异常
     * @param args 消息参数
     */
    public RateLimitException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }
} 