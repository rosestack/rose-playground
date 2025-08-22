package io.github.rosestack.billing.domain.payment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.PaymentMethod;
import io.github.rosestack.billing.domain.enums.PaymentStatus;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 *
 * 记录所有支付相关的信息，包括支付状态、金额、方式等
 * 支持多种支付网关和支付方式
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_payment")
public class BillPayment extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付编号
     * 系统生成的唯一支付标识
     */
    private String paymentNo;

    /**
     * 关联账单ID
     * 可为空，支持余额充值等场景
     */
    private Long invoiceId;

    /**
     * 支付金额
     * 必须大于0
     */
    private BigDecimal amount;

    /**
     * 货币单位
     * 默认USD，支持多种货币
     */
    private String currency;

    /**
     * 支付方式
     * 信用卡、借记卡、PayPal等
     */
    private PaymentMethod paymentMethod;

    /**
     * 支付网关
     * 如：stripe、paypal、alipay等
     */
    private String paymentGateway;

    /**
     * 网关交易ID
     * 第三方支付平台返回的交易标识
     */
    private String gatewayTransactionId;

    /**
     * 支付状态
     * PENDING、SUCCESS、FAILED、CANCELLED、REFUNDED
     */
    private PaymentStatus status;

    /**
     * 支付完成时间
     * 实际支付成功的时间
     */
    private LocalDateTime paidTime;

    /**
     * 失败原因
     * 支付失败时记录详细原因
     */
    private String failureReason;

    /**
     * 检查支付是否成功
     */
    public boolean isSuccessful() {
        return status != null && status.isSuccessful();
    }

    /**
     * 检查支付是否失败
     */
    public boolean isFailed() {
        return status != null && status.isFailed();
    }

    /**
     * 检查支付是否在处理中
     */
    public boolean isPending() {
        return status != null && status.isPending();
    }

    /**
     * 检查是否可以退款
     */
    public boolean canRefund() {
        return status != null && status.canRefund();
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return status != null && status.canRetry();
    }

    /**
     * 检查支付是否为最终状态
     */
    public boolean isFinalStatus() {
        return status != null && status.isFinalStatus();
    }

    /**
     * 标记支付成功
     */
    public void markAsSuccessful(String gatewayTransactionId) {
        this.status = PaymentStatus.SUCCESS;
        this.gatewayTransactionId = gatewayTransactionId;
        this.paidTime = LocalDateTime.now();
        this.failureReason = null; // 清除之前的失败原因
    }

    /**
     * 标记支付失败
     */
    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.paidTime = null;
    }

    /**
     * 标记支付取消
     */
    public void markAsCancelled(String reason) {
        this.status = PaymentStatus.CANCELLED;
        this.failureReason = reason;
        this.paidTime = null;
    }

    /**
     * 标记支付已退款
     */
    public void markAsRefunded() {
        if (!canRefund()) {
            throw new IllegalStateException("Payment cannot be refunded in current status: " + status);
        }
        this.status = PaymentStatus.REFUNDED;
    }

    /**
     * 验证支付记录是否有效
     */
    public boolean isValidPayment() {
        // 基本字段验证
        if (paymentNo == null || paymentNo.trim().isEmpty()) {
            return false;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (paymentMethod == null) {
            return false;
        }

        if (status == null) {
            return false;
        }

        // 状态一致性验证
        if (isSuccessful() && paidTime == null) {
            return false;
        }

        if (isFailed() && (failureReason == null || failureReason.trim().isEmpty())) {
            return false;
        }

        // 网关信息验证
        if (isSuccessful() && paymentMethod.isOnlinePayment() 
            && (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty())) {
            return false;
        }

        return true;
    }

    /**
     * 获取支付显示信息
     */
    public String getPaymentDisplay() {
        return String.format("%s - %s %.2f %s", 
            paymentNo, 
            status.getDescription(),
            amount != null ? amount : BigDecimal.ZERO, 
            currency != null ? currency : "USD");
    }

    /**
     * 获取支付方式显示信息
     */
    public String getPaymentMethodDisplay() {
        String display = paymentMethod.getDescription();
        if (paymentGateway != null && !paymentGateway.trim().isEmpty()) {
            display += " (" + paymentGateway + ")";
        }
        return display;
    }

    /**
     * 检查是否需要手动处理
     */
    public boolean requiresManualProcessing() {
        return paymentMethod != null && paymentMethod.requiresManualProcessing();
    }

    /**
     * 检查是否支持自动续费
     */
    public boolean supportsAutoRenewal() {
        return paymentMethod != null && paymentMethod.supportsAutoRenewal();
    }

    /**
     * 获取处理时长（从创建到支付完成的时长，分钟）
     */
    public Long getProcessingTimeInMinutes() {
        if (getCreatedTime() == null || paidTime == null) {
            return null;
        }
        return java.time.Duration.between(getCreatedTime(), paidTime).toMinutes();
    }
}