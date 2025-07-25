package io.github.rose.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订阅报表数据模型
 */
@Data
public class SubscriptionReport {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long trialSubscriptions;
    private long cancelledSubscriptions;
    private Map<String, Long> subscriptionsByPlan;
    private BigDecimal churnRate;
    private long newSubscriptions;
}

