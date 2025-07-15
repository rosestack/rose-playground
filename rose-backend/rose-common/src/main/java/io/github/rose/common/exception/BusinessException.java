package io.github.rose.common.exception;

import io.github.rose.common.util.MessageUtils;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        this(message, null);
    }

    public BusinessException(String message, Object... params) {
        this(null, message, params);
    }

    public BusinessException(Throwable cause, String message) {
        this(cause, message, null);
    }

    public BusinessException(Throwable cause, String message, Object... params) {
        super(MessageUtils.getMessage(message, params), cause);
    }
}
