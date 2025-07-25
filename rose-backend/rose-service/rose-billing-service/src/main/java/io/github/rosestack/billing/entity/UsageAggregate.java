package io.github.rosestack.billing.entity;

import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用量统计实体（聚合数据）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsageAggregate extends BaseTenantEntity {
    private String id;

    /**
     * 计量类型
     */
    private String metricType;

    /**
     * 聚合周期（HOURLY, DAILY, MONTHLY）
     */
    private String aggregationPeriod;

    /**
     * 周期开始时间
     */
    private LocalDateTime periodStart;

    /**
     * 周期结束时间
     */
    private LocalDateTime periodEnd;

    /**
     * 总使用量
     */
    private BigDecimal totalUsage;

    /**
     * 计费金额
     */
    private BigDecimal billingAmount;

    /**
     * 是否已计费
     */
    private Boolean billed;
}
