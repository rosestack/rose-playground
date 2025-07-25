package io.github.rosestack.infrastructure.exception;

/**
 * 业务异常类
 * <p>
 * 用于表示业务规则违反或业务流程异常，如用户不存在、订单状态无效等。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>错误码范围：1000+</li>
 *   <li>不可重试</li>
 *   <li>支持国际化消息</li>
 * </ul>
 *
 * @author chensoul
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