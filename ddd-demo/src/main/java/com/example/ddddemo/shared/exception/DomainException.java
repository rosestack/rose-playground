package com.example.ddddemo.shared.exception;

/**
 * 领域异常
 * <p>
 * 用于表示领域层的业务规则违反，如实体状态错误、业务约束违反等
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
    }

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}