package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.PricingConfigType;
import io.github.rosestack.billing.domain.price.BillPrice;
import io.github.rosestack.billing.domain.price.BillPriceMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 价格计算组件
 *
 * 提供灵活的价格计算功能，支持多种定价模式和计算策略
 * 包括固定定价、阶梯定价、包量定价等多种计算方式
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PricingCalculator {

    private final BillPriceMapper priceMapper;
    private final ObjectMapper objectMapper;

    /**
     * 计算价格
     */
    public PricingResult calculatePrice(PricingRequest request) {
        log.debug("计算价格: {}", request);

        try {
            // 获取有效定价
            BillPrice price = priceMapper.findEffectivePrice(
                    request.getTenantId(),
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getBillingCycle());

            if (price == null) {
                return PricingResult.failure("未找到有效定价");
            }

            // 根据计费周期计算价格
            return calculatePriceByType(price, request);

        } catch (Exception e) {
            log.error("价格计算异常: {}", request, e);
            return PricingResult.failure("价格计算失败: " + e.getMessage());
        }
    }

    /**
     * 根据定价类型计算价格
     */
    private PricingResult calculatePriceByType(BillPrice price, PricingRequest request) {
        switch (price.getBillingCycle()) {
            case MONTHLY:
            case YEARLY:
                return calculateFixedPrice(price, request);
            case USAGE:
                return calculateUsagePrice(price, request);
            default:
                return PricingResult.failure("不支持的计费周期: " + price.getBillingCycle());
        }
    }

    /**
     * 计算固定定价
     */
    private PricingResult calculateFixedPrice(BillPrice price, PricingRequest request) {
        BigDecimal unitPrice = price.getPrice();
        BigDecimal quantity = request.getQuantity() != null ? request.getQuantity() : BigDecimal.ONE;

        BigDecimal totalAmount = unitPrice.multiply(quantity);

        PricingResult result = new PricingResult();
        result.setSuccess(true);
        result.setUnitPrice(unitPrice);
        result.setQuantity(quantity);
        result.setTotalAmount(totalAmount);
        result.setCurrency(price.getCurrency());
        result.setBillingCycle(price.getBillingCycle());
        result.setPriceType("FIXED");
        result.setCalculationDetails("固定定价: " + unitPrice + " x " + quantity + " = " + totalAmount);

        return result;
    }

    /**
     * 计算使用量定价
     */
    private PricingResult calculateUsagePrice(BillPrice price, PricingRequest request) {
        if (request.getUsageAmount() == null || request.getUsageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return PricingResult.failure("使用量不能为空或小于等于0");
        }

        try {
            PricingConfig config = parsePricingConfig(price.getPricingConfig());
            return calculateByPricingType(config, request.getUsageAmount(), price);
        } catch (Exception e) {
            log.error("解析定价配置失败: {}", price.getPricingConfig(), e);
            return PricingResult.failure("定价配置错误");
        }
    }

    /**
     * 根据定价类型计算
     */
    private PricingResult calculateByPricingType(PricingConfig config, BigDecimal usageAmount, BillPrice price) {
        switch (config.getType()) {
            case QUOTA:
                return calculateQuotaPrice(config, usageAmount, price);
            case TIERED:
                return calculateTieredPrice(config, usageAmount, price);
            case USAGE:
                return calculateSimpleUsagePrice(config, usageAmount, price);
            case PACKAGE:
                return calculatePackagePrice(config, usageAmount, price);
            case TIERED_FIXED:
                return calculateTieredFixedPrice(config, usageAmount, price);
            default:
                return PricingResult.failure("不支持的定价类型: " + config.getType());
        }
    }

    /**
     * 配额定价计算
     */
    private PricingResult calculateQuotaPrice(PricingConfig config, BigDecimal usageAmount, BillPrice price) {
        List<PricingTier> values = config.getValues();
        if (values.isEmpty()) {
            return PricingResult.failure("配额定价配置为空");
        }

        PricingTier tier = values.get(0);
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 超出配额的部分需要额外付费
        if (usageAmount.compareTo(tier.getMax()) > 0) {
            BigDecimal overageAmount = usageAmount.subtract(tier.getMax());
            totalAmount = overageAmount.multiply(tier.getPrice());
        }

        PricingResult result = new PricingResult();
        result.setSuccess(true);
        result.setUnitPrice(tier.getPrice());
        result.setQuantity(usageAmount);
        result.setTotalAmount(totalAmount);
        result.setCurrency(price.getCurrency());
        result.setBillingCycle(price.getBillingCycle());
        result.setPriceType("QUOTA");
        result.setCalculationDetails(String.format("配额定价: 配额=%s, 使用量=%s, 超量=%s, 费用=%s",
                tier.getMax(), usageAmount,
                usageAmount.subtract(tier.getMax()).max(BigDecimal.ZERO),
                totalAmount));

        return result;
    }

    /**
     * 阶梯定价计算
     */
    private PricingResult calculateTieredPrice(PricingConfig config, BigDecimal usageAmount, BillPrice price) {
        List<PricingTier> tiers = config.getValues();
        if (tiers.isEmpty()) {
            return PricingResult.failure("阶梯定价配置为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal remainingUsage = usageAmount;
        List<String> calculations = new ArrayList<>();

        for (PricingTier tier : tiers) {
            if (remainingUsage.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tierStart = tier.getMin();
            BigDecimal tierEnd = tier.getMax();

            // 计算在当前阶梯的使用量
            BigDecimal tierUsage = BigDecimal.ZERO;

            if (usageAmount.compareTo(tierStart) > 0) {
                if (tierEnd != null && usageAmount.compareTo(tierEnd) <= 0) {
                    // 使用量在当前阶梯内
                    tierUsage = usageAmount.subtract(tierStart);
                } else if (tierEnd != null) {
                    // 使用量超过当前阶梯
                    tierUsage = tierEnd.subtract(tierStart);
                } else {
                    // 最后一个阶梯，无上限
                    tierUsage = remainingUsage;
                }

                BigDecimal tierAmount = tierUsage.multiply(tier.getPrice());
                totalAmount = totalAmount.add(tierAmount);
                remainingUsage = remainingUsage.subtract(tierUsage);

                calculations.add(String.format("阶梯[%s-%s]: %s x %s = %s",
                        tierStart, tierEnd != null ? tierEnd : "∞",
                        tierUsage, tier.getPrice(), tierAmount));
            }
        }

        PricingResult result = new PricingResult();
        result.setSuccess(true);
        result.setQuantity(usageAmount);
        result.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        result.setCurrency(price.getCurrency());
        result.setBillingCycle(price.getBillingCycle());
        result.setPriceType("TIERED");
        result.setCalculationDetails("阶梯定价: " + String.join("; ", calculations));

        return result;
    }

    /**
     * 简单使用量定价计算
     */
    private PricingResult calculateSimpleUsagePrice(PricingConfig config, BigDecimal usageAmount, BillPrice price) {
        List<PricingTier> values = config.getValues();
        if (values.isEmpty()) {
            return PricingResult.failure("使用量定价配置为空");
        }

        PricingTier tier = values.get(0);
        BigDecimal unitPrice = tier.getPrice();
        BigDecimal totalAmount = usageAmount.multiply(unitPrice);

        PricingResult result = new PricingResult();
        result.setSuccess(true);
        result.setUnitPrice(unitPrice);
        result.setQuantity(usageAmount);
        result.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        result.setCurrency(price.getCurrency());
        result.setBillingCycle(price.getBillingCycle());
        result.setPriceType("USAGE");
        result.setCalculationDetails(String.format("使用量定价: %s x %s = %s",
                usageAmount, unitPrice, totalAmount));

        return result;
    }

    /**
     * 包量定价计算
     */
    private PricingResult calculatePackagePrice(PricingConfig config, BigDecimal usageAmount, BillPrice price) {
        List<PricingTier> packages = config.getValues();
        if (packages.isEmpty()) {
            return PricingResult.failure("包量定价配置为空");
        }

        // 找到合适的包量
        PricingTier selectedPackage = null;
        for (PricingTier pkg : packages) {
            if (usageAmount.compareTo(pkg.getQuantity()) <= 0) {
                selectedPackage = pkg;
                break;
            }
        }

        if (selectedPackage == null) {
            // 如果没有合适的包量，使用最大的包量
            selectedPackage = packages.get(packages.size() - 1);
        }

        BigDecimal totalAmount = selectedPackage.getPrice();

        PricingResult result = new PricingResult();
        result.setSuccess(true);
        result.setUnitPrice(selectedPackage.getPrice());
        result.setQuantity(selectedPackage.getQuantity());
        result.setTotalAmount(totalAmount);
        result.setCurrency(price.getCurrency());
        result.setBillingCycle(price.getBillingCycle());
        result.setPriceType("PACKAGE");
        result.setCalculationDetails(String.format("包量定价: 包量=%s, 价格=%s",
                selectedPackage.getQuantity(), selectedPackage.getPrice()));

        return result;
    }

    /**
     * 固定阶梯定价计算
     */
    private PricingResult calculateTieredFixedPrice(PricingConfig config, BigDecimal usageAmount, BillPrice price) {
        List<PricingTier> tiers = config.getValues();
        if (tiers.isEmpty()) {
            return PricingResult.failure("固定阶梯定价配置为空");
        }

        // 找到对应的阶梯
        PricingTier selectedTier = null;
        for (PricingTier tier : tiers) {
            if (usageAmount.compareTo(tier.getMin()) >= 0 &&
                (tier.getMax() == null || usageAmount.compareTo(tier.getMax()) <= 0)) {
                selectedTier = tier;
                break;
            }
        }

        if (selectedTier == null) {
            return PricingResult.failure("未找到匹配的定价阶梯");
        }

        BigDecimal totalAmount = selectedTier.getPrice();

        PricingResult result = new PricingResult();
        result.setSuccess(true);
        result.setUnitPrice(selectedTier.getPrice());
        result.setQuantity(usageAmount);
        result.setTotalAmount(totalAmount);
        result.setCurrency(price.getCurrency());
        result.setBillingCycle(price.getBillingCycle());
        result.setPriceType("TIERED_FIXED");
        result.setCalculationDetails(String.format("固定阶梯定价: 阶梯[%s-%s], 固定价格=%s",
                selectedTier.getMin(),
                selectedTier.getMax() != null ? selectedTier.getMax() : "∞",
                selectedTier.getPrice()));

        return result;
    }

    /**
     * 解析定价配置
     */
    private PricingConfig parsePricingConfig(String configJson) throws Exception {
        if (configJson == null || configJson.trim().isEmpty()) {
            throw new IllegalArgumentException("定价配置不能为空");
        }

        JsonNode configNode = objectMapper.readTree(configJson);
        PricingConfig config = new PricingConfig();

        String typeStr = configNode.path("type").asText("usage");
        config.setType(PricingConfigType.valueOf(typeStr.toUpperCase()));

        List<PricingTier> values = new ArrayList<>();
        JsonNode valuesNode = configNode.path("values");

        if (valuesNode.isArray()) {
            for (JsonNode valueNode : valuesNode) {
                PricingTier tier = new PricingTier();
                tier.setMin(getJsonDecimal(valueNode, "min", BigDecimal.ZERO));
                tier.setMax(getJsonDecimal(valueNode, "max", null));
                tier.setQuantity(getJsonDecimal(valueNode, "quantity", null));
                tier.setPrice(getJsonDecimal(valueNode, "price", BigDecimal.ZERO));
                values.add(tier);
            }
        }

        config.setValues(values);
        return config;
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
     * 预估价格
     */
    public PricingResult estimatePrice(String targetType, Long targetId, BillingCycle billingCycle,
                                     BigDecimal quantity, BigDecimal usageAmount) {
        PricingRequest request = new PricingRequest();
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        request.setBillingCycle(billingCycle);
        request.setQuantity(quantity);
        request.setUsageAmount(usageAmount);

        return calculatePrice(request);
    }

    /**
     * 定价请求
     */
    @Data
    public static class PricingRequest {
        private String tenantId;
        private String targetType;
        private Long targetId;
        private BillingCycle billingCycle;
        private BigDecimal quantity;
        private BigDecimal usageAmount;
    }

    /**
     * 定价结果
     */
    @Data
    public static class PricingResult {
        private boolean success;
        private String message;
        private BigDecimal unitPrice;
        private BigDecimal quantity;
        private BigDecimal totalAmount;
        private String currency;
        private BillingCycle billingCycle;
        private String priceType;
        private String calculationDetails;

        public static PricingResult failure(String message) {
            PricingResult result = new PricingResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }

    /**
     * 定价配置
     */
    @Data
    public static class PricingConfig {
        private PricingConfigType type;
        private List<PricingTier> values;
    }

    /**
     * 定价阶梯
     */
    @Data
    public static class PricingTier {
        private BigDecimal min;
        private BigDecimal max;
        private BigDecimal quantity;
        private BigDecimal price;
    }
}
