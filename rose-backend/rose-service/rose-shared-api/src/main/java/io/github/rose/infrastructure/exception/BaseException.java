package io.github.rose.infrastructure.exception;

/**
 * 异常基类
 * <p>
 * 提供异常的基础功能，包括国际化消息键和消息参数的支持。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>支持国际化消息键</li>
 *   <li>支持消息参数</li>
 *   <li>提供统一的异常处理基础</li>
 * </ul>
 *
 * @author chensoul
 * @since 1.0.0
 */
public abstract class BaseException extends RuntimeException {

    /** 国际化消息键 */
    private final String messageKey;
    
    /** 消息参数 */
    private final Object[] messageArgs;

    /**
     * 构造基础异常
     *
     * @param messageKey 国际化消息键
     */
    protected BaseException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = new Object[0];
    }

    /**
     * 构造基础异常
     *
     * @param messageKey 国际化消息键
     * @param cause 原始异常
     */
    protected BaseException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.messageArgs = new Object[0];
    }

    /**
     * 构造基础异常
     *
     * @param messageKey 国际化消息键
     * @param messageArgs 消息参数
     */
    protected BaseException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs != null ? messageArgs : new Object[0];
    }

    /**
     * 构造基础异常
     *
     * @param messageKey 国际化消息键
     * @param cause 原始异常
     * @param messageArgs 消息参数
     */
    protected BaseException(String messageKey, Throwable cause, Object... messageArgs) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs != null ? messageArgs : new Object[0];
    }

    /**
     * 获取国际化消息键
     *
     * @return 消息键
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * 获取消息参数
     *
     * @return 消息参数数组
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }
}