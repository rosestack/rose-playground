
package io.github.rose.security.auth.exception;

public abstract class AbstractRateLimitException extends RuntimeException {

    public AbstractRateLimitException() {
        super();
    }

    public AbstractRateLimitException(String message) {
        super(message);
    }

    public AbstractRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractRateLimitException(Throwable cause) {
        super(cause);
    }

    protected AbstractRateLimitException(String message, Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
