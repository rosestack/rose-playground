package com.example.ddddemo.shared.exception;

/**
 * 基础设施异常
 * <p>
 * 用于表示基础设施层的技术错误，如数据库连接失败、外部服务调用失败等
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
public class InfrastructureException extends RuntimeException {

    private final String errorCode;

    public InfrastructureException(String message) {
        super(message);
        this.errorCode = "INFRASTRUCTURE_ERROR";
    }

    public InfrastructureException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InfrastructureException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}