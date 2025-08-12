package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 实时仪表板数据模型 */
@Data
public class DashboardData {
    private LocalDateTime generatedTime;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private long activeSubscriptions;
    private BigDecimal trialConversionRate;
    private BigDecimal monthlyRecurringRevenue;
    private BigDecimal annualRecurringRevenue;
    private List<RecentTransaction> recentTransactions;
    private Map<String, BigDecimal> revenueChartData;
}
