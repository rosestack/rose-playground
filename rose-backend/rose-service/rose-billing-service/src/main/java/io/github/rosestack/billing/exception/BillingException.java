package io.github.rosestack.billing.exception;

/**
 * 计费系统基础异常类
 *
 * @author rose
 */
public class BillingException extends RuntimeException {

    private final String errorCode;

    public BillingException(String message) {
        super(message);
        this.errorCode = "BILLING_ERROR";
    }

    public BillingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BillingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BILLING_ERROR";
    }

    public BillingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
