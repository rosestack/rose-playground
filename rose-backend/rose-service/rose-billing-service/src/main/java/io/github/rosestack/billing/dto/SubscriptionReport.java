package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/** 订阅报表数据模型 */
@Data
public class SubscriptionReport {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime generatedTime;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long trialSubscriptions;
    private long cancelledSubscriptions;
    private Map<String, Long> subscriptionsByPlan;
    private BigDecimal churnRate;
    private long newSubscriptions;
}
