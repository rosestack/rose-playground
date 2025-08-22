package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 账单状态枚举
 *
 * 定义账单的生命周期状态
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum InvoiceStatus {

    /**
     * 草稿状态 - 账单生成中，尚未确认
     * 特点：账单生成中，尚未确认
     * 用户影响：用户看不到此账单
     */
    DRAFT("草稿状态"),

    /**
     * 待支付 - 账单已确认，等待用户支付
     * 特点：账单已确认，等待用户支付
     * 用户影响：用户可以看到并支付此账单
     */
    PENDING("待支付"),

    /**
     * 已支付 - 用户已成功支付
     * 特点：用户已成功支付
     * 用户影响：服务正常，账单已结清
     */
    PAID("已支付"),

    /**
     * 逾期未付 - 超过到期时间仍未支付
     * 特点：超过到期时间仍未支付
     * 用户影响：可能影响服务使用
     */
    OVERDUE("逾期未付"),

    /**
     * 已取消 - 账单被取消，无需支付
     * 特点：账单被取消，无需支付
     * 用户影响：账单作废
     */
    CANCELLED("已取消"),

    /**
     * 已退款 - 账单金额已退还给用户
     * 特点：账单金额已退还给用户
     * 用户影响：金额已退回
     */
    REFUNDED("已退款");

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否为待处理状态
     */
    public boolean isPending() {
        return this == DRAFT || this == PENDING;
    }

    /**
     * 判断是否为已完成状态
     */
    public boolean isCompleted() {
        return this == PAID || this == CANCELLED || this == REFUNDED;
    }

    /**
     * 判断是否为问题状态
     */
    public boolean isProblem() {
        return this == OVERDUE;
    }

    /**
     * 判断是否可以支付
     */
    public boolean canPay() {
        return this == PENDING || this == OVERDUE;
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return this == DRAFT || this == PENDING;
    }

    /**
     * 判断是否可以退款
     */
    public boolean canRefund() {
        return this == PAID;
    }

    /**
     * 检查是否为活跃状态（需要处理的状态）
     */
    public boolean isActive() {
        return this == PENDING || this == OVERDUE;
    }

    /**
     * 检查是否为终态（不再变化的状态）
     */
    public boolean isFinal() {
        return this == PAID || this == CANCELLED || this == REFUNDED;
    }
}
