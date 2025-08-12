package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import lombok.Data;

/** 财务关键指标 */
@Data
public class FinancialKeyMetrics {
    private BigDecimal customerAcquisitionCost; // CAC
    private BigDecimal customerLifetimeValue; // LTV
    private BigDecimal ltvToCacRatio; // LTV/CAC
    private BigDecimal netRevenueRetention; // NRR
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
    private BigDecimal grossMargin;
}
