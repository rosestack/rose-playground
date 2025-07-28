package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单实体
 *
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("invoice")
public class Invoice extends BaseTenantEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 账单号
     */
    @TableField("invoice_number")
    private String invoiceNumber;

    /**
     * 订阅ID
     */
    @TableField("subscription_id")
    private String subscriptionId;

    /**
     * 计费周期开始日期
     */
    @TableField("period_start")
    private LocalDate periodStart;

    /**
     * 计费周期结束日期
     */
    @TableField("period_end")
    private LocalDate periodEnd;

    /**
     * 基础费用
     */
    @TableField("base_amount")
    private BigDecimal baseAmount;

    /**
     * 使用量费用
     */
    @TableField("usage_amount")
    private BigDecimal usageAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 税费
     */
    private BigDecimal taxAmount;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 账单状态
     */
    private InvoiceStatus status;

    /**
     * 到期日期
     */
    private LocalDate dueDate;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付交易ID
     */
    private String paymentTransactionId;

    /**
     * 账单明细项
     */
    private List<InvoiceLineItem> lineItems;

    /**
     * 备注
     */
    private String notes;

}


