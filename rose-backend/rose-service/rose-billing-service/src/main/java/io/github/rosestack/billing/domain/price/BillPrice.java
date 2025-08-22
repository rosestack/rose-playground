package io.github.rosestack.billing.domain.price;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.PriceType;
import io.github.rosestack.billing.domain.enums.TargetType;
import io.github.rosestack.core.model.HasStatus;
import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一定价表实体类
 *
 * 支持多种定价策略，包括标准定价和租户专属定价
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_price")
public class BillPrice extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 定价类型
     * PLAN: 标准套餐定价 - 适用于所有租户的套餐定价
     * FEATURE: 标准功能定价 - 适用于所有租户的功能定价
     * TENANT_PLAN: 租户专属套餐定价 - 特定租户的套餐优惠价格
     * TENANT_FEATURE: 租户专属功能定价 - 特定租户的功能优惠价格
     */
    private PriceType type;

    /**
     * 目标类型
     * PLAN: 指向套餐
     * FEATURE: 指向功能
     */
    private TargetType targetType;

    /**
     * 目标ID
     * 当targetType为PLAN时，指向bill_plan.id
     * 当targetType为FEATURE时，指向bill_feature.id
     */
    private Long targetId;

    /**
     * 租户ID
     * 当type为PLAN或FEATURE时，此字段为NULL（适用于所有租户）
     * 当type为TENANT_PLAN或TENANT_FEATURE时，此字段有具体值（特定租户）
     */
    private Long tenantId;

    /**
     * 价格
     * billing_cycle = 'MONTHLY/YEARLY' 时，表示固定费用金额
     * billing_cycle = 'USAGE' 时，通常为0（实际单价在pricingConfig中配置）
     */
    private BigDecimal price;

    /**
     * 货币单位
     */
    private String currency;

    /**
     * 计费周期
     * MONTHLY: 月度计费 - 固定月费
     * YEARLY: 年度计费 - 固定年费
     * USAGE: 使用量计费 - 按实际使用量计费
     */
    private BillingCycle billingCycle;

    /**
     * 定价配置JSON
     * 仅当billingCycle=USAGE时必填，支持多种定价模式
     *
     * 统一JSON格式结构：
     * {
     *   "type": "quota|tiered|usage|package|tiered_fixed",
     *   "values": [
     *     {
     *       "min": 0,
     *       "max": 1000,
     *       "quantity": 10000,  // 仅package类型使用
     *       "price": 0.001
     *     }
     *   ]
     * }
     */
    private String pricingConfig;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态
     * DRAFT: 草稿状态
     * ACTIVE: 生效中
     * INACTIVE: 已禁用
     * EXPIRED: 已过期
     */
    private String status;

    /**
     * 检查定价是否激活
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 检查定价是否有效（考虑时间范围）
     */
    public boolean isEffective() {
        if (!isActive()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查生效时间
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }

        // 检查失效时间
        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否为租户专属定价
     */
    public boolean isTenantSpecific() {
        return type != null && type.isTenantSpecific();
    }

    /**
     * 检查是否为标准定价
     */
    public boolean isStandardPricing() {
        return type != null && !type.isTenantSpecific();
    }

    /**
     * 检查是否为套餐相关定价
     */
    public boolean isPlanRelated() {
        return type != null && type.isPlanRelated();
    }

    /**
     * 检查是否为功能相关定价
     */
    public boolean isFeatureRelated() {
        return type != null && type.isFeatureRelated();
    }

    /**
     * 检查是否为固定费用定价
     */
    public boolean isFixedPricing() {
        return billingCycle != null && billingCycle.isFixedBilling();
    }

    /**
     * 检查是否为使用量定价
     */
    public boolean isUsagePricing() {
        return billingCycle != null && billingCycle.isUsageBilling();
    }

    /**
     * 检查是否需要定价配置
     */
    public boolean requiresPricingConfig() {
        return isUsagePricing();
    }

    /**
     * 激活定价
     */
    public void activate() {
        this.status = "ACTIVE";
        if (this.effectiveTime == null) {
            this.effectiveTime = LocalDateTime.now();
        }
    }

    /**
     * 禁用定价
     */
    public void deactivate() {
        this.status = "INACTIVE";
    }

    /**
     * 设为过期
     */
    public void expire() {
        this.status = "EXPIRED";
        if (this.expireTime == null) {
            this.expireTime = LocalDateTime.now();
        }
    }

    /**
     * 获取定价的唯一标识
     * 格式：type:targetType:targetId:tenantId:billingCycle
     */
    public String getPricingKey() {
        return String.format("%s:%s:%d:%s:%s",
            type, targetType, targetId,
            tenantId != null ? tenantId.toString() : "NULL",
            billingCycle);
    }

    /**
     * 验证定价配置是否有效
     */
    public boolean isValidConfiguration() {
        // 基本字段验证
        if (type == null || targetType == null || targetId == null || billingCycle == null) {
            return false;
        }

        // 目标类型验证
        if (!"PLAN".equals(targetType) && !"FEATURE".equals(targetType)) {
            return false;
        }

        // 租户专属定价必须有租户ID
        if (isTenantSpecific() && tenantId == null) {
            return false;
        }

        // 标准定价不应该有租户ID
        if (isStandardPricing() && tenantId != null) {
            return false;
        }

        // 使用量定价必须有定价配置
        if (isUsagePricing() && (pricingConfig == null || pricingConfig.trim().isEmpty())) {
            return false;
        }

        // 固定费用定价应该有价格
        if (isFixedPricing() && (price == null || price.compareTo(BigDecimal.ZERO) < 0)) {
            return false;
        }

        // 时间范围验证
        if (effectiveTime != null && expireTime != null && effectiveTime.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查定价是否在指定时间有效
     */
    public boolean isEffectiveAt(LocalDateTime time) {
        if (!isActive()) {
            return false;
        }

        if (effectiveTime != null && time.isBefore(effectiveTime)) {
            return false;
        }

        if (expireTime != null && time.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 获取价格的显示值
     */
    public String getPriceDisplay() {
        if (price == null) {
            return "0.00";
        }
        return String.format("%.2f %s", price, currency != null ? currency : "USD");
    }
}
