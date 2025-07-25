package io.github.rose.billing.entity;

import io.github.rose.billing.enums.InvoiceStatus;
import io.github.rose.core.entity.BaseTenantEntity;
import lombok.Data;

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
public class Invoice extends BaseTenantEntity {
    private String id;

    /**
     * 账单号
     */
    private String invoiceNumber;

    /**
     * 订阅ID
     */
    private String subscriptionId;

    /**
     * 计费周期开始日期
     */
    private LocalDate periodStart;

    /**
     * 计费周期结束日期
     */
    private LocalDate periodEnd;

    /**
     * 基础费用
     */
    private BigDecimal baseAmount;

    /**
     * 使用量费用
     */
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


