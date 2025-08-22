package io.github.rosestack.billing.domain.invoice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.InvoiceItemType;
import io.github.rosestack.billing.domain.enums.TargetType;
import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单明细实体类
 *
 * 记录账单中的每个明细项目，支持套餐费用、功能费用、折扣、税费等
 * 提供灵活的明细管理和计算功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_invoice_item")
public class BillInvoiceItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联账单ID
     */
    private Long invoiceId;

    /**
     * 明细类型
     * PLAN: 套餐费用
     * FEATURE: 功能费用
     * DISCOUNT: 折扣
     * TAX: 税费
     * ADJUSTMENT: 调整
     */
    private InvoiceItemType itemType;

    /**
     * 目标类型
     * 当itemType为PLAN或FEATURE时必填
     */
    private TargetType targetType;

    /**
     * 目标ID
     * 指向具体的套餐或功能ID
     */
    private Long targetId;

    /**
     * 明细描述
     * 用户友好的描述信息
     */
    private String description;

    /**
     * 数量
     * 套餐席位数、功能使用次数等
     */
    private BigDecimal quantity;

    /**
     * 单价
     * 每单位的价格
     */
    private BigDecimal unitPrice;

    /**
     * 总金额
     * quantity * unitPrice 的结果
     */
    private BigDecimal amount;

    /**
     * 货币单位
     */
    private String currency;

    /**
     * 计费周期开始日期
     */
    private LocalDate billingPeriodStart;

    /**
     * 计费周期结束日期
     */
    private LocalDate billingPeriodEnd;

    /**
     * 明细元数据
     * JSON格式，存储额外的明细信息
     */
    private String metadata;

    /**
     * 检查是否为费用项
     */
    public boolean isChargeItem() {
        return itemType != null && itemType.isChargeItem();
    }

    /**
     * 检查是否为优惠项
     */
    public boolean isDiscountItem() {
        return itemType != null && itemType.isDiscountItem();
    }

    /**
     * 检查是否需要关联目标对象
     */
    public boolean requiresTarget() {
        return itemType != null && itemType.requiresTarget();
    }

    /**
     * 检查是否为套餐相关明细
     */
    public boolean isPlanRelated() {
        return itemType != null && itemType.isPlanRelated();
    }

    /**
     * 检查是否为功能相关明细
     */
    public boolean isFeatureRelated() {
        return itemType != null && itemType.isFeatureRelated();
    }

    /**
     * 检查是否为税费相关明细
     */
    public boolean isTaxRelated() {
        return itemType != null && itemType.isTaxRelated();
    }

    /**
     * 计算总金额
     */
    public void calculateAmount() {
        if (quantity != null && unitPrice != null) {
            this.amount = quantity.multiply(unitPrice);
        } else {
            this.amount = BigDecimal.ZERO;
        }
    }

    /**
     * 验证明细数据是否有效
     */
    public boolean isValidItem() {
        // 基本字段验证
        if (invoiceId == null || itemType == null || description == null || description.trim().isEmpty()) {
            return false;
        }

        // 数量和单价验证
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        if (unitPrice == null) {
            return false;
        }

        // 金额验证
        if (amount == null) {
            return false;
        }

        // 目标对象验证
        if (requiresTarget() && (targetType == null || targetId == null)) {
            return false;
        }

        // 目标类型一致性验证
        if (targetType != null && targetId != null) {
            if (isPlanRelated() && targetType != TargetType.PLAN) {
                return false;
            }
            if (isFeatureRelated() && targetType != TargetType.FEATURE) {
                return false;
            }
        }

        // 计费周期验证
        if (billingPeriodStart != null && billingPeriodEnd != null 
            && billingPeriodStart.isAfter(billingPeriodEnd)) {
            return false;
        }

        return true;
    }

    /**
     * 获取明细显示信息
     */
    public String getItemDisplay() {
        return String.format("%s - %.2f %s x %s = %.2f %s",
            description,
            unitPrice != null ? unitPrice : BigDecimal.ZERO,
            currency != null ? currency : "USD",
            quantity != null ? quantity : BigDecimal.ZERO,
            amount != null ? amount : BigDecimal.ZERO,
            currency != null ? currency : "USD");
    }

    /**
     * 获取计费周期显示信息
     */
    public String getBillingPeriodDisplay() {
        if (billingPeriodStart == null || billingPeriodEnd == null) {
            return "";
        }
        return String.format("%s ~ %s", billingPeriodStart, billingPeriodEnd);
    }

    /**
     * 检查是否为正向费用（收费）
     */
    public boolean isPositiveAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否为负向费用（退费或折扣）
     */
    public boolean isNegativeAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 创建套餐费用明细
     */
    public static BillInvoiceItem createPlanItem(Long invoiceId, Long planId, String description, 
            BigDecimal quantity, BigDecimal unitPrice, String currency) {
        BillInvoiceItem item = new BillInvoiceItem();
        item.setInvoiceId(invoiceId);
        item.setItemType(InvoiceItemType.PLAN);
        item.setTargetType(TargetType.PLAN);
        item.setTargetId(planId);
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setCurrency(currency);
        item.calculateAmount();
        return item;
    }

    /**
     * 创建功能费用明细
     */
    public static BillInvoiceItem createFeatureItem(Long invoiceId, Long featureId, String description,
            BigDecimal quantity, BigDecimal unitPrice, String currency) {
        BillInvoiceItem item = new BillInvoiceItem();
        item.setInvoiceId(invoiceId);
        item.setItemType(InvoiceItemType.FEATURE);
        item.setTargetType(TargetType.FEATURE);
        item.setTargetId(featureId);
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setCurrency(currency);
        item.calculateAmount();
        return item;
    }

    /**
     * 创建折扣明细
     */
    public static BillInvoiceItem createDiscountItem(Long invoiceId, String description, 
            BigDecimal discountAmount, String currency) {
        BillInvoiceItem item = new BillInvoiceItem();
        item.setInvoiceId(invoiceId);
        item.setItemType(InvoiceItemType.DISCOUNT);
        item.setDescription(description);
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(discountAmount.negate()); // 折扣为负数
        item.setAmount(discountAmount.negate());
        item.setCurrency(currency);
        return item;
    }

    /**
     * 创建税费明细
     */
    public static BillInvoiceItem createTaxItem(Long invoiceId, String description, 
            BigDecimal taxAmount, String currency) {
        BillInvoiceItem item = new BillInvoiceItem();
        item.setInvoiceId(invoiceId);
        item.setItemType(InvoiceItemType.TAX);
        item.setDescription(description);
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(taxAmount);
        item.setAmount(taxAmount);
        item.setCurrency(currency);
        return item;
    }

    /**
     * 创建调整明细
     */
    public static BillInvoiceItem createAdjustmentItem(Long invoiceId, String description, 
            BigDecimal adjustmentAmount, String currency) {
        BillInvoiceItem item = new BillInvoiceItem();
        item.setInvoiceId(invoiceId);
        item.setItemType(InvoiceItemType.ADJUSTMENT);
        item.setDescription(description);
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(adjustmentAmount);
        item.setAmount(adjustmentAmount);
        item.setCurrency(currency);
        return item;
    }
}