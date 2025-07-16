package io.github.rose.i18n;

/**
 * 国际化消息相关异常。
 */
public class MessageSourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MessageSourceException(String message) {
        super(message);
    }

    public MessageSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageSourceException(Throwable cause) {
        super(cause);
    }
}