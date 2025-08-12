package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

/**
 * 租户使用量数据
 */
@Data
public class TenantUsageData {
    private String tenantId;
    private String tenantName;
    private Map<String, BigDecimal> usageByType;
    private BigDecimal totalUsage;
}
