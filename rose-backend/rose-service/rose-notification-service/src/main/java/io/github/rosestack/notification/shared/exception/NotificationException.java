package io.github.rosestack.notification.shared.exception;

import io.github.rosestack.core.exception.BaseException;

/**
 * 通知业务异常基类
 * <p>
 * 所有通知相关的业务异常都应继承此类。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public class NotificationException extends BaseException {

    public NotificationException(String messageKey) {
        super(messageKey);
    }

    protected NotificationException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    protected NotificationException(String messageKey, Object... messageArgs) {
        super(messageKey, messageArgs);
    }

    protected NotificationException(String messageKey, Throwable cause, Object... messageArgs) {
        super(messageKey, cause, messageArgs);
    }
}