package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 计费周期枚举
 * 
 * 定义系统中支持的计费周期类型
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum BillingCycle {

    /**
     * 月度计费 - 固定月费，每月收取固定金额
     * price字段：表示月费金额，如99.00元/月
     * pricing_config：通常为NULL，不需要使用量配置
     * 适用场景：套餐基础月费、月度订阅服务
     */
    MONTHLY("月度计费"),

    /**
     * 年度计费 - 固定年费，每年收取固定金额
     * price字段：表示年费金额，如999.00元/年
     * pricing_config：通常为NULL，不需要使用量配置
     * 适用场景：套餐基础年费、年度订阅优惠
     */
    YEARLY("年度计费"),

    /**
     * 使用量计费 - 按实际使用量计费，支持多种定价模式
     * price字段：通常为0，实际单价在pricing_config中配置
     * pricing_config：必须配置，定义具体的计费规则
     * 适用场景：API调用、存储空间、流量等按量计费功能
     */
    USAGE("使用量计费");

    /**
     * 计费周期描述
     */
    private final String description;

    /**
     * 判断是否为固定费用计费
     */
    public boolean isFixedBilling() {
        return this == MONTHLY || this == YEARLY;
    }

    /**
     * 判断是否为使用量计费
     */
    public boolean isUsageBilling() {
        return this == USAGE;
    }

    /**
     * 获取计费周期的天数
     */
    public int getDaysInCycle() {
        switch (this) {
            case MONTHLY:
                return 30; // 简化处理，实际应该根据具体月份计算
            case YEARLY:
                return 365; // 简化处理，实际应该根据具体年份计算
            case USAGE:
                return 1; // 按量计费没有固定周期
            default:
                throw new IllegalArgumentException("未知的计费周期: " + this);
        }
    }
}