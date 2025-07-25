package io.github.rosestack.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; /**
 * 使用量报表数据模型
 */
@Data
public class UsageReport {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;
    private Map<String, BigDecimal> usageByType;
    private List<TenantUsageData> topTenantsByUsage;
    private Map<String, BigDecimal> usageTrend;
}
