package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 租户收入数据
 */
@Data
public class TenantRevenueData {
    private String tenantId;
    private String tenantName;
    private BigDecimal revenue;
    private long invoiceCount;
    private BigDecimal averageInvoiceValue;
}
