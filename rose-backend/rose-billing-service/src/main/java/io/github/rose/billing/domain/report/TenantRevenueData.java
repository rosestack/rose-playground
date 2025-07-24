package io.github.rose.billing.domain.report;

import lombok.Data;

import java.math.BigDecimal; /**
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
