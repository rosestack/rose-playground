package io.github.rosestack.billing.domain.subscription;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.SubscriptionStatus;
import io.github.rosestack.core.model.HasStatus;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订阅实体
 *
 * 管理用户对套餐的订阅关系，包括订阅状态、时间周期、席位数量等
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_subscription")
public class BillSubscription extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订阅编号 - 全局唯一标识
     */
    private String subNo;

    /**
     * 套餐ID
     */
    private Long planId;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 价格快照 - JSON格式存储订阅时的完整定价信息
     * 包含套餐价格和功能定价的快照，确保历史计费数据不受价格变更影响
     */
    private String pricingSnapshot;

    /**
     * 订阅席位数量
     * 用于多席位计费和PER_SEAT功能配额计算
     */
    private Integer quantity;

    /**
     * 订阅开始时间
     */
    private LocalDateTime startTime;

    /**
     * 订阅结束时间
     * NULL表示无限期订阅
     */
    private LocalDateTime endTime;

    /**
     * 当前计费周期开始时间
     */
    private LocalDateTime currentPeriodStartTime;

    /**
     * 当前计费周期结束时间
     */
    private LocalDateTime currentPeriodEndTime;

    /**
     * 下次计费时间
     */
    private LocalDateTime nextBillingTime;

    /**
     * 订阅状态
     * TRIAL: 试用中 - 用户正在试用套餐，未付费
     * ACTIVE: 活跃中 - 正常付费订阅，服务正常
     * PAST_DUE: 逾期未付 - 付费失败，但仍在宽限期内
     * SUSPENDED: 已暂停 - 服务暂停，但订阅关系保留
     * CANCELLED: 已取消 - 用户主动取消，但服务继续到周期结束
     * EXPIRED: 已过期 - 订阅已结束，服务完全停止
     */
    private SubscriptionStatus status;

    /**
     * 是否自动续费
     */
    private Boolean autoRenew;

    /**
     * 是否在周期结束时取消
     * 用于优雅取消订阅，避免立即中断服务
     */
    private Boolean cancelAtPeriodEnd;

    /**
     * 检查订阅是否激活
     */
    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.name().equals(status);
    }

    /**
     * 检查订阅是否在试用期
     */
    public boolean isTrial() {
        return SubscriptionStatus.TRIAL.name().equals(status);
    }

    /**
     * 检查订阅是否已过期
     */
    public boolean isExpired() {
        return SubscriptionStatus.EXPIRED.name().equals(status);
    }

    /**
     * 检查订阅是否已取消
     */
    public boolean isCancelled() {
        return SubscriptionStatus.CANCELLED.name().equals(status);
    }

    /**
     * 检查订阅是否可以提供服务
     */
    public boolean canProvideService() {
        return SubscriptionStatus.TRIAL.name().equals(status) ||
               SubscriptionStatus.ACTIVE.name().equals(status) ||
               SubscriptionStatus.PAST_DUE.name().equals(status) ||
               SubscriptionStatus.CANCELLED.name().equals(status); // 取消状态下继续服务到周期结束
    }

    /**
     * 检查是否在当前计费周期内
     */
    public boolean isInCurrentPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return currentPeriodStartTime != null &&
               currentPeriodEndTime != null &&
               !now.isBefore(currentPeriodStartTime) &&
               !now.isAfter(currentPeriodEndTime);
    }

    /**
     * 检查是否需要续费
     */
    public boolean needsRenewal() {
        return Boolean.TRUE.equals(autoRenew) &&
               !Boolean.TRUE.equals(cancelAtPeriodEnd) &&
               canProvideService();
    }

    /**
     * 激活订阅
     */
    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    /**
     * 暂停订阅
     */
    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }

    /**
     * 取消订阅（优雅取消）
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.autoRenew = false;
        this.cancelAtPeriodEnd = true;
    }

    /**
     * 立即取消订阅
     */
    public void cancelImmediately() {
        this.status = SubscriptionStatus.EXPIRED;
        this.autoRenew = false;
        this.endTime = LocalDateTime.now();
    }

    /**
     * 标记为过期
     */
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }

    /**
     * 设置试用状态
     */
    public void setTrial() {
        this.status = SubscriptionStatus.TRIAL;
    }

    /**
     * 试用转正式订阅
     */
    public void convertFromTrial() {
        if (this.status == SubscriptionStatus.TRIAL) {
            this.status = SubscriptionStatus.ACTIVE;
        }
    }

    /**
     * 更新计费周期
     */
    public void updateBillingPeriod(LocalDateTime periodStart, LocalDateTime periodEnd, LocalDateTime nextBilling) {
        this.currentPeriodStartTime = periodStart;
        this.currentPeriodEndTime = periodEnd;
        this.nextBillingTime = nextBilling;
    }

    /**
     * 设置逾期状态
     */
    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    /**
     * 从逾期恢复到正常状态
     */
    public void recoverFromPastDue() {
        if (this.status == SubscriptionStatus.PAST_DUE) {
            this.status = SubscriptionStatus.ACTIVE;
        }
    }

    /**
     * 获取当前周期剩余天数
     */
    public long getRemainingDaysInPeriod() {
        if (currentPeriodEndTime == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(currentPeriodEndTime)) {
            return 0;
        }

        return java.time.Duration.between(now, currentPeriodEndTime).toDays();
    }

    /**
     * 检查订阅是否即将到期（7天内）
     */
    public boolean isExpiringSoon() {
        return getRemainingDaysInPeriod() <= 7;
    }
}
