package io.github.rosestack.billing.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 综合财务报表数据模型
 */
@Data
public class ComprehensiveFinancialReport {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime generatedTime;
    private RevenueReport revenueReport;
    private SubscriptionReport subscriptionReport;
    private UsageReport usageReport;
    private FinancialKeyMetrics keyMetrics;
}
