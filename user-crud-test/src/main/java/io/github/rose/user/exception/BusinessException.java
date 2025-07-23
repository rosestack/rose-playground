package io.github.rose.user.exception;

/**
 * 业务异常类
 *
 * @author Chen Soul
 * @since 1.0.0
 */
public class BusinessException extends BaseException {

    /**
     * 构造业务异常
     *
     * @param messageKey 国际化消息键
     */
    public BusinessException(String messageKey) {
        super(messageKey);
    }

    /**
     * 构造业务异常
     *
     * @param messageKey 国际化消息键
     * @param cause 原始异常
     */
    public BusinessException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    /**
     * 构造业务异常
     *
     * @param messageKey 国际化消息键
     * @param args 消息参数
     */
    public BusinessException(String messageKey, Object... args) {
        super(messageKey, args);
    }

    /**
     * 构造业务异常
     *
     * @param messageKey 国际化消息键
     * @param cause 原始异常
     * @param args 消息参数
     */
    public BusinessException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }
}