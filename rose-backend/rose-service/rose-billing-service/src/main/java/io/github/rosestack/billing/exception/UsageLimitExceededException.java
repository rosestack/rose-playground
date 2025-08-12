package io.github.rosestack.billing.exception;

import java.math.BigDecimal;

/**
 * 使用量超限异常
 *
 * @author rose
 */
public class UsageLimitExceededException extends BillingException {

    private final String metricType;
    private final BigDecimal currentUsage;
    private final BigDecimal limit;

    public UsageLimitExceededException(String metricType, BigDecimal currentUsage, BigDecimal limit) {
        super("USAGE_LIMIT_EXCEEDED", String.format("使用量超限: %s 当前使用量 %s 超过限制 %s", metricType, currentUsage, limit));
        this.metricType = metricType;
        this.currentUsage = currentUsage;
        this.limit = limit;
    }

    public String getMetricType() {
        return metricType;
    }

    public BigDecimal getCurrentUsage() {
        return currentUsage;
    }

    public BigDecimal getLimit() {
        return limit;
    }
}
