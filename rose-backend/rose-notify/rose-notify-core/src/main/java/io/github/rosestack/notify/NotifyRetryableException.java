package io.github.rosestack.notify;

/**
 * 可重试的通知异常（如网络、第三方服务超时等）。
 */
public class NotifyRetryableException extends NotifyException {
    public NotifyRetryableException(String message) {
        super(message);
    }

    public NotifyRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
