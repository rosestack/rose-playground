package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 收入报表数据模型 */
@Data
public class RevenueReport {
    private String reportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime generatedTime;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> revenueByPeriod;
    private Map<String, BigDecimal> revenueByPlan;
    private List<TenantRevenueData> topTenantsByRevenue;
    private BigDecimal growthRate;
    private BigDecimal averageOrderValue;
}
