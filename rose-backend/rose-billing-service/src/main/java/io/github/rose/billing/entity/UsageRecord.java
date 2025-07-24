package io.github.rose.billing.entity;

import io.github.rose.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用量记录实体
 *
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsageRecord extends TenantModel<String> {

    /**
     * 计量类型（API_CALLS, STORAGE, USERS, SMS, EMAIL等）
     */
    private String metricType;

    /**
     * 使用量数值
     */
    private BigDecimal quantity;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 关联的资源ID（用户ID、API密钥等）
     */
    private String resourceId;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 元数据（如API路径、文件类型等）
     */
    private String metadata;

    /**
     * 是否已计费
     */
    private Boolean billed;

    /**
     * 计费时间
     */
    private LocalDateTime billedAt;

    /**
     * 关联的账单ID
     */
    private String invoiceId;
}

/**
 * 使用量统计实体（聚合数据）
 */
@Data
@EqualsAndHashCode(callSuper = true)
class UsageAggregate extends TenantModel<String> {

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
