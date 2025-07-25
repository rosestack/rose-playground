package io.github.rosestack.billing.service;

import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定价计算器
 *
 * @author rose
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PricingCalculator {

    private final UsageRecordRepository usageRepository;

    /**
     * 计算使用量费用
     */
    public BigDecimal calculateUsageAmount(String tenantId, LocalDateTime periodStart,
                                         LocalDateTime periodEnd, SubscriptionPlan plan) {

        Map<String, BigDecimal> usagePricing = plan.getUsagePricing();
        if (usagePricing == null || usagePricing.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : usagePricing.entrySet()) {
            String metricType = entry.getKey();
            BigDecimal unitPrice = entry.getValue();

            // 获取该时间段内的使用量
            BigDecimal usage = usageRepository.sumUsageByTenantAndMetricAndPeriod(
                tenantId, metricType, periodStart, periodEnd);

            if (usage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal amount = usage.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
                totalAmount = totalAmount.add(amount);

                log.debug("计算使用量费用 - 租户: {}, 类型: {}, 用量: {}, 单价: {}, 金额: {}",
                    tenantId, metricType, usage, unitPrice, amount);
            }
        }

        return totalAmount;
    }

    /**
     * 检查使用量限制
     */
    public boolean checkLimit(SubscriptionPlan plan, String metricType, BigDecimal currentUsage) {
        Map<String, Object> features = plan.getFeatures();
        if (features == null) {
            return true;
        }

        Object limitObj = features.get(metricType + "_limit");
        if (limitObj == null) {
            return true; // 没有限制
        }

        try {
            BigDecimal limit = new BigDecimal(limitObj.toString());
            return currentUsage.compareTo(limit) <= 0;
        } catch (NumberFormatException e) {
            log.warn("无法解析限制值: {}", limitObj);
            return true;
        }
    }

    /**
     * 计算阶梯定价
     */
    public BigDecimal calculateTieredPricing(BigDecimal usage, Map<String, BigDecimal> tiers) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal remainingUsage = usage;

        // 示例阶梯定价：
        // 0-1000: $0.10/unit
        // 1001-5000: $0.08/unit
        // 5001+: $0.05/unit

        for (Map.Entry<String, BigDecimal> tier : tiers.entrySet()) {
            String[] range = tier.getKey().split("-");
            BigDecimal tierStart = new BigDecimal(range[0]);
            BigDecimal tierEnd = range.length > 1 ? new BigDecimal(range[1]) : new BigDecimal(Long.MAX_VALUE);
            BigDecimal tierPrice = tier.getValue();

            if (remainingUsage.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tierUsage = remainingUsage.min(tierEnd.subtract(tierStart).add(BigDecimal.ONE));
            totalAmount = totalAmount.add(tierUsage.multiply(tierPrice));
            remainingUsage = remainingUsage.subtract(tierUsage);
        }

        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算折扣
     */
    public BigDecimal calculateDiscount(String tenantId, BigDecimal amount, String discountCode) {
        // 实现促销码、长期订阅折扣等逻辑
        if ("WELCOME10".equals(discountCode)) {
            return amount.multiply(new BigDecimal("0.1")); // 10%折扣
        }
        return BigDecimal.ZERO;
    }
}
