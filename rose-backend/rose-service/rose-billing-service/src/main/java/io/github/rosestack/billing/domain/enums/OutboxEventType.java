package io.github.rosestack.billing.domain.enums;

/**
 * Outbox 事件类型枚举
 * <p>
 * 定义计费系统中的各种业务事件类型
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum OutboxEventType {
    
    /**
     * 订阅创建事件
     */
    SUBSCRIPTION_CREATED,
    
    /**
     * 订阅更新事件
     */
    SUBSCRIPTION_UPDATED,
    
    /**
     * 订阅取消事件
     */
    SUBSCRIPTION_CANCELLED,
    
    /**
     * 订阅过期事件
     */
    SUBSCRIPTION_EXPIRED,
    
    /**
     * 账单生成事件
     */
    INVOICE_GENERATED,
    
    /**
     * 账单支付事件
     */
    INVOICE_PAID,
    
    /**
     * 账单逾期事件
     */
    INVOICE_OVERDUE,
    
    /**
     * 支付成功事件
     */
    PAYMENT_SUCCEEDED,
    
    /**
     * 支付失败事件
     */
    PAYMENT_FAILED,
    
    /**
     * 配额超限事件
     */
    QUOTA_EXCEEDED,
    
    /**
     * 用量记录事件
     */
    USAGE_RECORDED,
    
    /**
     * 试用开始事件
     */
    TRIAL_STARTED,
    
    /**
     * 试用结束事件
     */
    TRIAL_ENDED,
    
    /**
     * 试用转换事件
     */
    TRIAL_CONVERTED
}