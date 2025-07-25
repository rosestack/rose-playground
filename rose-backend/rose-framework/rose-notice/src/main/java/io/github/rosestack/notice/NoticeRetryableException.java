package io.github.rosestack.notice;

/**
 * 可重试的通知异常（如网络、第三方服务超时等）。
 */
public class NoticeRetryableException extends NoticeException {
    public NoticeRetryableException(String message) {
        super(message);
    }

    public NoticeRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
