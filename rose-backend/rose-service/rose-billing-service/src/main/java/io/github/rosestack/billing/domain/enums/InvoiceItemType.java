package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 账单明细类型枚举
 * 
 * 定义账单明细的类型分类
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum InvoiceItemType {

    /**
     * 套餐费用 - 套餐基础费用明细
     * target_type: 'PLAN'
     * target_id: 套餐ID
     * description: "PRO套餐月费"
     * quantity: 1
     * unit_price: 99.00
     * amount: 99.00
     */
    PLAN("套餐费用"),

    /**
     * 功能费用 - 按量计费功能费用明细
     * target_type: 'FEATURE'
     * target_id: 功能ID
     * description: "API调用超量费用"
     * quantity: 5000（超量5000次）
     * unit_price: 0.001
     * amount: 5.00
     */
    FEATURE("功能费用"),

    /**
     * 折扣 - 折扣优惠明细
     * target_type: NULL
     * target_id: NULL
     * description: "新用户优惠"
     * quantity: 1
     * unit_price: -10.00
     * amount: -10.00
     */
    DISCOUNT("折扣"),

    /**
     * 税费 - 税费明细
     * target_type: NULL
     * target_id: NULL
     * description: "增值税"
     * quantity: 1
     * unit_price: 8.91
     * amount: 8.91
     */
    TAX("税费"),

    /**
     * 调整 - 调整费用明细
     * target_type: 'PLAN'
     * target_id: 套餐ID
     * description: "套餐升级差额"
     * quantity: 1
     * unit_price: 50.00
     * amount: 50.00
     */
    ADJUSTMENT("调整");

    /**
     * 明细类型描述
     */
    private final String description;

    /**
     * 判断是否为费用项（产生正费用）
     */
    public boolean isChargeItem() {
        return this == PLAN || this == FEATURE || this == TAX || this == ADJUSTMENT;
    }

    /**
     * 判断是否为优惠项（产生负费用）
     */
    public boolean isDiscountItem() {
        return this == DISCOUNT;
    }

    /**
     * 判断是否需要关联目标对象
     */
    public boolean requiresTarget() {
        return this == PLAN || this == FEATURE || this == ADJUSTMENT;
    }

    /**
     * 判断是否为套餐相关明细
     */
    public boolean isPlanRelated() {
        return this == PLAN;
    }

    /**
     * 判断是否为功能相关明细
     */
    public boolean isFeatureRelated() {
        return this == FEATURE;
    }

    /**
     * 判断是否为税费相关明细
     */
    public boolean isTaxRelated() {
        return this == TAX;
    }
}