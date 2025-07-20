package io.github.rose.core.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 业务异常类
 * <p>
 * 设计原则：
 * 1. 不处理国际化逻辑，只携带异常信息
 * 2. 明确区分简单消息和国际化消息
 * 3. 使用静态工厂方法避免构造器歧义
 * 4. 提供清晰的API语义
 */
@Getter
@Setter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 国际化消息编码（可选）
     * 如果为空，表示不需要国际化处理
     */
    private String messageCode;

    /**
     * 国际化消息参数（可选）
     * 用于替换消息模板中的占位符
     */
    private Object[] messageArgs;

    /**
     * 默认错误消息
     * 当国际化处理失败或不需要国际化时使用
     */
    private String defaultMessage;

    /**
     * 是否需要国际化处理
     */
    private boolean needsInternationalization;

    // ========== 公共构造器 ==========

    /**
     * 简单消息构造器（不需要国际化）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(null, message, null, null, false);
    }

    /**
     * 简单消息构造器（不需要国际化，带异常原因）
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public BusinessException(String message, Throwable cause) {
        this(null, message, null, cause, false);
    }

    /**
     * 国际化构造器
     *
     * @param messageCode 国际化消息编码
     * @param messageArgs 消息参数
     */
    public BusinessException(String messageCode, Object[] messageArgs) {
        this(messageCode, null, messageArgs, null, true);
    }

    /**
     * 国际化构造器（带默认消息）
     *
     * @param messageCode    国际化消息编码
     * @param defaultMessage 默认错误消息
     * @param messageArgs    消息参数
     */
    public BusinessException(String messageCode, String defaultMessage, Object[]
            messageArgs) {
        this(messageCode, defaultMessage, messageArgs, null, true);
    }

    /**
     * 完整构造器（受保护，供子类使用）
     *
     * @param messageCode               国际化消息编码
     * @param defaultMessage            默认错误消息
     * @param messageArgs               消息参数
     * @param cause                     异常原因
     * @param needsInternationalization 是否需要国际化
     */
    protected BusinessException(String messageCode, String defaultMessage, Object[] messageArgs,
                                Throwable cause, boolean needsInternationalization) {
        super(defaultMessage != null ? defaultMessage : messageCode, cause);
        this.messageCode = messageCode;
        this.defaultMessage = defaultMessage;
        this.messageArgs = messageArgs;
        this.needsInternationalization = needsInternationalization;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建简单的业务异常（不需要国际化）
     *
     * @param message 错误消息
     * @return BusinessException实例
     */
    public static BusinessException of(String message) {
        return new BusinessException(null, message, null, null, false);
    }

    /**
     * 创建简单的业务异常（不需要国际化，带异常原因）
     *
     * @param message 错误消息
     * @param cause   异常原因
     * @return BusinessException实例
     */
    public static BusinessException of(String message, Throwable cause) {
        return new BusinessException(null, message, null, cause, false);
    }

    /**
     * 创建国际化业务异常
     *
     * @param messageCode 国际化消息编码
     * @param messageArgs 消息参数
     * @return BusinessException实例
     */
    public static BusinessException i18n(String messageCode, Object[] messageArgs) {
        return new BusinessException(messageCode, null, messageArgs, null, true);
    }

    /**
     * 创建国际化业务异常（带默认消息）
     *
     * @param messageCode    国际化消息编码
     * @param defaultMessage 默认错误消息
     * @param messageArgs    消息参数
     * @return BusinessException实例
     */
    public static BusinessException i18n(String messageCode, String defaultMessage, Object[] messageArgs) {
        return new BusinessException(messageCode, defaultMessage, messageArgs, null, true);
    }

    // ========== 重写父类方法 ==========

    /**
     * 获取异常消息
     * 优先返回defaultMessage，如果为空则返回messageCode
     */
    @Override
    public String getMessage() {
        if (defaultMessage != null) {
            return defaultMessage;
        }
        if (messageCode != null) {
            return messageCode;
        }
        return super.getMessage();
    }

    /**
     * 获取本地化消息（与getMessage相同）
     */
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    // ========== 工具方法 ==========

    /**
     * 判断是否有消息参数
     */
    public boolean hasMessageArgs() {
        return messageArgs != null && messageArgs.length > 0;
    }

    /**
     * 获取消息参数数量
     */
    public int getMessageArgsCount() {
        return messageArgs != null ? messageArgs.length : 0;
    }

    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(": ");

        if (needsInternationalization) {
            sb.append("messageCode=").append(messageCode);
            if (hasMessageArgs()) {
                sb.append(", args=").append(java.util.Arrays.toString(messageArgs));
            }
            if (defaultMessage != null) {
                sb.append(", defaultMessage=").append(defaultMessage);
            }
        } else {
            sb.append(getMessage());
        }

        return sb.toString();
    }
}