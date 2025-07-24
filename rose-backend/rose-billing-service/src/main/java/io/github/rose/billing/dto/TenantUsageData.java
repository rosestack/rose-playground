package io.github.rose.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map; /**
 * 租户使用量数据
 */
@Data
public class TenantUsageData {
    private String tenantId;
    private String tenantName;
    private Map<String, BigDecimal> usageByType;
    private BigDecimal totalUsage;
}
