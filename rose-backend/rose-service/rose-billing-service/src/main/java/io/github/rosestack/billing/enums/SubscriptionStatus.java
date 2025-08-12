package io.github.rosestack.billing.enums;

/**
 * 订阅状态枚举
 */
public enum SubscriptionStatus {
    TRIAL("试用中"),
    ACTIVE("活跃"),
    PAUSED("暂停"),
    CANCELLED("已取消"),
    EXPIRED("已过期"),
    PENDING_PAYMENT("待支付");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
