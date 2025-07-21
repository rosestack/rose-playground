package com.example.ddddemo.shared.exception;

/**
 * 业务异常
 * <p>
 * 用于表示业务逻辑错误，如数据验证失败、业务规则违反等
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}