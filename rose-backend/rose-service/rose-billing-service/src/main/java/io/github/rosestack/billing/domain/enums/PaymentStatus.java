package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付状态枚举
 * 
 * 定义支付的生命周期状态
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    /**
     * 处理中 - 支付请求已提交，等待处理结果
     */
    PENDING("处理中"),

    /**
     * 成功 - 支付成功完成
     */
    SUCCESS("支付成功"),

    /**
     * 失败 - 支付失败，需要重新支付
     */
    FAILED("支付失败"),

    /**
     * 已取消 - 用户主动取消支付
     */
    CANCELLED("已取消"),

    /**
     * 已退款 - 支付已退款给用户
     */
    REFUNDED("已退款");

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * 判断是否为成功状态
     */
    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为失败状态
     */
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * 判断是否可以退款
     */
    public boolean canRefund() {
        return this == SUCCESS;
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return this == FAILED;
    }

    /**
     * 判断是否在处理中
     */
    public boolean isPending() {
        return this == PENDING;
    }
}