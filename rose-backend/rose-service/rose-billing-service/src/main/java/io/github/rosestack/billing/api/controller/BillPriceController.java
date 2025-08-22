package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillPriceService;
import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.TargetType;
import io.github.rosestack.billing.domain.price.BillPrice;
import io.github.rosestack.core.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 定价管理控制器
 *
 * 提供定价管理的RESTful API接口
 * 支持标准定价和租户专属定价的管理
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/prices")
@RequiredArgsConstructor
@Slf4j
public class BillPriceController {

    private final BillPriceService priceService;

    /**
     * 创建定价
     */
    @PostMapping
    public ApiResponse<BillPrice> createPrice(@Valid @RequestBody BillPrice price) {
        log.info("Creating new price for target: {} {}", price.getTargetType(), price.getTargetId());

        BillPrice createdPrice = priceService.createPrice(price);
        return ApiResponse.ok(createdPrice);
    }

    /**
     * 更新定价
     */
    @PutMapping("/{id}")
    public ApiResponse<BillPrice> updatePrice(@PathVariable Long id,
                                             @Valid @RequestBody BillPrice price) {
        log.info("Updating price: {}", id);

        price.setId(id);
        BillPrice updatedPrice = priceService.updatePrice(price);
        return ApiResponse.ok(updatedPrice);
    }

    /**
     * 获取最佳定价
     */
    @GetMapping("/best")
    public ApiResponse<BillPrice> getBestPrice(
            @RequestParam String tenantId,
            @RequestParam TargetType targetType,
            @RequestParam Long targetId,
            @RequestParam BillingCycle cycle) {

        log.debug("Getting best price for tenant: {}, target: {} {}, cycle: {}",
                 tenantId, targetType, targetId, cycle);

        BillPrice price = priceService.getBestPrice(tenantId, targetType, targetId, cycle);
        return ApiResponse.ok(price);
    }

    /**
     * 获取标准定价
     */
    @GetMapping("/standard")
    public ApiResponse<BillPrice> getStandardPrice(
            @RequestParam TargetType targetType,
            @RequestParam Long targetId,
            @RequestParam BillingCycle cycle) {

        log.debug("Getting standard price for target: {} {}, cycle: {}", targetType, targetId, cycle);

        BillPrice price = priceService.getStandardPrice(targetType, targetId, cycle);
        return ApiResponse.ok(price);
    }

    /**
     * 创建租户专属定价
     */
    @PostMapping("/tenant-specific")
    public ApiResponse<BillPrice> createTenantPrice(
            @RequestParam String tenantId,
            @RequestParam TargetType targetType,
            @RequestParam Long targetId,
            @RequestParam BigDecimal price,
            @RequestParam(required = false, defaultValue = "CNY") String currency,
            @RequestParam BillingCycle cycle,
            @RequestParam(required = false) String pricingConfig) {

        log.info("Creating tenant specific price for tenant: {}, target: {} {}",
                tenantId, targetType, targetId);

        BillPrice tenantPrice = priceService.createTenantPrice(
                tenantId, targetType, targetId, price, currency, cycle, pricingConfig);
        return ApiResponse.ok(tenantPrice);
    }

    /**
     * 获取租户的所有定价
     */
    @GetMapping("/tenant/{tenantId}")
    public ApiResponse<List<BillPrice>> getTenantPrices(@PathVariable String tenantId) {
        log.debug("Getting all prices for tenant: {}", tenantId);

        List<BillPrice> prices = priceService.getTenantPrices(tenantId);
        return ApiResponse.ok(prices);
    }

    /**
     * 获取所有有效的套餐定价
     */
    @GetMapping("/plans")
    public ApiResponse<List<BillPrice>> getEffectivePlanPrices() {
        log.debug("Getting all effective plan prices");

        List<BillPrice> prices = priceService.getEffectivePlanPrices();
        return ApiResponse.ok(prices);
    }

    /**
     * 获取所有有效的功能定价
     */
    @GetMapping("/features")
    public ApiResponse<List<BillPrice>> getEffectiveFeaturePrices() {
        log.debug("Getting all effective feature prices");

        List<BillPrice> prices = priceService.getEffectiveFeaturePrices();
        return ApiResponse.ok(prices);
    }

    /**
     * 使定价过期
     */
    @PutMapping("/{id}/expire")
    public ApiResponse<Void> expirePrice(@PathVariable Long id) {
        log.info("Expiring price: {}", id);

        priceService.expirePrice(id);
        return ApiResponse.ok();
    }

    /**
     * 批量使目标的所有定价过期
     */
    @PutMapping("/expire-target")
    public ApiResponse<Void> expireTargetPrices(
            @RequestParam TargetType targetType,
            @RequestParam Long targetId) {

        log.info("Expiring all prices for target: {} {}", targetType, targetId);

        priceService.expireTargetPrices(targetType, targetId);
        return ApiResponse.ok();
    }
}