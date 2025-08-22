package io.github.rosestack.billing.domain.trial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.TrialStatus;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 试用记录实体
 *
 * 跟踪用户对套餐的试用历史，防止重复试用
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_trial_record")
public class BillTrialRecord extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐ID
     */
    private Long planId;

    /**
     * 试用开始时间
     */
    private LocalDateTime trialStartTime;

    /**
     * 试用结束时间
     */
    private LocalDateTime trialEndTime;

    /**
     * 试用状态
     * ACTIVE: 试用中 - 试用期内，用户正在使用套餐功能
     * EXPIRED: 已过期 - 试用期结束，未转换为付费用户
     * CONVERTED: 已转换 - 试用期内或结束后转换为付费用户
     * CANCELLED: 已取消 - 用户主动取消试用
     */
    private TrialStatus status;

    /**
     * 转换为付费时间
     */
    private LocalDateTime convertedTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 检查试用是否激活
     */
    public boolean isActive() {
        return TrialStatus.ACTIVE.equals(status);
    }

    /**
     * 检查试用是否已过期
     */
    public boolean isExpired() {
        return TrialStatus.EXPIRED.equals(status);
    }

    /**
     * 检查试用是否已转换
     */
    public boolean isConverted() {
        return TrialStatus.CONVERTED.equals(status);
    }

    /**
     * 检查试用是否已取消
     */
    public boolean isCancelled() {
        return TrialStatus.CANCELLED.equals(status);
    }

    /**
     * 检查试用是否仍在有效期内
     */
    public boolean isWithinTrialPeriod() {
        if (!isActive()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return trialStartTime != null &&
               trialEndTime != null &&
               !now.isBefore(trialStartTime) &&
               !now.isAfter(trialEndTime);
    }

    /**
     * 获取试用剩余天数
     */
    public long getRemainingDays() {
        if (trialEndTime == null || !isActive()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(trialEndTime)) {
            return 0;
        }

        return java.time.Duration.between(now, trialEndTime).toDays();
    }

    /**
     * 检查试用是否即将到期（3天内）
     */
    public boolean isExpiringSoon() {
        return isActive() && getRemainingDays() <= 3;
    }

    /**
     * 获取试用总天数
     */
    public long getTrialDays() {
        if (trialStartTime == null || trialEndTime == null) {
            return 0;
        }

        return java.time.Duration.between(trialStartTime, trialEndTime).toDays();
    }

    /**
     * 转换为付费用户
     */
    public void convertToPaid() {
        this.status = TrialStatus.CONVERTED;
        this.convertedTime = LocalDateTime.now();
    }

    /**
     * 标记试用过期
     */
    public void markExpired() {
        this.status = TrialStatus.EXPIRED;
    }

    /**
     * 取消试用
     */
    public void cancel(String reason) {
        this.status = TrialStatus.CANCELLED;
        this.cancelReason = reason;
    }

    /**
     * 开始试用
     */
    public void startTrial() {
        this.status = TrialStatus.ACTIVE;
        this.trialStartTime = LocalDateTime.now();
    }

    /**
     * 设置试用期限
     */
    public void setTrialPeriod(int days) {
        if (this.trialStartTime == null) {
            this.trialStartTime = LocalDateTime.now();
        }
        this.trialEndTime = this.trialStartTime.plusDays(days);
    }

    /**
     * 创建试用记录的静态工厂方法
     */
    public static BillTrialRecord createTrialRecord(String tenantId, Long planId, int trialDays) {
        BillTrialRecord record = new BillTrialRecord();
        record.setTenantId(tenantId);
        record.setPlanId(planId);
        record.startTrial();
        record.setTrialPeriod(trialDays);
        return record;
    }
}
