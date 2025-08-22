package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 试用状态枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TrialStatus {

    /**
     * 试用中 - 试用期内，用户正在使用套餐功能
     */
    ACTIVE("试用中"),

    /**
     * 已过期 - 试用期结束，未转换为付费用户
     */
    EXPIRED("已过期"),

    /**
     * 已转换 - 试用期内或结束后转换为付费用户
     */
    CONVERTED("已转换"),

    /**
     * 已取消 - 用户主动取消试用
     */
    CANCELLED("已取消");

    private final String description;

    /**
     * 检查是否为活跃状态
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 检查是否为终态（不再变化的状态）
     */
    public boolean isFinal() {
        return this == EXPIRED || this == CONVERTED || this == CANCELLED;
    }

    /**
     * 检查是否为成功状态
     */
    public boolean isSuccessful() {
        return this == CONVERTED;
    }
}
