package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.billing.domain.feature.BillFeatureMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.billing.domain.usage.BillUsage;
import io.github.rosestack.billing.domain.usage.BillUsageMapper;
import io.github.rosestack.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用量统计服务
 *
 * 提供用量记录、统计、配额检查等核心功能
 * 支持实时用量统计、配额管理、用量告警等企业级功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillUsageService {

    private final BillUsageMapper usageMapper;
    private final BillSubscriptionMapper subscriptionMapper;
    private final BillFeatureMapper featureMapper;

    /**
     * 记录用量
     */
    @Transactional(rollbackFor = Exception.class)
    public BillUsage recordUsage(BillUsage usage) {
        log.debug("Recording usage: subscription={}, feature={}, amount={}",
                usage.getSubscriptionId(), usage.getFeatureId(), usage.getUsageAmount());

        // 验证订阅是否存在且可提供服务
        BillSubscription subscription = subscriptionMapper.selectById(usage.getSubscriptionId());
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + usage.getSubscriptionId());
        }
        if (!subscription.canProvideService()) {
            throw new BusinessException("订阅状态不允许使用服务: " + subscription.getStatus());
        }

        // 验证功能是否存在且启用
        BillFeature feature = featureMapper.selectById(usage.getFeatureId());
        if (feature == null) {
            throw new BusinessException("功能不存在: " + usage.getFeatureId());
        }
        if (!feature.isActive()) {
            throw new BusinessException("功能未启用: " + feature.getName());
        }

        // 设置默认值
        if (usage.getUsageTime() == null) {
            usage.setUsageTime(LocalDateTime.now());
        }
        if (usage.getBillingPeriod() == null) {
            usage.setBillingPeriodFromDate(usage.getUsageTime());
        }
        if (usage.getUnit() == null) {
            usage.setUnit(feature.getUnit());
        }

        // 保存用量记录
        usageMapper.insert(usage);
        log.debug("Usage recorded successfully: id={}", usage.getId());

        return usage;
    }

    /**
     * 批量记录用量
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordUsageBatch(List<BillUsage> usages) {
        log.info("Recording batch usage: {} records", usages.size());

        if (usages.isEmpty()) {
            return;
        }

        // 验证所有用量记录
        for (BillUsage usage : usages) {
            validateUsageRecord(usage);
        }

        // 批量插入
        for (BillUsage usage : usages) {
            usageMapper.insert(usage);
        }

        log.info("Batch usage recorded successfully: {} records", usages.size());
    }

    /**
     * 记录API调用用量
     */
    public BillUsage recordApiUsage(String tenantId, Long subscriptionId, Long featureId,
                                   String endpoint, String method, int statusCode) {
        BillUsage usage = BillUsage.createApiUsage(tenantId, subscriptionId, featureId, endpoint, method, statusCode);
        return recordUsage(usage);
    }

    /**
     * 记录存储用量
     */
    public BillUsage recordStorageUsage(String tenantId, Long subscriptionId, Long featureId,
                                       BigDecimal sizeInBytes, String fileType) {
        BillUsage usage = BillUsage.createStorageUsage(tenantId, subscriptionId, featureId, sizeInBytes, fileType);
        return recordUsage(usage);
    }

    /**
     * 查询订阅在指定计费周期的用量统计
     */
    public Map<Long, BigDecimal> getUsageBySubscriptionAndPeriod(Long subscriptionId, LocalDate billingPeriod) {
        List<BillUsage> usages = usageMapper.findBySubscriptionAndPeriod(subscriptionId, billingPeriod);

        return usages.stream()
                .collect(Collectors.groupingBy(
                        BillUsage::getFeatureId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                usage -> usage.getUsageAmount() != null ? usage.getUsageAmount() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));
    }

    /**
     * 查询功能在指定计费周期的总用量
     */
    public BigDecimal getFeatureUsageInPeriod(Long subscriptionId, Long featureId, LocalDate billingPeriod) {
        return usageMapper.sumUsageBySubscriptionFeatureAndPeriod(subscriptionId, featureId, billingPeriod);
    }

    /**
     * 查询当月用量统计
     */
    public Map<Long, BigDecimal> getCurrentMonthUsage(Long subscriptionId) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return getUsageBySubscriptionAndPeriod(subscriptionId, currentMonth);
    }

    /**
     * 查询用量趋势（最近几个月）
     */
    public Map<LocalDate, Map<Long, BigDecimal>> getUsageTrend(Long subscriptionId, int months) {
        LocalDate endDate = LocalDate.now().withDayOfMonth(1);
        LocalDate startDate = endDate.minusMonths(months - 1);

        return startDate.datesUntil(endDate.plusMonths(1), java.time.Period.ofMonths(1))
                .collect(Collectors.toMap(
                        date -> date,
                        date -> getUsageBySubscriptionAndPeriod(subscriptionId, date)
                ));
    }

    /**
     * 检查配额是否充足
     */
    public boolean checkQuotaAvailable(Long subscriptionId, Long featureId, BigDecimal requestedAmount) {
        // TODO: 实现配额检查逻辑
        // 1. 获取套餐中该功能的配额限制
        // 2. 计算当前周期已使用量
        // 3. 判断剩余配额是否足够

        log.debug("Checking quota for subscription={}, feature={}, requested={}",
                subscriptionId, featureId, requestedAmount);

        // 临时实现：总是返回true
        return true;
    }

    /**
     * 获取配额使用情况
     */
    public QuotaUsage getQuotaUsage(Long subscriptionId, Long featureId) {
        // TODO: 实现配额使用情况查询
        // 1. 获取套餐中该功能的配额限制
        // 2. 计算当前周期已使用量
        // 3. 计算剩余配额和使用率

        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);
        BigDecimal usedAmount = getFeatureUsageInPeriod(subscriptionId, featureId, currentPeriod);

        return new QuotaUsage(
                BigDecimal.valueOf(1000), // 临时配额限制
                usedAmount,
                BigDecimal.valueOf(1000).subtract(usedAmount)
        );
    }

    /**
     * 清理历史用量数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanupHistoricalUsage(int monthsToKeep) {
        log.info("Cleaning up historical usage data, keeping {} months", monthsToKeep);

        LocalDate cutoffDate = LocalDate.now().minusMonths(monthsToKeep).withDayOfMonth(1);
        usageMapper.deleteUsageBeforePeriod(cutoffDate);

        log.info("Historical usage data cleaned up before: {}", cutoffDate);
    }

    /**
     * 获取用量统计报表数据
     */
    public UsageReport generateUsageReport(Long subscriptionId, LocalDate startDate, LocalDate endDate) {
        List<BillUsage> usages = usageMapper.findBySubscriptionAndTimeRange(
                subscriptionId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        Map<Long, BigDecimal> featureUsages = usages.stream()
                .collect(Collectors.groupingBy(
                        BillUsage::getFeatureId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                usage -> usage.getUsageAmount() != null ? usage.getUsageAmount() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));

        Map<LocalDate, BigDecimal> dailyUsages = usages.stream()
                .collect(Collectors.groupingBy(
                        usage -> usage.getUsageTime().toLocalDate(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                usage -> usage.getUsageAmount() != null ? usage.getUsageAmount() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));

        return new UsageReport(subscriptionId, startDate, endDate, featureUsages, dailyUsages);
    }

    /**
     * 验证用量记录
     */
    private void validateUsageRecord(BillUsage usage) {
        if (usage.getSubscriptionId() == null) {
            throw new BusinessException("订阅ID不能为空");
        }
        if (usage.getFeatureId() == null) {
            throw new BusinessException("功能ID不能为空");
        }
        if (usage.getUsageAmount() == null || usage.getUsageAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("用量必须为非负数");
        }
    }

    /**
     * 配额使用情况
     */
    public static class QuotaUsage {
        private final BigDecimal totalQuota;
        private final BigDecimal usedAmount;
        private final BigDecimal remainingQuota;

        public QuotaUsage(BigDecimal totalQuota, BigDecimal usedAmount, BigDecimal remainingQuota) {
            this.totalQuota = totalQuota;
            this.usedAmount = usedAmount;
            this.remainingQuota = remainingQuota;
        }

        public BigDecimal getTotalQuota() { return totalQuota; }
        public BigDecimal getUsedAmount() { return usedAmount; }
        public BigDecimal getRemainingQuota() { return remainingQuota; }

        public BigDecimal getUsagePercentage() {
            if (totalQuota == null || totalQuota.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return usedAmount.divide(totalQuota, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 用量报表
     */
    public static class UsageReport {
        private final Long subscriptionId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<Long, BigDecimal> featureUsages;
        private final Map<LocalDate, BigDecimal> dailyUsages;

        public UsageReport(Long subscriptionId, LocalDate startDate, LocalDate endDate,
                          Map<Long, BigDecimal> featureUsages, Map<LocalDate, BigDecimal> dailyUsages) {
            this.subscriptionId = subscriptionId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.featureUsages = featureUsages;
            this.dailyUsages = dailyUsages;
        }

        public Long getSubscriptionId() { return subscriptionId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<Long, BigDecimal> getFeatureUsages() { return featureUsages; }
        public Map<LocalDate, BigDecimal> getDailyUsages() { return dailyUsages; }
    }
}
