package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;

/**
 * 重置周期枚举
 * 
 * 定义功能配额和使用量的重置频率
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum ResetPeriod implements BaseEnum {

    /**
     * 每日重置 - 每天重置配额和使用量
     * 适用场景：日度API调用限制、日度消息发送等
     */
    DAY("DAY", "每日重置"),

    /**
     * 每月重置 - 每月重置配额和使用量
     * 适用场景：月度流量、月度API调用、月度存储等
     */
    MONTH("MONTH", "每月重置"),

    /**
     * 每年重置 - 每年重置配额和使用量
     * 适用场景：年度报告生成、年度数据导出等
     */
    YEAR("YEAR", "每年重置"),

    /**
     * 永不重置 - 配额不会自动重置
     * 适用场景：用户数量、项目数量、永久存储空间等
     */
    NEVER("NEVER", "永不重置");

    private final String code;
    private final String name;

    ResetPeriod(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 检查是否需要重置
     */
    public boolean needsReset() {
        return this != NEVER;
    }

    /**
     * 获取重置周期的天数
     * 用于计算下一次重置时间
     */
    public int getDaysInPeriod() {
        switch (this) {
            case DAY:
                return 1;
            case MONTH:
                return 30; // 简化处理，实际应该根据具体月份计算
            case YEAR:
                return 365; // 简化处理，实际应该根据是否闰年计算
            case NEVER:
            default:
                return Integer.MAX_VALUE;
        }
    }
}