package io.github.rosestack.billing.entity;

import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 账单明细项 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvoiceLineItem extends BaseTenantEntity {
    private String id;

    /** 项目描述 */
    private String description;

    /** 计量类型 */
    private String metricType;

    /** 数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 小计 */
    private BigDecimal amount;
}
