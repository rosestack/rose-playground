package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 套餐功能关联状态枚举
 * 
 * 定义套餐与功能关联关系的状态，用于控制特定套餐中的功能是否可用
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PlanFeatureStatus {

    /**
     * 激活状态 - 功能在该套餐中可用
     * 特点：功能配置生效，用户可以使用该功能
     * 使用场景：正常的套餐功能配置
     */
    ACTIVE("激活状态"),

    /**
     * 禁用状态 - 功能在该套餐中不可用
     * 特点：功能配置不生效，用户无法使用该功能
     * 使用场景：临时禁用某个套餐的特定功能、功能下线等
     */
    INACTIVE("禁用状态");

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否为激活状态
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isInactive() {
        return this == INACTIVE;
    }
}