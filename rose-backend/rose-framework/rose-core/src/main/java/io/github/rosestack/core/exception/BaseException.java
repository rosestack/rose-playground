package io.github.rosestack.core.exception;

import java.io.Serial;

/**
 * 基础异常类
 * <p>
 * 提供国际化消息支持和异常上下文信息
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public abstract class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

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