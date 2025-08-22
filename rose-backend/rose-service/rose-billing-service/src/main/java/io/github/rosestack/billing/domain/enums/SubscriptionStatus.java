package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订阅状态枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {

    /**
     * 试用中 - 用户正在试用套餐，未付费
     * 特点：提供完整功能，但有时间限制
     * 使用场景：试用期内，让用户体验完整功能
     * 转换：试用结束后可转为ACTIVE（付费）或EXPIRED（过期）
     */
    TRIAL("试用中"),

    /**
     * 活跃中 - 正常付费订阅，服务正常
     * 特点：用户已付费，享受完整服务
     * 使用场景：正常的付费用户状态
     * 转换：可转为PAST_DUE（逾期）、SUSPENDED（暂停）、CANCELLED（取消）
     */
    ACTIVE("活跃中"),

    /**
     * 逾期未付 - 付费失败，但仍在宽限期内
     * 特点：服务可能继续提供，但有逾期提醒
     * 使用场景：给用户时间更新付费方式或处理付费问题
     * 转换：付费成功转ACTIVE，超过宽限期转SUSPENDED或EXPIRED
     */
    PAST_DUE("逾期未付"),

    /**
     * 已暂停 - 服务暂停，但订阅关系保留
     * 特点：服务停止，但数据和配置保留
     * 使用场景：长期逾期、违规、主动暂停等
     * 转换：问题解决后可转回ACTIVE
     */
    SUSPENDED("已暂停"),

    /**
     * 已取消 - 用户主动取消，但服务继续到周期结束
     * 特点：用户已取消，但服务继续到当前计费周期结束
     * 使用场景：优雅取消，避免立即中断服务
     * 转换：周期结束后转为EXPIRED
     */
    CANCELLED("已取消"),

    /**
     * 已过期 - 订阅已结束，服务完全停止
     * 特点：订阅关系结束，服务完全停止
     * 使用场景：试用过期、订阅到期、取消后到期
     * 转换：用户重新订阅可转为ACTIVE或TRIAL
     */
    EXPIRED("已过期");

    private final String description;

    /**
     * 检查是否可以提供服务
     */
    public boolean canProvideService() {
        return this == TRIAL ||
               this == ACTIVE ||
               this == PAST_DUE ||
               this == CANCELLED; // 取消状态下继续服务到周期结束
    }

    /**
     * 检查是否为付费状态
     */
    public boolean isPaid() {
        return this == ACTIVE;
    }

    /**
     * 检查是否为试用状态
     */
    public boolean isTrial() {
        return this == TRIAL;
    }

    /**
     * 检查是否已结束
     */
    public boolean isEnded() {
        return this == EXPIRED;
    }

    /**
     * 检查是否需要处理付费问题
     */
    public boolean needsPaymentAction() {
        return this == PAST_DUE;
    }

    /**
     * 检查是否可以重新激活
     */
    public boolean canReactivate() {
        return this == SUSPENDED || this == PAST_DUE || this == EXPIRED;
    }
}
