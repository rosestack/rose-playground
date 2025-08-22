package io.github.rosestack.billing.domain.plan;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.BillingMode;
import io.github.rosestack.billing.domain.enums.PlanStatus;
import io.github.rosestack.billing.domain.enums.PlanType;
import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 套餐计划实体
 *
 * 定义系统中的套餐计划，包括套餐的基本信息、计费模式、试用配置等
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_plan")
public class BillPlan extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐代码 - 全局唯一标识
     * 如: FREE, BASIC, PRO, ENTERPRISE
     */
    private String code;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐版本号
     * 支持同一套餐的多个版本并存
     */
    private String version;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 套餐类型
     * FREE: 免费版
     * BASIC: 基础版
     * PRO: 专业版
     * ENTERPRISE: 企业版
     */
    private PlanType planType;

    /**
     * 计费模式
     * PREPAID: 预付费 - 先充值后使用
     * POSTPAID: 后付费 - 先使用后付款
     * HYBRID: 混合模式 - 优先扣余额，不足部分记账
     */
    private BillingMode billingMode;

    /**
     * 是否支持试用
     */
    private Boolean trialEnabled;

    /**
     * 试用天数
     */
    private Integer trialDays;

    /**
     * 每用户试用次数限制
     * 防止用户重复试用同一套餐
     */
    private Integer trialLimitPerUser;

    /**
     * 套餐状态
     * DRAFT: 草稿状态 - 开发中，不对外开放
     * ACTIVE: 生效中 - 正常运营状态
     * INACTIVE: 已禁用 - 暂停新订阅
     * DEPRECATED: 已弃用 - 不推荐使用，逐步下线
     * ARCHIVED: 已归档 - 仅作历史记录保存
     */
    private PlanStatus status;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 检查套餐是否可用
     */
    public boolean isAvailable() {
        if (status != PlanStatus.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查生效时间
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }

        // 检查失效时间
        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否支持试用
     */
    public boolean supportsTriad() {
        return Boolean.TRUE.equals(trialEnabled) &&
               trialDays != null &&
               trialDays > 0;
    }

    /**
     * 检查是否为免费套餐
     */
    public boolean isFree() {
        return PlanType.FREE == planType;
    }

    /**
     * 检查是否为付费套餐
     */
    public boolean isPaid() {
        return !isFree();
    }

    /**
     * 检查是否为预付费模式
     */
    public boolean isPrepaid() {
        return BillingMode.PREPAID == billingMode;
    }

    /**
     * 检查是否为后付费模式
     */
    public boolean isPostpaid() {
        return BillingMode.POSTPAID == billingMode;
    }

    /**
     * 检查是否为混合计费模式
     */
    public boolean isHybrid() {
        return BillingMode.HYBRID == billingMode;
    }

    /**
     * 设置套餐状态（枚举版本）
     */
    public void setStatus(PlanStatus status) {
        this.status = status;
    }


    /**
     * 激活套餐
     */
    public void activate() {
        this.status = PlanStatus.ACTIVE;
        if (this.effectiveTime == null) {
            this.effectiveTime = LocalDateTime.now();
        }
    }

    /**
     * 禁用套餐
     */
    public void deactivate() {
        this.status = PlanStatus.INACTIVE;
    }

    /**
     * 弃用套餐
     */
    public void deprecate() {
        this.status = PlanStatus.DEPRECATED;
    }

    /**
     * 归档套餐
     */
    public void archive() {
        this.status = PlanStatus.ARCHIVED;
        if (this.expireTime == null) {
            this.expireTime = LocalDateTime.now();
        }
    }

    /**
     * 设置试用配置
     */
    public void setTrialConfig(boolean enabled, Integer days, Integer limitPerUser) {
        this.trialEnabled = enabled;
        this.trialDays = enabled ? days : 0;
        this.trialLimitPerUser = enabled ? limitPerUser : 0;
    }
}
