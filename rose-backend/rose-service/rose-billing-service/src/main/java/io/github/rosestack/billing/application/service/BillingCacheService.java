package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.TargetType;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.billing.domain.price.BillPrice;
import io.github.rosestack.billing.domain.price.BillPriceMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.billing.domain.usage.BillUsageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 计费缓存服务
 *
 * 为计费系统提供缓存支持，提升查询性能
 * 缓存套餐信息、定价信息、配额信息和使用量汇总等高频查询数据
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingCacheService {

    private final BillPlanMapper planMapper;
    private final BillPriceMapper priceMapper;
    private final BillSubscriptionMapper subscriptionMapper;
    private final BillUsageMapper usageMapper;

    // 缓存键名常量
    public static final String CACHE_PLAN = "billing:plan";
    public static final String CACHE_PRICE = "billing:price";
    public static final String CACHE_SUBSCRIPTION = "billing:subscription";
    public static final String CACHE_QUOTA = "billing:quota";
    public static final String CACHE_USAGE_SUMMARY = "billing:usage:summary";

    /**
     * 缓存套餐信息
     */
    @Cacheable(value = CACHE_PLAN, key = "#planId", unless = "#result == null")
    public BillPlan getPlanCache(Long planId) {
        log.debug("Cache miss for plan: {}", planId);
        return planMapper.selectById(planId);
    }

    /**
     * 缓存定价信息
     */
    @Cacheable(value = CACHE_PRICE, 
               key = "#tenantId + ':' + #targetType + ':' + #targetId + ':' + #cycle", 
               unless = "#result == null")
    public BillPrice getPricingCache(String tenantId, TargetType targetType, 
                                   Long targetId, BillingCycle cycle) {
        log.debug("Cache miss for pricing: tenant={}, target={}:{}, cycle={}", 
                 tenantId, targetType, targetId, cycle);
        return priceMapper.findBestPrice(tenantId, targetType, targetId, cycle);
    }

    /**
     * 缓存订阅信息
     */
    @Cacheable(value = CACHE_SUBSCRIPTION, key = "#subscriptionId", unless = "#result == null")
    public BillSubscription getSubscriptionCache(Long subscriptionId) {
        log.debug("Cache miss for subscription: {}", subscriptionId);
        return subscriptionMapper.selectById(subscriptionId);
    }

    /**
     * 缓存配额信息
     */
    @Cacheable(value = CACHE_QUOTA, 
               key = "#subscriptionId + ':' + #featureId", 
               unless = "#result == null")
    public QuotaInfo getQuotaCache(Long subscriptionId, Long featureId) {
        log.debug("Cache miss for quota: subscription={}, feature={}", subscriptionId, featureId);
        
        // 获取订阅信息
        BillSubscription subscription = getSubscriptionCache(subscriptionId);
        if (subscription == null) {
            return null;
        }

        // 获取功能配置（从套餐功能配置中获取配额信息）
        // 这里简化实现，实际应该从 BillPlanFeature 中获取
        QuotaInfo quotaInfo = new QuotaInfo();
        quotaInfo.setSubscriptionId(subscriptionId);
        quotaInfo.setFeatureId(featureId);
        quotaInfo.setTotalQuota(BigDecimal.valueOf(1000)); // 示例配额
        quotaInfo.setUsedQuota(getCurrentUsage(subscriptionId, featureId));
        quotaInfo.setAvailableQuota(quotaInfo.getTotalQuota().subtract(quotaInfo.getUsedQuota()));
        
        return quotaInfo;
    }

    /**
     * 缓存使用量汇总信息
     */
    @Cacheable(value = CACHE_USAGE_SUMMARY, 
               key = "#subscriptionId + ':' + #period", 
               unless = "#result == null")
    public UsageSummary getUsageSummaryCache(Long subscriptionId, LocalDate period) {
        log.debug("Cache miss for usage summary: subscription={}, period={}", subscriptionId, period);
        
        UsageSummary summary = new UsageSummary();
        summary.setSubscriptionId(subscriptionId);
        summary.setPeriod(period);
        
        // 计算总使用量（这里需要根据实际的使用量统计逻辑实现）
        summary.setTotalUsage(BigDecimal.valueOf(500)); // 示例使用量
        summary.setTotalCost(BigDecimal.valueOf(50.00)); // 示例费用
        
        return summary;
    }

    /**
     * 获取当前使用量
     */
    private BigDecimal getCurrentUsage(Long subscriptionId, Long featureId) {
        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);
        BigDecimal usage = usageMapper.sumUsageBySubscriptionFeatureAndPeriod(
                subscriptionId, featureId, currentPeriod);
        return usage != null ? usage : BigDecimal.ZERO;
    }

    /**
     * 清除套餐缓存
     */
    @CacheEvict(value = CACHE_PLAN, key = "#planId")
    public void evictPlanCache(Long planId) {
        log.debug("Evicting plan cache: {}", planId);
    }

    /**
     * 清除定价缓存
     */
    @CacheEvict(value = CACHE_PRICE, 
                key = "#tenantId + ':' + #targetType + ':' + #targetId + ':' + #cycle")
    public void evictPricingCache(String tenantId, TargetType targetType, 
                                Long targetId, BillingCycle cycle) {
        log.debug("Evicting pricing cache: tenant={}, target={}:{}, cycle={}", 
                 tenantId, targetType, targetId, cycle);
    }

    /**
     * 清除订阅缓存
     */
    @CacheEvict(value = CACHE_SUBSCRIPTION, key = "#subscriptionId")
    public void evictSubscriptionCache(Long subscriptionId) {
        log.debug("Evicting subscription cache: {}", subscriptionId);
    }

    /**
     * 清除配额缓存
     */
    @CacheEvict(value = CACHE_QUOTA, key = "#subscriptionId + ':' + #featureId")
    public void evictQuotaCache(Long subscriptionId, Long featureId) {
        log.debug("Evicting quota cache: subscription={}, feature={}", subscriptionId, featureId);
    }

    /**
     * 清除使用量汇总缓存
     */
    @CacheEvict(value = CACHE_USAGE_SUMMARY, key = "#subscriptionId + ':' + #period")
    public void evictUsageSummaryCache(Long subscriptionId, LocalDate period) {
        log.debug("Evicting usage summary cache: subscription={}, period={}", subscriptionId, period);
    }

    /**
     * 清除订阅相关的所有缓存
     */
    @Caching(evict = {
        @CacheEvict(value = CACHE_SUBSCRIPTION, key = "#subscriptionId"),
        @CacheEvict(value = CACHE_QUOTA, allEntries = true),
        @CacheEvict(value = CACHE_USAGE_SUMMARY, allEntries = true)
    })
    public void evictSubscriptionRelatedCache(Long subscriptionId) {
        log.debug("Evicting all caches related to subscription: {}", subscriptionId);
    }

    /**
     * 清除所有缓存
     */
    @Caching(evict = {
        @CacheEvict(value = CACHE_PLAN, allEntries = true),
        @CacheEvict(value = CACHE_PRICE, allEntries = true),
        @CacheEvict(value = CACHE_SUBSCRIPTION, allEntries = true),
        @CacheEvict(value = CACHE_QUOTA, allEntries = true),
        @CacheEvict(value = CACHE_USAGE_SUMMARY, allEntries = true)
    })
    public void evictAllCache() {
        log.info("Evicting all billing caches");
    }

    /**
     * 配额信息类
     */
    public static class QuotaInfo {
        private Long subscriptionId;
        private Long featureId;
        private BigDecimal totalQuota;
        private BigDecimal usedQuota;
        private BigDecimal availableQuota;

        // Getters and Setters
        public Long getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
        public Long getFeatureId() { return featureId; }
        public void setFeatureId(Long featureId) { this.featureId = featureId; }
        public BigDecimal getTotalQuota() { return totalQuota; }
        public void setTotalQuota(BigDecimal totalQuota) { this.totalQuota = totalQuota; }
        public BigDecimal getUsedQuota() { return usedQuota; }
        public void setUsedQuota(BigDecimal usedQuota) { this.usedQuota = usedQuota; }
        public BigDecimal getAvailableQuota() { return availableQuota; }
        public void setAvailableQuota(BigDecimal availableQuota) { this.availableQuota = availableQuota; }
        
        public boolean hasAvailableQuota() {
            return availableQuota != null && availableQuota.compareTo(BigDecimal.ZERO) > 0;
        }
        
        public double getUsagePercentage() {
            if (totalQuota == null || totalQuota.compareTo(BigDecimal.ZERO) == 0) {
                return 0.0;
            }
            return usedQuota.divide(totalQuota, 4, java.math.RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                           .doubleValue();
        }
    }

    /**
     * 使用量汇总信息类
     */
    public static class UsageSummary {
        private Long subscriptionId;
        private LocalDate period;
        private BigDecimal totalUsage;
        private BigDecimal totalCost;

        // Getters and Setters
        public Long getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
        public LocalDate getPeriod() { return period; }
        public void setPeriod(LocalDate period) { this.period = period; }
        public BigDecimal getTotalUsage() { return totalUsage; }
        public void setTotalUsage(BigDecimal totalUsage) { this.totalUsage = totalUsage; }
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    }
}