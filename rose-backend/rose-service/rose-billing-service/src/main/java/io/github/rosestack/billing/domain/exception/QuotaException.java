package io.github.rosestack.billing.domain.exception;

/**
 * 配额相关异常
 *
 * 用于处理配额检查和使用过程中的各种异常情况
 * 遵循简单原则，只保留必要的异常信息
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class QuotaException extends RuntimeException {

    public QuotaException(String message) {
        super(message);
    }

    public QuotaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 配额不足异常
     */
    public static class InsufficientQuotaException extends QuotaException {
        public InsufficientQuotaException(String message) {
            super(message);
        }
    }

    /**
     * 功能未配置异常
     */
    public static class FeatureNotConfiguredException extends QuotaException {
        public FeatureNotConfiguredException(String message) {
            super(message);
        }
    }

    /**
     * 功能已禁用异常
     */
    public static class FeatureDisabledException extends QuotaException {
        public FeatureDisabledException(String message) {
            super(message);
        }
    }

    /**
     * 订阅不存在异常
     */
    public static class SubscriptionNotFoundException extends QuotaException {
        public SubscriptionNotFoundException(String message) {
            super(message);
        }
    }
}