package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.billing.domain.feature.BillFeatureMapper;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanFeature;
import io.github.rosestack.billing.domain.plan.BillPlanFeatureMapper;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.billing.domain.usage.BillUsage;
import io.github.rosestack.billing.domain.usage.BillUsageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计费引擎服务
 *
 * 实现基于用量的计费逻辑，支持多种计费模式和定价策略
 * 核心功能包括用量计费、配额检查、费用计算等
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingEngineService {

    private final BillSubscriptionMapper subscriptionMapper;
    private final BillPlanMapper planMapper;
    private final BillFeatureMapper featureMapper;
    private final BillPlanFeatureMapper planFeatureMapper;
    private final BillUsageMapper usageMapper;
    private final ObjectMapper objectMapper;

    /**
     * 预估计费
     * 与 calculateBilling 类似，但不产生实际的计费记录
     */
    public BillingResult estimateBilling(Long subscriptionId, LocalDate periodStart, LocalDate periodEnd) {
        log.info("Estimating billing for subscription: {}, period: {} to {}", 
                subscriptionId, periodStart, periodEnd);
        
        // 使用相同的计算逻辑，但不保存结果
        return calculateBilling(subscriptionId, periodStart, periodEnd);
    }

    /**
     * 计算订阅在指定周期的总费用
     */
    public BillingResult calculateBilling(Long subscriptionId, LocalDate periodStart, LocalDate periodEnd) {
        log.info("Calculating billing for subscription: {}, period: {} to {}", 
                subscriptionId, periodStart, periodEnd);

        // 获取订阅信息
        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new IllegalArgumentException("订阅不存在: " + subscriptionId);
        }

        // 获取套餐信息
        BillPlan plan = planMapper.selectById(subscription.getPlanId());
        if (plan == null) {
            throw new IllegalArgumentException("套餐不存在: " + subscription.getPlanId());
        }

        // 获取套餐的所有功能配置
        List<BillPlanFeature> planFeatures = planFeatureMapper.findByPlanId(subscription.getPlanId());

        BillingResult result = new BillingResult();
        result.setSubscriptionId(subscriptionId);
        result.setPlanId(subscription.getPlanId());
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);
        result.setQuantity(subscription.getQuantity());

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, FeatureBilling> featureBillings = new HashMap<>();

        // 计算每个功能的费用
        for (BillPlanFeature planFeature : planFeatures) {
            FeatureBilling featureBilling = calculateFeatureBilling(
                    subscriptionId, planFeature, periodStart, periodEnd, subscription.getQuantity());
            
            featureBillings.put(planFeature.getFeatureId(), featureBilling);
            totalAmount = totalAmount.add(featureBilling.getAmount());
        }

        result.setFeatureBillings(featureBillings);
        result.setSubtotal(totalAmount);

        // 应用折扣
        BigDecimal discount = calculateDiscount(subscription, plan, totalAmount);
        result.setDiscount(discount);

        // 计算税费
        BigDecimal tax = calculateTax(totalAmount.subtract(discount));
        result.setTax(tax);

        // 计算最终金额
        BigDecimal finalAmount = totalAmount.subtract(discount).add(tax);
        result.setTotalAmount(finalAmount);

        log.info("Billing calculated: subscription={}, amount={}", subscriptionId, finalAmount);
        return result;
    }

    /**
     * 计算功能费用
     */
    private FeatureBilling calculateFeatureBilling(Long subscriptionId, BillPlanFeature planFeature, 
                                                  LocalDate periodStart, LocalDate periodEnd, int quantity) {
        
        // 获取功能信息
        BillFeature feature = featureMapper.selectById(planFeature.getFeatureId());
        if (feature == null) {
            log.warn("Feature not found: {}", planFeature.getFeatureId());
            return new FeatureBilling(planFeature.getFeatureId(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // 获取用量数据
        BigDecimal usage = usageMapper.sumUsageBySubscriptionFeatureAndPeriod(
                subscriptionId, planFeature.getFeatureId(), periodStart);

        // 解析功能配置
        FeatureConfig config = parseFeatureConfig(planFeature.getFeatureValue());
        
        // 根据功能类型计算费用
        BigDecimal amount = BigDecimal.ZERO;
        
        switch (feature.getType()) {
            case QUOTA:
                // 配额型功能：通常是包月费用
                amount = config.getBasePrice().multiply(BigDecimal.valueOf(quantity));
                break;
                
            case USAGE:
                // 用量型功能：按实际用量计费
                amount = calculateUsageBilling(usage, config);
                break;
                
            case SWITCH:
                // 开关型功能：固定费用
                if (config.isEnabled()) {
                    amount = config.getBasePrice().multiply(BigDecimal.valueOf(quantity));
                }
                break;
                
            default:
                log.warn("Unknown feature type: {}", feature.getType());
        }

        return new FeatureBilling(planFeature.getFeatureId(), usage, amount, config.getQuota());
    }

    /**
     * 计算用量计费
     */
    private BigDecimal calculateUsageBilling(BigDecimal usage, FeatureConfig config) {
        if (usage == null || usage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal remainingUsage = usage;

        // 免费额度
        if (config.getFreeQuota() != null && config.getFreeQuota().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal freeUsage = remainingUsage.min(config.getFreeQuota());
            remainingUsage = remainingUsage.subtract(freeUsage);
        }

        // 阶梯计费
        if (config.getTierPrices() != null && !config.getTierPrices().isEmpty()) {
            amount = calculateTieredPricing(remainingUsage, config.getTierPrices());
        } else {
            // 单一价格
            amount = remainingUsage.multiply(config.getUnitPrice());
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 阶梯计费计算
     */
    private BigDecimal calculateTieredPricing(BigDecimal usage, List<TierPrice> tierPrices) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal processedUsage = BigDecimal.ZERO;

        for (TierPrice tier : tierPrices) {
            if (processedUsage.compareTo(usage) >= 0) {
                break;
            }

            BigDecimal tierStart = tier.getMinUsage();
            BigDecimal tierEnd = tier.getMaxUsage();
            
            // 跳过还没到的阶梯
            if (usage.compareTo(tierStart) <= 0) {
                continue;
            }
            
            // 计算在当前阶梯内的使用量
            BigDecimal tierUsageStart = processedUsage.max(tierStart);
            BigDecimal tierUsageEnd;
            
            if (tierEnd == null) {
                // 最后一层，无上限
                tierUsageEnd = usage;
            } else {
                tierUsageEnd = usage.min(tierEnd);
            }
            
            BigDecimal tierUsage = tierUsageEnd.subtract(tierUsageStart);
            if (tierUsage.compareTo(BigDecimal.ZERO) > 0) {
                amount = amount.add(tierUsage.multiply(tier.getUnitPrice()));
                processedUsage = tierUsageEnd;
            }
        }

        return amount;
    }

    /**
     * 计算折扣
     */
    private BigDecimal calculateDiscount(BillSubscription subscription, BillPlan plan, BigDecimal amount) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        // 1. 年付折扣
        BigDecimal annualDiscount = calculateAnnualDiscount(subscription, amount);
        totalDiscount = totalDiscount.add(annualDiscount);
        
        // 2. 批量折扣
        BigDecimal quantityDiscount = calculateQuantityDiscount(subscription, amount);
        totalDiscount = totalDiscount.add(quantityDiscount);
        
        // 3. 促销活动折扣
        BigDecimal promotionDiscount = calculatePromotionDiscount(subscription, plan, amount);
        totalDiscount = totalDiscount.add(promotionDiscount);
        
        // 4. 用户等级折扣
        BigDecimal vipDiscount = calculateVipDiscount(subscription, amount);
        totalDiscount = totalDiscount.add(vipDiscount);
        
        // 折扣上限不能超过原价格
        return totalDiscount.min(amount);
    }
    
    /**
     * 计算年付折扣
     */
    private BigDecimal calculateAnnualDiscount(BillSubscription subscription, BigDecimal amount) {
        try {
            JsonNode pricingSnapshot = objectMapper.readTree(subscription.getPricingSnapshot());
            String billingCycle = pricingSnapshot.path("plan_pricing").path("billing_cycle").asText();
            
            if ("YEARLY".equals(billingCycle)) {
                // 年付享受 10% 折扣
                return amount.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            log.warn("Failed to parse billing cycle for annual discount", e);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算批量折扣
     */
    private BigDecimal calculateQuantityDiscount(BillSubscription subscription, BigDecimal amount) {
        int quantity = subscription.getQuantity();
        BigDecimal discountRate = BigDecimal.ZERO;
        
        if (quantity >= 100) {
            // 100+ 席位，15% 折扣
            discountRate = BigDecimal.valueOf(0.15);
        } else if (quantity >= 50) {
            // 50+ 席位，10% 折扣
            discountRate = BigDecimal.valueOf(0.10);
        } else if (quantity >= 20) {
            // 20+ 席位，5% 折扣
            discountRate = BigDecimal.valueOf(0.05);
        }
        
        return amount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算促销活动折扣
     */
    private BigDecimal calculatePromotionDiscount(BillSubscription subscription, BillPlan plan, BigDecimal amount) {
        // TODO: 这里可以集成促销系统，查询当前有效的促销活动
        // 简化实现：根据套餐类型给予折扣
        
        if (plan.getPlanType().name().equals("ENTERPRISE")) {
            // 企业版新用户特惠 5%
            return amount.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算用户等级折扣
     */
    private BigDecimal calculateVipDiscount(BillSubscription subscription, BigDecimal amount) {
        // TODO: 这里可以集成用户等级系统，根据用户VIP等级给予折扣
        // 简化实现：暂不实现
        
        return BigDecimal.ZERO;
    }

    /**
     * 计算税费
     */
    private BigDecimal calculateTax(BigDecimal amount) {
        // TODO: 根据地区和税率计算税费
        // 简化实现：固定税率6%
        return amount.multiply(BigDecimal.valueOf(0.06)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 解析功能配置
     */
    private FeatureConfig parseFeatureConfig(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            return createDefaultConfig();
        }

        try {
            JsonNode configNode = objectMapper.readTree(configJson);
            FeatureConfig config = new FeatureConfig();

            // 基础配置
            config.setEnabled(getJsonBoolean(configNode, "enabled", true));
            config.setBasePrice(getJsonDecimal(configNode, "basePrice", BigDecimal.valueOf(10.00)));
            config.setUnitPrice(getJsonDecimal(configNode, "unitPrice", BigDecimal.valueOf(0.01)));
            config.setQuota(getJsonDecimal(configNode, "quota", BigDecimal.valueOf(1000)));
            config.setFreeQuota(getJsonDecimal(configNode, "freeQuota", BigDecimal.valueOf(100)));

            // 阶梯定价配置
            if (configNode.has("tierPrices") && configNode.get("tierPrices").isArray()) {
                List<TierPrice> tierPrices = new ArrayList<>();
                JsonNode tierPricesNode = configNode.get("tierPrices");
                for (JsonNode tierNode : tierPricesNode) {
                    if (tierNode != null && !tierNode.isNull()) {
                        BigDecimal minUsage = getJsonDecimal(tierNode, "minUsage", BigDecimal.ZERO);
                        BigDecimal maxUsage = tierNode.has("maxUsage") && !tierNode.get("maxUsage").isNull() 
                                ? getJsonDecimal(tierNode, "maxUsage", null) : null;
                        BigDecimal unitPrice = getJsonDecimal(tierNode, "unitPrice", BigDecimal.ZERO);
                        
                        // 只有当单价大于0时才添加阶梯
                        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0) {
                            tierPrices.add(new TierPrice(minUsage, maxUsage, unitPrice));
                        }
                    }
                }
                if (!tierPrices.isEmpty()) {
                    config.setTierPrices(tierPrices);
                }
            }

            return config;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse feature config JSON: {}, using default config", configJson, e);
            return createDefaultConfig();
        }
    }

    /**
     * 创建默认配置
     */
    private FeatureConfig createDefaultConfig() {
        FeatureConfig config = new FeatureConfig();
        config.setEnabled(true);
        config.setBasePrice(BigDecimal.valueOf(10.00));
        config.setUnitPrice(BigDecimal.valueOf(0.01));
        config.setQuota(BigDecimal.valueOf(1000));
        config.setFreeQuota(BigDecimal.valueOf(100));
        return config;
    }

    /**
     * 从JSON节点获取布尔值
     */
    private boolean getJsonBoolean(JsonNode node, String fieldName, boolean defaultValue) {
        if (node != null && node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull() && fieldNode.isBoolean()) {
                return fieldNode.asBoolean();
            }
        }
        return defaultValue;
    }

    /**
     * 从JSON节点获取BigDecimal值
     */
    private BigDecimal getJsonDecimal(JsonNode node, String fieldName, BigDecimal defaultValue) {
        if (node != null && node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull() && fieldNode.isNumber()) {
                return BigDecimal.valueOf(fieldNode.asDouble());
            }
        }
        return defaultValue;
    }

    /**
     * 检查配额限制
     */
    public QuotaCheckResult checkQuota(Long subscriptionId, Long featureId, BigDecimal requestedAmount) {
        log.debug("Checking quota for subscription: {}, feature: {}, requested: {}", 
                subscriptionId, featureId, requestedAmount);
        
        // 获取功能配置
        List<BillPlanFeature> planFeatures = planFeatureMapper.findBySubscriptionAndFeature(
                subscriptionId, featureId);
        
        if (planFeatures.isEmpty()) {
            log.warn("Feature not configured for subscription: {}, feature: {}", subscriptionId, featureId);
            return new QuotaCheckResult(false, "功能未配置", BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BillPlanFeature planFeature = planFeatures.get(0);
        FeatureConfig config = parseFeatureConfig(planFeature.getFeatureValue());
        
        // 检查功能是否启用
        if (!config.isEnabled()) {
            log.warn("Feature is disabled for subscription: {}, feature: {}", subscriptionId, featureId);
            return new QuotaCheckResult(false, "功能已禁用", BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // 获取当前用量
        LocalDate currentPeriod = LocalDate.now().withDayOfMonth(1);
        BigDecimal currentUsage = usageMapper.sumUsageBySubscriptionFeatureAndPeriod(
                subscriptionId, featureId, currentPeriod);

        BigDecimal totalQuota = config.getQuota() != null ? config.getQuota() : BigDecimal.ZERO;
        BigDecimal usedQuota = currentUsage != null ? currentUsage : BigDecimal.ZERO;
        BigDecimal availableQuota = totalQuota.subtract(usedQuota);

        boolean allowed = availableQuota.compareTo(requestedAmount) >= 0;
        String message = allowed ? "配额充足" : "配额不足";
        
        log.debug("Quota check result - subscription: {}, feature: {}, allowed: {}, available: {}, total: {}", 
                subscriptionId, featureId, allowed, availableQuota, totalQuota);

        return new QuotaCheckResult(allowed, message, availableQuota, totalQuota);
    }

    /**
     * 计费结果
     */
    public static class BillingResult {
        private Long subscriptionId;
        private Long planId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Integer quantity;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal tax;
        private BigDecimal totalAmount;
        private Map<Long, FeatureBilling> featureBillings;

        // Getters and Setters
        public Long getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        public LocalDate getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
        public LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public Map<Long, FeatureBilling> getFeatureBillings() { return featureBillings; }
        public void setFeatureBillings(Map<Long, FeatureBilling> featureBillings) { this.featureBillings = featureBillings; }
    }

    /**
     * 功能计费明细
     */
    public static class FeatureBilling {
        private final Long featureId;
        private final BigDecimal usage;
        private final BigDecimal amount;
        private final BigDecimal quota;

        public FeatureBilling(Long featureId, BigDecimal usage, BigDecimal amount, BigDecimal quota) {
            this.featureId = featureId;
            this.usage = usage;
            this.amount = amount;
            this.quota = quota;
        }

        public Long getFeatureId() { return featureId; }
        public BigDecimal getUsage() { return usage; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getQuota() { return quota; }
    }

    /**
     * 功能配置
     */
    public static class FeatureConfig {
        private boolean enabled;
        private BigDecimal basePrice;
        private BigDecimal unitPrice;
        private BigDecimal quota;
        private BigDecimal freeQuota;
        private List<TierPrice> tierPrices;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getQuota() { return quota; }
        public void setQuota(BigDecimal quota) { this.quota = quota; }
        public BigDecimal getFreeQuota() { return freeQuota; }
        public void setFreeQuota(BigDecimal freeQuota) { this.freeQuota = freeQuota; }
        public List<TierPrice> getTierPrices() { return tierPrices; }
        public void setTierPrices(List<TierPrice> tierPrices) { this.tierPrices = tierPrices; }
    }

    /**
     * 阶梯价格
     */
    public static class TierPrice {
        private BigDecimal minUsage;
        private BigDecimal maxUsage;
        private BigDecimal unitPrice;

        public TierPrice(BigDecimal minUsage, BigDecimal maxUsage, BigDecimal unitPrice) {
            this.minUsage = minUsage;
            this.maxUsage = maxUsage;
            this.unitPrice = unitPrice;
        }

        public BigDecimal getMinUsage() { return minUsage; }
        public BigDecimal getMaxUsage() { return maxUsage; }
        public BigDecimal getUnitPrice() { return unitPrice; }
    }

    /**
     * 配额检查结果
     */
    public static class QuotaCheckResult {
        private final boolean allowed;
        private final String message;
        private final BigDecimal availableQuota;
        private final BigDecimal totalQuota;

        public QuotaCheckResult(boolean allowed, String message, BigDecimal availableQuota, BigDecimal totalQuota) {
            this.allowed = allowed;
            this.message = message;
            this.availableQuota = availableQuota;
            this.totalQuota = totalQuota;
        }

        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
        public BigDecimal getAvailableQuota() { return availableQuota; }
        public BigDecimal getTotalQuota() { return totalQuota; }
    }
}