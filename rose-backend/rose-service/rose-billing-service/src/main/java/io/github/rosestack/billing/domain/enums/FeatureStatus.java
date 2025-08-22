package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 功能状态枚举
 * 
 * 定义功能的启用状态，用于控制功能是否对用户可用
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum FeatureStatus {

    /**
     * 激活状态 - 功能正常可用
     * 特点：功能对用户完全可用，参与计费和配额检查
     * 使用场景：正常运营中的功能
     */
    ACTIVE("激活状态"),

    /**
     * 禁用状态 - 功能暂时不可用
     * 特点：功能对用户不可用，不参与计费和配额检查
     * 使用场景：功能维护、临时下线、问题修复等
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