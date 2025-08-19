package io.github.rosestack.notice;

public class NoticeException extends RuntimeException {
    public NoticeException(String message) {
        super(message);
    }

    public NoticeException(String message, Throwable cause) {
        super(message, cause);
    }
}
