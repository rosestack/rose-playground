
package io.github.rose.core.exception;

import java.io.Serial;

/**
 * 限流异常
 * 继承BusinessException，直接使用父类构造器
 */
public class RateLimitException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 简单消息构造器（不需要国际化）
     *
     * @param message 错误消息
     */
    public RateLimitException(String message) {
        super(message);
    }

    /**
     * 简单消息构造器（不需要国际化，带异常原因）
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 国际化构造器
     *
     * @param messageCode 国际化消息编码
     * @param messageArgs 消息参数
     */
    public RateLimitException(String messageCode, Object[] messageArgs) {
        super(messageCode, messageArgs);
    }

    /**
     * 国际化构造器（带默认消息）
     *
     * @param messageCode    国际化消息编码
     * @param defaultMessage 默认错误消息
     * @param messageArgs    消息参数
     */
    public RateLimitException(String messageCode, String defaultMessage, Object[] messageArgs) {
        super(messageCode, defaultMessage, messageArgs);
    }

}
