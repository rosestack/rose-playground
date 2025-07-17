package io.github.rose.i18n;

/**
 * 国际化刷新异常
 * 
 * <p>当消息源刷新失败时抛出此异常。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class I18nRefreshException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public I18nRefreshException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public I18nRefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * 
     * @param cause 原因异常
     */
    public I18nRefreshException(Throwable cause) {
        super(cause);
    }
}
