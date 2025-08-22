package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 定价目标类型枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TargetType {

    /**
     * 套餐
     * 定价目标为套餐
     */
    PLAN("套餐"),

    /**
     * 功能
     * 定价目标为功能
     */
    FEATURE("功能");

    private final String description;
}
