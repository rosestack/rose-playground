package io.github.rose.billing.domain.report;

import lombok.Data;

import java.time.LocalDateTime; /**
 * 综合财务报表数据模型
 */
@Data
public class ComprehensiveFinancialReport {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;
    private RevenueReport revenueReport;
    private SubscriptionReport subscriptionReport;
    private UsageReport usageReport;
    private FinancialKeyMetrics keyMetrics;
}
