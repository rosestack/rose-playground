package io.github.rose.billing.domain.report;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; /**
 * 收入报表数据模型
 */
@Data
public class RevenueReport {
    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> revenueByPeriod;
    private Map<String, BigDecimal> revenueByPlan;
    private List<TenantRevenueData> topTenantsByRevenue;
    private BigDecimal growthRate;
    private BigDecimal averageOrderValue;
}
