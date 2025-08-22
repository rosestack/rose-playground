package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillUsageService;
import io.github.rosestack.billing.domain.usage.BillUsage;
import io.github.rosestack.core.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用量统计API控制器
 * <p>
 * 提供用量记录、统计查询、配额检查等API接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/usage")
@RequiredArgsConstructor
@Slf4j
public class BillUsageController {

	private final BillUsageService usageService;

	/**
	 * 记录用量
	 */
	@PostMapping
	public ApiResponse<BillUsage> recordUsage(@Valid @RequestBody BillUsage usage) {
		log.debug("Recording usage for subscription: {}, feature: {}",
			usage.getSubscriptionId(), usage.getFeatureId());
		BillUsage recordedUsage = usageService.recordUsage(usage);
		return ApiResponse.ok(recordedUsage);
	}

	/**
	 * 批量记录用量
	 */
	@PostMapping("/batch")
	public ApiResponse<Void> recordUsageBatch(@Valid @RequestBody List<BillUsage> usages) {
		log.info("Recording batch usage: {} records", usages.size());
		usageService.recordUsageBatch(usages);
		return ApiResponse.ok();
	}

	/**
	 * 记录API调用用量
	 */
	@PostMapping("/api-call")
	public ApiResponse<BillUsage> recordApiUsage(
		@RequestParam String tenantId,
		@RequestParam Long subscriptionId,
		@RequestParam Long featureId,
		@RequestParam String endpoint,
		@RequestParam String method,
		@RequestParam int statusCode) {

		BillUsage usage = usageService.recordApiUsage(tenantId, subscriptionId, featureId, endpoint, method, statusCode);
		return ApiResponse.ok(usage);
	}

	/**
	 * 记录存储用量
	 */
	@PostMapping("/storage")
	public ApiResponse<BillUsage> recordStorageUsage(
		@RequestParam String tenantId,
		@RequestParam Long subscriptionId,
		@RequestParam Long featureId,
		@RequestParam BigDecimal sizeInBytes,
		@RequestParam String fileType) {

		BillUsage usage = usageService.recordStorageUsage(tenantId, subscriptionId, featureId, sizeInBytes, fileType);
		return ApiResponse.ok(usage);
	}

	/**
	 * 查询订阅在指定计费周期的用量统计
	 */
	@GetMapping("/subscription/{subscriptionId}/period/{billingPeriod}")
	public ApiResponse<Map<Long, BigDecimal>> getUsageBySubscriptionAndPeriod(
		@PathVariable Long subscriptionId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingPeriod) {

		Map<Long, BigDecimal> usage = usageService.getUsageBySubscriptionAndPeriod(subscriptionId, billingPeriod);
		return ApiResponse.ok(usage);
	}

	/**
	 * 查询功能在指定计费周期的总用量
	 */
	@GetMapping("/subscription/{subscriptionId}/feature/{featureId}/period/{billingPeriod}")
	public ApiResponse<BigDecimal> getFeatureUsageInPeriod(
		@PathVariable Long subscriptionId,
		@PathVariable Long featureId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingPeriod) {

		BigDecimal usage = usageService.getFeatureUsageInPeriod(subscriptionId, featureId, billingPeriod);
		return ApiResponse.ok(usage);
	}

	/**
	 * 查询当月用量统计
	 */
	@GetMapping("/subscription/{subscriptionId}/current-month")
	public ApiResponse<Map<Long, BigDecimal>> getCurrentMonthUsage(@PathVariable Long subscriptionId) {
		Map<Long, BigDecimal> usage = usageService.getCurrentMonthUsage(subscriptionId);
		return ApiResponse.ok(usage);
	}

	/**
	 * 查询用量趋势（最近几个月）
	 */
	@GetMapping("/subscription/{subscriptionId}/trend")
	public ApiResponse<Map<LocalDate, Map<Long, BigDecimal>>> getUsageTrend(
		@PathVariable Long subscriptionId,
		@RequestParam(defaultValue = "6") int months) {

		Map<LocalDate, Map<Long, BigDecimal>> trend = usageService.getUsageTrend(subscriptionId, months);
		return ApiResponse.ok(trend);
	}

	/**
	 * 检查配额是否充足
	 */
	@GetMapping("/subscription/{subscriptionId}/feature/{featureId}/quota-check")
	public ApiResponse<Boolean> checkQuotaAvailable(
		@PathVariable Long subscriptionId,
		@PathVariable Long featureId,
		@RequestParam BigDecimal requestedAmount) {

		boolean available = usageService.checkQuotaAvailable(subscriptionId, featureId, requestedAmount);
		return ApiResponse.ok(available);
	}

	/**
	 * 获取配额使用情况
	 */
	@GetMapping("/subscription/{subscriptionId}/feature/{featureId}/quota")
	public ApiResponse<BillUsageService.QuotaUsage> getQuotaUsage(
		@PathVariable Long subscriptionId,
		@PathVariable Long featureId) {

		BillUsageService.QuotaUsage quotaUsage = usageService.getQuotaUsage(subscriptionId, featureId);
		return ApiResponse.ok(quotaUsage);
	}

	/**
	 * 生成用量报表
	 */
	@GetMapping("/subscription/{subscriptionId}/report")
	public ApiResponse<BillUsageService.UsageReport> generateUsageReport(
		@PathVariable Long subscriptionId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		BillUsageService.UsageReport report = usageService.generateUsageReport(subscriptionId, startDate, endDate);
		return ApiResponse.ok(report);
	}

	/**
	 * 清理历史用量数据（管理员接口）
	 */
	@PostMapping("/cleanup-historical")
	public ApiResponse<Void> cleanupHistoricalUsage(
		@RequestParam(defaultValue = "12") int monthsToKeep) {

		log.info("Cleaning up historical usage data, keeping {} months", monthsToKeep);
		usageService.cleanupHistoricalUsage(monthsToKeep);
		return ApiResponse.ok();
	}
}