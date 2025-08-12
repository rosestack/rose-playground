package io.github.rosestack.billing.service;

import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.enums.BillingType;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定价计算器
 *
 * @author rose
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PricingCalculator {
    public static final BigDecimal defaultTaxRate = new BigDecimal("0.1");

    private final UsageRecordRepository usageRepository;

    /**
     * 计算使用量费用
     *
     * @param tenantId    租户ID
     * @param periodStart 计费周期开始时间
     * @param periodEnd   计费周期结束时间
     * @param plan        订阅计划
     * @return 使用量费用总额
     */
    public BigDecimal calculateUsageAmount(
            String tenantId, LocalDateTime periodStart, LocalDateTime periodEnd, SubscriptionPlan plan) {

        Map<String, BigDecimal> usagePricing = plan.getUsagePricing();
        if (usagePricing == null || usagePricing.isEmpty()) {
            log.debug("订阅计划 {} 没有配置使用量定价", plan.getId());
            return BigDecimal.ZERO;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 性能优化：批量查询所有计量类型的使用量
        Map<String, BigDecimal> usageMap = new HashMap<>();
        for (String metricType : usagePricing.keySet()) {
            BigDecimal usage =
                    usageRepository.sumUsageByTenantAndMetricAndPeriod(tenantId, metricType, periodStart, periodEnd);
            usageMap.put(metricType, usage);
        }

        // 计算费用
        for (Map.Entry<String, BigDecimal> entry : usagePricing.entrySet()) {
            String metricType = entry.getKey();
            BigDecimal unitPrice = entry.getValue();
            BigDecimal usage = usageMap.get(metricType);

            if (usage != null && usage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal amount = usage.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
                totalAmount = totalAmount.add(amount);

                log.debug(
                        "计算使用量费用 - 租户: {}, 类型: {}, 用量: {}, 单价: {}, 金额: {}",
                        tenantId,
                        metricType,
                        usage,
                        unitPrice,
                        amount);
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

            BigDecimal tierUsage =
                    remainingUsage.min(tierEnd.subtract(tierStart).add(BigDecimal.ONE));
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

    /**
     * 计算基础订阅费用
     */
    public BigDecimal calculateBasePrice(SubscriptionPlan plan, LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (plan.getBillingType() == BillingType.USAGE_BASED) {
            return BigDecimal.ZERO;
        }

        BigDecimal basePrice = plan.getBasePrice();

        // 如果是按月计费，计算实际使用天数的比例
        if (plan.getBillingType() == BillingType.MONTHLY) {
            long totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd);
            if (totalDays != 30) { // 非完整月份，按比例计算
                BigDecimal ratio =
                        BigDecimal.valueOf(totalDays).divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
                basePrice = basePrice.multiply(ratio);
            }
        }

        return basePrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算税费
     */
    public BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(defaultTaxRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算总费用
     */
    public BigDecimal calculateTotalAmount(
            String tenantId,
            SubscriptionPlan plan,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String discountCode) {
        log.info("开始计算总费用: tenantId={}, planId={}, period={} to {}", tenantId, plan.getId(), periodStart, periodEnd);

        // 基础费用
        BigDecimal baseAmount = calculateBasePrice(plan, periodStart, periodEnd);

        // 使用量费用
        BigDecimal usageAmount = calculateUsageAmount(tenantId, periodStart, periodEnd, plan);

        // 小计
        BigDecimal subtotal = baseAmount.add(usageAmount);

        // 折扣
        BigDecimal discountAmount = calculateDiscount(tenantId, subtotal, discountCode);
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        // 税费
        BigDecimal taxAmount = calculateTax(afterDiscount);
        BigDecimal totalAmount = afterDiscount.add(taxAmount);

        log.info(
                "费用计算完成: base={}, usage={}, discount={}, tax={}, total={}",
                baseAmount,
                usageAmount,
                discountAmount,
                taxAmount,
                totalAmount);

        return totalAmount;
    }

    /**
     * 验证使用量是否超限
     */
    public boolean isUsageExceeded(String tenantId, SubscriptionPlan plan, String metricType) {
        // 获取当前计费周期的使用量
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart =
                now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        BigDecimal currentUsage =
                usageRepository.sumUsageByTenantAndMetricAndPeriod(tenantId, metricType, periodStart, now);

        return !checkLimit(plan, metricType, currentUsage);
    }
}
