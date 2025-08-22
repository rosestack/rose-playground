package io.github.rosestack.billing.domain.invoice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.BillStatus;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单实体
 *
 * 管理订阅产生的账单信息，包括账单金额、状态、支付信息等
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_invoice")
public class BillInvoice extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账单编号 - 全局唯一标识
     */
    private String billNo;

    /**
     * 订阅ID
     */
    private Long subscriptionId;

    /**
     * 计费周期开始日期
     */
    private LocalDate periodStart;

    /**
     * 计费周期结束日期
     */
    private LocalDate periodEnd;

    /**
     * 账单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 未支付金额
     */
    private BigDecimal unpaidAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 税费金额
     */
    private BigDecimal taxAmount;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 账单状态
     * DRAFT: 草稿 - 账单生成中或待确认
     * PENDING: 待付 - 账单已生成，等待支付
     * PAID: 已付 - 账单已完全支付
     * OVERDUE: 逾期 - 账单超过支付期限
     * CANCELLED: 已取消 - 账单已取消或作废
     * REFUNDED: 已退款 - 账单已退款
     */
    private BillStatus status;

    /**
     * 账单到期日期
     */
    private LocalDate dueDate;

    /**
     * 支付完成时间
     */
    private LocalDateTime paidTime;

    /**
     * 账单详情 - JSON格式存储账单明细
     * 包含各功能的用量和费用明细
     */
    private String billDetails;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 检查账单是否已支付
     */
    public boolean isPaid() {
        return BillStatus.PAID == status;
    }

    /**
     * 检查账单是否部分支付
     */
    public boolean isPartialPaid() {
        // 在新的设计中，我们不再有独立的PARTIAL_PAID状态
        // 而是根据已支付金额和总金额来判断
        return BillStatus.PENDING == status &&
               paidAmount != null &&
               paidAmount.compareTo(BigDecimal.ZERO) > 0 &&
               paidAmount.compareTo(totalAmount) < 0;
    }

    /**
     * 检查账单是否逾期
     */
    public boolean isOverdue() {
        return BillStatus.OVERDUE == status ||
               (BillStatus.PENDING == status && dueDate != null && dueDate.isBefore(LocalDate.now()));
    }

    /**
     * 检查账单是否可以支付
     */
    public boolean canBePaid() {
        return BillStatus.PENDING == status ||
               BillStatus.OVERDUE == status;
    }

    /**
     * 检查账单是否可以取消
     */
    public boolean canBeVoided() {
        return BillStatus.DRAFT == status ||
               BillStatus.PENDING == status;
    }

    /**
     * 获取剩余应付金额
     */
    public BigDecimal getRemainingAmount() {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }
        if (paidAmount == null) {
            return totalAmount;
        }
        return totalAmount.subtract(paidAmount);
    }

    /**
     * 记录支付
     */
    public void recordPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }

        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        paidAmount = paidAmount.add(amount);

        // 更新未支付金额
        unpaidAmount = getRemainingAmount();

        // 更新账单状态
        if (unpaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            status = BillStatus.PAID;
            paidTime = LocalDateTime.now();
        }
        // 注意：在新设计中，我们不再有独立的PARTIAL_PAID状态
        // 部分支付状态通过isPartialPaid()方法来判断
    }

    /**
     * 标记为逾期
     */
    public void markAsOverdue() {
        if (canBePaid()) {
            this.status = BillStatus.OVERDUE;
        }
    }

    /**
     * 作废账单
     */
    public void voidBill() {
        if (canBeVoided()) {
            this.status = BillStatus.CANCELLED;
        } else {
            throw new IllegalStateException("账单状态不允许作废: " + status);
        }
    }

    /**
     * 标记为已退款
     */
    public void markAsRefunded() {
        this.status = BillStatus.REFUNDED;
        this.unpaidAmount = BigDecimal.ZERO;
    }

    /**
     * 发布账单（从草稿状态到待付状态）
     */
    public void publish() {
        if (BillStatus.DRAFT == status) {
            this.status = BillStatus.PENDING;
            this.unpaidAmount = this.totalAmount;
        } else {
            throw new IllegalStateException("只有草稿状态的账单可以发布: " + status);
        }
    }

    /**
     * 计算支付进度百分比
     */
    public BigDecimal getPaymentProgress() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (paidAmount == null) {
            return BigDecimal.ZERO;
        }
        return paidAmount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取账单描述
     */
    public String getBillDescription() {
        if (periodStart != null && periodEnd != null) {
            return String.format("计费周期: %s 至 %s", periodStart, periodEnd);
        }
        return "账单";
    }
}
