package io.github.rosestack.billing.domain.outbox;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.OutboxEventStatus;
import io.github.rosestack.billing.domain.enums.OutboxEventType;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Outbox 事件实体
 * <p>
 * 用于实现 Outbox 模式，确保业务操作和消息发布的原子性
 * 在同一个数据库事务中，系统同时更新业务数据和插入 Outbox 表记录
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_outbox_event")
public class OutboxEvent extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件唯一标识符
     * 用于防重复处理和幂等性保证
     */
    private String eventId;

    /**
     * 事件类型
     */
    private OutboxEventType eventType;

    /**
     * 聚合根类型
     * 如: Subscription, Invoice, Payment 等
     */
    private String aggregateType;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 事件数据（JSON格式）
     * 包含事件的详细信息
     */
    private String eventData;

    /**
     * 事件状态
     */
    private OutboxEventStatus status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 事件元数据（JSON格式）
     * 包含额外的元信息，如来源、上下文等
     */
    private String metadata;

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        if (status == OutboxEventStatus.PUBLISHED || status == OutboxEventStatus.SKIPPED) {
            return false;
        }
        
        if (maxRetryCount != null && retryCount != null && retryCount >= maxRetryCount) {
            return false;
        }
        
        if (nextRetryTime != null && LocalDateTime.now().isBefore(nextRetryTime)) {
            return false;
        }
        
        return true;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
    }

    /**
     * 设置下次重试时间（指数退避）
     */
    public void setNextRetryTimeWithBackoff() {
        if (retryCount == null || retryCount == 0) {
            nextRetryTime = LocalDateTime.now().plusMinutes(1);
        } else {
            // 指数退避：1分钟、2分钟、4分钟、8分钟...最大60分钟
            long delayMinutes = Math.min(60, (long) Math.pow(2, retryCount - 1));
            nextRetryTime = LocalDateTime.now().plusMinutes(delayMinutes);
        }
    }

    /**
     * 标记为已发布
     */
    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedTime = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * 标记为发布失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = OutboxEventStatus.FAILED;
        this.errorMessage = errorMessage;
        incrementRetryCount();
        setNextRetryTimeWithBackoff();
    }

    /**
     * 标记为已跳过
     */
    public void markAsSkipped(String reason) {
        this.status = OutboxEventStatus.SKIPPED;
        this.errorMessage = reason;
    }

    /**
     * 标记为发布中
     */
    public void markAsPublishing() {
        this.status = OutboxEventStatus.PUBLISHING;
    }
}