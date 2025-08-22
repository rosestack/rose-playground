package io.github.rosestack.billing.domain.enums;

/**
 * Outbox 事件状态枚举
 * <p>
 * 定义 Outbox 事件的各种状态，确保事件的可靠传递
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum OutboxEventStatus {
    
    /**
     * 待发布 - 事件已创建，等待发布
     */
    PENDING,
    
    /**
     * 发布中 - 事件正在发布过程中
     */
    PUBLISHING,
    
    /**
     * 已发布 - 事件已成功发布
     */
    PUBLISHED,
    
    /**
     * 发布失败 - 事件发布失败，需要重试
     */
    FAILED,
    
    /**
     * 已跳过 - 事件被跳过，不再重试
     */
    SKIPPED
}