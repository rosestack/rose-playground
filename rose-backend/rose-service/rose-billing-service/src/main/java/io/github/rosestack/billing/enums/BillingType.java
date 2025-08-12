package io.github.rosestack.billing.enums;

/** 计费类型枚举 */
public enum BillingType {
    MONTHLY("按月订阅"),
    YEARLY("按年订阅"),
    USAGE_BASED("按使用量计费"),
    HYBRID("混合计费");

    private final String description;

    BillingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
