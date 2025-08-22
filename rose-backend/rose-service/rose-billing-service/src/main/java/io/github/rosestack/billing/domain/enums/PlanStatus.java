package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;

/**
 * 套餐状态枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum PlanStatus implements BaseEnum {

    /**
     * 草稿状态 - 开发中，不对外开放
     * 特点：新套餐设计阶段，内部测试使用
     * 用户影响：用户无法看到和订阅此套餐
     * 使用场景：套餐开发、功能测试、内部评审
     */
    DRAFT("DRAFT", "草稿状态"),

    /**
     * 生效中 - 正常运营状态，用户可以订阅
     * 特点：套餐正常销售，功能完全可用
     * 用户影响：用户可以正常订阅和使用
     * 使用场景：主要销售的套餐版本
     */
    ACTIVE("ACTIVE", "生效中"),

    /**
     * 已禁用 - 暂停新订阅，现有订阅继续服务
     * 特点：不接受新用户订阅，但现有用户继续服务
     * 用户影响：现有用户不受影响，新用户无法订阅
     * 使用场景：临时下架、问题修复期间、容量限制
     */
    INACTIVE("INACTIVE", "已禁用"),

    /**
     * 已弃用 - 不推荐使用，逐步下线
     * 特点：标记为过时，鼓励用户迁移到新版本
     * 用户影响：现有用户可继续使用，建议升级到新版本
     * 使用场景：老版本套餐的退出策略、功能升级过渡
     */
    DEPRECATED("DEPRECATED", "已弃用"),

    /**
     * 已归档 - 仅作历史记录保存，完全停止服务
     * 特点：完全停止服务，仅保留历史记录
     * 用户影响：所有相关订阅需要迁移，功能完全停用
     * 使用场景：彻底下线的套餐版本、合规性数据保留
     */
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String name;

    PlanStatus(String code, String name) {
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
     * 检查套餐是否可以被新用户订阅
     */
    public boolean canBeSubscribed() {
        return this == ACTIVE;
    }

    /**
     * 检查套餐是否可以提供服务
     */
    public boolean canProvideService() {
        return this == ACTIVE || this == INACTIVE || this == DEPRECATED;
    }

    /**
     * 检查套餐是否已经下线
     */
    public boolean isOffline() {
        return this == ARCHIVED;
    }

    /**
     * 检查套餐是否在开发中
     */
    public boolean isDraft() {
        return this == DRAFT;
    }

    /**
     * 检查套餐是否正常运营
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}