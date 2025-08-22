package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;

/**
 * 套餐类型枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum PlanType implements BaseEnum {

    /**
     * 免费版 - 基础功能，适合个人用户试用
     */
    FREE("FREE", "免费版"),

    /**
     * 基础版 - 基本付费套餐，适合小团队
     */
    BASIC("BASIC", "基础版"),

    /**
     * 专业版 - 完整功能套餐，适合成长型企业
     */
    PRO("PRO", "专业版"),

    /**
     * 企业版 - 高级功能套餐，适合大型企业
     */
    ENTERPRISE("ENTERPRISE", "企业版");

    private final String code;
    private final String name;

    PlanType(String code, String name) {
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
     * 检查是否为免费套餐
     */
    public boolean isFree() {
        return this == FREE;
    }

    /**
     * 检查是否为付费套餐
     */
    public boolean isPaid() {
        return this != FREE;
    }
}