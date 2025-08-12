package io.github.rosestack.billing.enums;

/**
 * 账单状态枚举
 */
public enum InvoiceStatus {
    DRAFT("草稿"),
    PENDING("待支付"),
    PAID("已支付"),
    OVERDUE("逾期"),
    CANCELLED("已取消"),
    REFUNDED("已退款");

    private final String description;

    InvoiceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
