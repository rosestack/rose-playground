package io.github.rose.i18n;

/**
 * Exception for i18n message related errors.
 */
public class MessageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }
} 