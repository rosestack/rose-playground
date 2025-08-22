package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 定价类型枚举
 * 
 * 定义系统中支持的定价类型，包括标准定价和租户专属定价
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PriceType {

    /**
     * 标准套餐定价 - 适用于所有租户的套餐定价
     * tenant_id = NULL
     */
    PLAN("标准套餐定价"),

    /**
     * 标准功能定价 - 适用于所有租户的功能定价
     * tenant_id = NULL  
     */
    FEATURE("标准功能定价"),

    /**
     * 租户专属套餐定价 - 特定租户的套餐优惠价格
     * tenant_id 有具体值
     */
    TENANT_PLAN("租户专属套餐定价"),

    /**
     * 租户专属功能定价 - 特定租户的功能优惠价格
     * tenant_id 有具体值
     */
    TENANT_FEATURE("租户专属功能定价");

    /**
     * 定价类型描述
     */
    private final String description;

    /**
     * 判断是否为租户专属定价
     */
    public boolean isTenantSpecific() {
        return this == TENANT_PLAN || this == TENANT_FEATURE;
    }

    /**
     * 判断是否为套餐相关定价
     */
    public boolean isPlanRelated() {
        return this == PLAN || this == TENANT_PLAN;
    }

    /**
     * 判断是否为功能相关定价
     */
    public boolean isFeatureRelated() {
        return this == FEATURE || this == TENANT_FEATURE;
    }
}