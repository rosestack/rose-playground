package io.github.rose.billing.entity;

import io.github.rose.billing.enums.SubscriptionStatus;
import io.github.rose.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户订阅实体
 *
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseTenantSubscription extends BaseTenantEntity {
    private String id;

    /**
     * 订阅计划ID
     */
    private String planId;

    /**
     * 订阅状态
     */
    private SubscriptionStatus status;

    /**
     * 订阅开始时间
     */
    private LocalDateTime startDate;

    /**
     * 订阅结束时间
     */
    private LocalDateTime endDate;

    /**
     * 下次计费时间
     */
    private LocalDateTime nextBillingDate;

    /**
     * 试用结束时间
     */
    private LocalDateTime trialEndDate;

    /**
     * 是否在试用期
     */
    private Boolean inTrial;

    /**
     * 自动续费
     */
    private Boolean autoRenew;

    /**
     * 当前计费周期的基础费用
     */
    private BigDecimal currentPeriodAmount;

    /**
     * 取消时间
     */
    private LocalDateTime cancelledAt;

    /**
     * 取消原因
     */
    private String cancellationReason;

    /**
     * 暂停时间
     */
    private LocalDateTime pausedAt;

    /**
     * 暂停原因
     */
    private String pauseReason;
}

