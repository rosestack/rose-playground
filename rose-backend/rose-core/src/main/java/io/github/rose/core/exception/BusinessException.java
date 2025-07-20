package io.github.rose.core.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 业务异常类，用于处理应用程序特定的错误情况。
 * <p>
 * 该异常类提供了一个全面的框架来处理业务逻辑错误，支持简单错误消息和国际化消息。
 * 它遵循清晰的关注点分离原则，通过携带异常信息而不直接执行国际化逻辑。
 *
 * <h3>设计原则：</h3>
 * <ul>
 *   <li><strong>信息载体：</strong> 携带异常信息而不处理国际化逻辑</li>
 *   <li><strong>清晰区分：</strong> 将简单消息与国际化消息分离</li>
 *   <li><strong>工厂方法：</strong> 使用静态工厂方法避免构造器歧义</li>
 *   <li><strong>清晰的API语义：</strong> 提供直观和自文档化的API方法</li>
 * </ul>
 *
 * <h3>使用模式：</h3>
 * <pre>{@code
 * // 简单消息（无国际化）
 * throw BusinessException.of("用户未找到");
 *
 * // 国际化消息
 * throw BusinessException.i18n("user.not.found", new Object[]{"john"});
 *
 * // 带回退的国际化消息
 * throw BusinessException.i18n("user.not.found", "用户未找到", new Object[]{"john"});
 * }</pre>
 *
 * <h3>国际化支持：</h3>
 * 异常支持两种模式：
 * <ul>
 *   <li><strong>简单模式：</strong> 直接消息，无国际化</li>
 *   <li><strong>国际化模式：</strong> 消息代码和参数用于国际化</li>
 * </ul>
 *
 * @author chensoul
 * @since 1.0.0
 * @see RuntimeException
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 国际化消息代码（可选）。
     * <p>
     * 当存在时，表示此异常应通过国际化系统处理。如果为null，异常使用简单消息模式。
     */
    private String messageCode;

    /**
     * 国际化消息参数（可选）。
     * <p>
     * 这些参数用于替换国际化消息模板中的占位符。仅在messageCode存在时相关。
     */
    private Object[] messageArgs;

    /**
     * 默认错误消息。
     * <p>
     * 当国际化处理失败或不需要国际化时用作回退。此消息也用作基础异常消息。
     */
    private String defaultMessage;

    /**
     * 标志，指示此异常是否需要国际化处理。
     * <p>
     * 当为true时，异常应通过国际化系统使用messageCode和messageArgs处理。
     * 当为false时，应直接使用defaultMessage。
     */
    private boolean needsInternationalization;

    // ==================== Public Constructors ====================

    /**
     * Creates a business exception with a simple message (no internationalization).
     * <p>
     * This constructor is used for exceptions that don't require internationalization
     * and have a fixed error message. The exception will operate in simple mode.
     *
     * @param message The error message to be displayed
     */
    public BusinessException(String message) {
        this(null, message, null, null, false);
    }

    /**
     * Creates a business exception with a simple message and cause (no internationalization).
     * <p>
     * This constructor is used for exceptions that don't require internationalization
     * but need to wrap another exception as the cause.
     *
     * @param message The error message to be displayed
     * @param cause   The underlying cause of this exception
     */
    public BusinessException(String message, Throwable cause) {
        this(null, message, null, cause, false);
    }

    /**
     * Creates an internationalized business exception with message code and arguments.
     * <p>
     * This constructor is used for exceptions that require internationalization.
     * The actual message will be resolved using the messageCode and messageArgs
     * through the internationalization system.
     *
     * @param messageCode The internationalization message code
     * @param messageArgs Arguments for message template substitution
     */
    public BusinessException(String messageCode, Object[] messageArgs) {
        this(messageCode, null, messageArgs, null, true);
    }

    /**
     * Creates an internationalized business exception with fallback message.
     * <p>
     * This constructor provides both internationalization support and a fallback
     * default message in case internationalization processing fails.
     *
     * @param messageCode    The internationalization message code
     * @param defaultMessage The default error message used as fallback
     * @param messageArgs    Arguments for message template substitution
     */
    public BusinessException(String messageCode, String defaultMessage, Object[]
            messageArgs) {
        this(messageCode, defaultMessage, messageArgs, null, true);
    }

    /**
     * Complete constructor for full control over exception properties (protected for subclasses).
     * <p>
     * This constructor provides complete control over all exception properties and is
     * primarily intended for use by subclasses or internal factory methods.
     *
     * @param messageCode               The internationalization message code (can be null)
     * @param defaultMessage            The default error message (can be null)
     * @param messageArgs               Arguments for message template substitution (can be null)
     * @param cause                     The underlying cause of this exception (can be null)
     * @param needsInternationalization Whether this exception requires i18n processing
     */
    protected BusinessException(String messageCode, String defaultMessage, Object[] messageArgs,
                                Throwable cause, boolean needsInternationalization) {
        super(defaultMessage != null ? defaultMessage : messageCode, cause);
        this.messageCode = messageCode;
        this.defaultMessage = defaultMessage;
        this.messageArgs = messageArgs;
        this.needsInternationalization = needsInternationalization;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a simple business exception without internationalization support.
     * <p>
     * This factory method provides a clean API for creating exceptions with simple
     * error messages that don't require internationalization processing.
     *
     * @param message The error message to be displayed
     * @return A new BusinessException instance in simple mode
     */
    public static BusinessException of(String message) {
        return new BusinessException(null, message, null, null, false);
    }

    /**
     * Creates a simple business exception with cause, without internationalization support.
     * <p>
     * This factory method is used when you need to wrap another exception while
     * providing a simple error message that doesn't require internationalization.
     *
     * @param message The error message to be displayed
     * @param cause   The underlying cause of this exception
     * @return A new BusinessException instance in simple mode with cause
     */
    public static BusinessException of(String message, Throwable cause) {
        return new BusinessException(null, message, null, cause, false);
    }

    /**
     * Creates an internationalized business exception with message code and arguments.
     * <p>
     * This factory method provides a clean API for creating exceptions that require
     * internationalization processing. The actual message will be resolved through
     * the internationalization system.
     *
     * @param messageCode The internationalization message code
     * @param messageArgs Arguments for message template substitution
     * @return A new BusinessException instance in internationalization mode
     */
    public static BusinessException i18n(String messageCode, Object[] messageArgs) {
        return new BusinessException(messageCode, null, messageArgs, null, true);
    }

    /**
     * Creates an internationalized business exception with fallback message.
     * <p>
     * This factory method provides both internationalization support and a fallback
     * mechanism. If internationalization processing fails, the defaultMessage will
     * be used instead.
     *
     * @param messageCode    The internationalization message code
     * @param defaultMessage The default error message used as fallback
     * @param messageArgs    Arguments for message template substitution
     * @return A new BusinessException instance in internationalization mode with fallback
     */
    public static BusinessException i18n(String messageCode, String defaultMessage, Object[] messageArgs) {
        return new BusinessException(messageCode, defaultMessage, messageArgs, null, true);
    }

    // ==================== Overridden Methods ====================

    /**
     * Returns the exception message with priority-based selection.
     * <p>
     * The message selection follows this priority:
     * 1. defaultMessage (if present)
     * 2. messageCode (if present)
     * 3. super.getMessage() (fallback)
     * <p>
     * This ensures that there's always a meaningful message available,
     * even when internationalization processing hasn't occurred yet.
     *
     * @return The exception message
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
     * Returns the localized message (same as getMessage for this implementation).
     * <p>
     * Note: This method doesn't perform actual localization. The localization
     * should be handled by external components using the messageCode and messageArgs.
     *
     * @return The exception message (same as getMessage())
     */
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    // ==================== Utility Methods ====================

    /**
     * Checks whether this exception has message arguments for internationalization.
     *
     * @return true if messageArgs is not null and not empty, false otherwise
     */
    public boolean hasMessageArgs() {
        return messageArgs != null && messageArgs.length > 0;
    }

    /**
     * Returns the number of message arguments.
     *
     * @return The count of message arguments, 0 if messageArgs is null
     */
    public int getMessageArgsCount() {
        return messageArgs != null ? messageArgs.length : 0;
    }

    /**
     * Returns a string representation of this exception with detailed information.
     * <p>
     * The format varies based on whether the exception requires internationalization:
     * - For i18n exceptions: Shows messageCode, args, and defaultMessage
     * - For simple exceptions: Shows the message directly
     *
     * @return A detailed string representation of this exception
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