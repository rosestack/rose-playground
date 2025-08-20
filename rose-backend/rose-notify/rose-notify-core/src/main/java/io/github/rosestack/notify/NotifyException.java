package io.github.rosestack.notify;

public class NotifyException extends RuntimeException {
    public NotifyException(String message) {
        super(message);
    }

    public NotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
