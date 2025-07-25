package io.github.rose.billing.entity;

import io.github.rose.core.entity.BaseTenantEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单明细项
 */
@Data
public class InvoiceLineItem extends BaseTenantEntity{
    private String id;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 计量类型
     */
    private String metricType;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 小计
     */
    private BigDecimal amount;
}