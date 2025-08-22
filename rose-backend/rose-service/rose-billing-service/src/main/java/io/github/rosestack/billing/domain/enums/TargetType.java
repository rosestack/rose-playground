package io.github.rosestack.billing.domain.enums;

/**
 * 定价目标类型枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
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

    TargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}