package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.enums.BillingType;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订阅计划实体
 *
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscription_plan")
public class SubscriptionPlan extends BaseTenantEntity{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 计划名称
     */
    private String name;

    /**
     * 计划代码（唯一标识）
     */
    private String code;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 计费类型：MONTHLY, YEARLY, USAGE_BASED, HYBRID
     */
    private BillingType billingType;

    /**
     * 基础费用
     */
    private BigDecimal basePrice;

    /**
     * 计费周期（天）
     */
    private Integer billingCycle;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大存储空间（GB）
     */
    private Long maxStorage;

    /**
     * API调用限制（每月）
     */
    private Long apiCallLimit;

    /**
     * 是否支持自定义品牌
     */
    private Boolean customBrandingEnabled;

    /**
     * 功能特性列表
     */
    private Map<String, Object> features;

    /**
     * 计量计费配置
     */
    private Map<String, BigDecimal> usagePricing;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 试用期天数
     */
    private Integer trialDays;

    /**
     * 计划生效时间
     */
    private LocalDateTime effectiveDate;

    /**
     * 计划失效时间
     */
    private LocalDateTime expiryDate;
}
