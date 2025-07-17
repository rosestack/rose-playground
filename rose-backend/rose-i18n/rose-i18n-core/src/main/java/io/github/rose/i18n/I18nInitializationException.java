package io.github.rose.i18n;

/**
 * 国际化初始化异常
 * 
 * <p>当消息源初始化失败时抛出此异常。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class I18nInitializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public I18nInitializationException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public I18nInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * 
     * @param cause 原因异常
     */
    public I18nInitializationException(Throwable cause) {
        super(cause);
    }
}
