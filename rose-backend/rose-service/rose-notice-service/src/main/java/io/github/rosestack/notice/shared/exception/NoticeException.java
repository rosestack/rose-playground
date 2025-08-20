package io.github.rosestack.notice.shared.exception;

import io.github.rosestack.core.exception.BaseException;

/**
 * 通知业务异常基类
 *
 * <p>所有通知相关的业务异常都应继承此类。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public class NoticeException extends BaseException {

    public NoticeException(String messageKey) {
        super(messageKey);
    }

    protected NoticeException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    protected NoticeException(String messageKey, Object... messageArgs) {
        super(messageKey, messageArgs);
    }

    protected NoticeException(String messageKey, Throwable cause, Object... messageArgs) {
        super(messageKey, cause, messageArgs);
    }
}
