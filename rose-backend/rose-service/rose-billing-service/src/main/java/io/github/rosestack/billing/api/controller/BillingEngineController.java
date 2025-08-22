package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillingEngineService;
import io.github.rosestack.core.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 计费引擎API控制器
 * <p>
 * 提供计费计算、配额检查等核心计费功能的API接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/engine")
@RequiredArgsConstructor
@Slf4j
public class BillingEngineController {

	private final BillingEngineService billingEngineService;

	/**
	 * 计算订阅费用
	 */
	@PostMapping("/calculate/{subscriptionId}")
	public ApiResponse<BillingEngineService.BillingResult> calculateBilling(
		@PathVariable Long subscriptionId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

		log.info("Calculating billing for subscription: {}, period: {} to {}",
			subscriptionId, periodStart, periodEnd);

		BillingEngineService.BillingResult result = billingEngineService.calculateBilling(
			subscriptionId, periodStart, periodEnd);

		return ApiResponse.ok(result);
	}

	/**
	 * 检查配额
	 */
	@PostMapping("/quota-check")
	public ApiResponse<String> checkQuota(
		@RequestParam Long subscriptionId,
		@RequestParam Long featureId,
		@RequestParam BigDecimal requestedAmount) {

		log.debug("Checking quota for subscription: {}, feature: {}, amount: {}",
			subscriptionId, featureId, requestedAmount);

		billingEngineService.checkQuota(subscriptionId, featureId, requestedAmount);
		return ApiResponse.ok("配额检查通过");
	}

	/**
	 * 预估费用（用于用户查看预计费用）
	 */
	@PostMapping("/estimate/{subscriptionId}")
	public ApiResponse<BillingEngineService.BillingResult> estimateBilling(
		@PathVariable Long subscriptionId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

		// 如果未指定时间，默认为当前月
		if (periodStart == null) {
			periodStart = LocalDate.now().withDayOfMonth(1);
		}
		if (periodEnd == null) {
			periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
		}

		log.info("Estimating billing for subscription: {}, period: {} to {}",
			subscriptionId, periodStart, periodEnd);

		BillingEngineService.BillingResult result = billingEngineService.estimateBilling(
			subscriptionId, periodStart, periodEnd);

		return ApiResponse.ok(result);
	}
}
