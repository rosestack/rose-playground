package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillFeatureService;
import io.github.rosestack.billing.domain.enums.FeatureType;
import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.core.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计费功能管理API控制器
 * <p>
 * 提供功能的创建、更新、查询、删除等RESTful接口
 * 支持多租户数据隔离和功能状态管理
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/billing/features")
@RequiredArgsConstructor
@Tag(name = "计费功能管理", description = "计费功能的CRUD操作和状态管理")
public class BillFeatureController {

	private final BillFeatureService billFeatureService;

	/**
	 * 创建功能
	 */
	@PostMapping
	@Operation(summary = "创建功能", description = "创建新的计费功能")
	public ApiResponse<BillFeature> createFeature(
		@Parameter(description = "功能信息") @RequestBody BillFeature feature) {

		log.info("创建功能请求: {}", feature.getCode());
		BillFeature result = billFeatureService.createFeature(feature);
		return ApiResponse.ok(result);
	}

	/**
	 * 更新功能
	 */
	@PutMapping("/{id}")
	@Operation(summary = "更新功能", description = "更新功能信息")
	public ApiResponse<BillFeature> updateFeature(
		@Parameter(description = "功能ID") @PathVariable Long id,
		@Parameter(description = "功能信息") @RequestBody BillFeature feature) {

		log.info("更新功能请求: id={}", id);
		feature.setId(id);
		BillFeature result = billFeatureService.updateFeature(feature);
		return ApiResponse.ok(result);
	}

	/**
	 * 根据ID获取功能
	 */
	@GetMapping("/{id}")
	@Operation(summary = "获取功能详情", description = "根据ID获取功能详细信息")
	public ApiResponse<BillFeature> getFeature(
		@Parameter(description = "功能ID") @PathVariable Long id) {

		log.debug("获取功能请求: id={}", id);
		BillFeature feature = billFeatureService.getFeatureById(id);
		return ApiResponse.ok(feature);
	}

	/**
	 * 根据租户和功能代码获取功能
	 */
	@GetMapping("/tenant/{tenantId}/code/{code}")
	@Operation(summary = "根据代码获取功能", description = "根据租户ID和功能代码获取功能")
	public ApiResponse<BillFeature> getFeatureByCode(
		@Parameter(description = "租户ID") @PathVariable String tenantId,
		@Parameter(description = "功能代码") @PathVariable String code) {

		log.debug("根据代码获取功能请求: tenantId={}, code={}", tenantId, code);
		BillFeature feature = billFeatureService.getFeatureByTenantAndCode(tenantId, code);
		return ApiResponse.ok(feature);
	}

	/**
	 * 获取租户的激活功能列表
	 */
	@GetMapping("/tenant/{tenantId}/enabled")
	@Operation(summary = "获取租户激活功能", description = "获取租户的所有激活功能列表")
	public ApiResponse<List<BillFeature>> getEnabledFeatures(
		@Parameter(description = "租户ID") @PathVariable String tenantId) {

		log.debug("获取租户激活功能请求: tenantId={}", tenantId);
		List<BillFeature> features = billFeatureService.getEnabledFeaturesByTenant(tenantId);
		return ApiResponse.ok(features);
	}

	/**
	 * 获取系统级激活功能列表
	 */
	@GetMapping("/system/enabled")
	@Operation(summary = "获取系统激活功能", description = "获取所有系统级激活功能列表")
	public ApiResponse<List<BillFeature>> getSystemEnabledFeatures() {

		log.debug("获取系统级激活功能请求");
		List<BillFeature> features = billFeatureService.getEnabledSystemFeatures();
		return ApiResponse.ok(features);
	}

	/**
	 * 根据类型获取功能列表
	 */
	@GetMapping("/tenant/{tenantId}/type/{type}")
	@Operation(summary = "根据类型获取功能", description = "根据租户ID和功能类型获取功能列表")
	public ApiResponse<List<BillFeature>> getFeaturesByType(
		@Parameter(description = "租户ID") @PathVariable String tenantId,
		@Parameter(description = "功能类型") @PathVariable FeatureType type) {

		log.debug("根据类型获取功能请求: tenantId={}, type={}", tenantId, type);
		List<BillFeature> features = billFeatureService.getFeaturesByTenantAndType(tenantId, type);
		return ApiResponse.ok(features);
	}

	/**
	 * 激活功能
	 */
	@PostMapping("/{id}/activate")
	@Operation(summary = "激活功能", description = "激活指定功能")
	public ApiResponse<Void> activateFeature(
		@Parameter(description = "功能ID") @PathVariable Long id) {

		log.info("激活功能请求: id={}", id);
		billFeatureService.activateFeature(id);
		return ApiResponse.ok();
	}

	/**
	 * 禁用功能
	 */
	@PostMapping("/{id}/deactivate")
	@Operation(summary = "禁用功能", description = "禁用指定功能")
	public ApiResponse<Void> deactivateFeature(
		@Parameter(description = "功能ID") @PathVariable Long id) {

		log.info("禁用功能请求: id={}", id);
		billFeatureService.deactivateFeature(id);
		return ApiResponse.ok();
	}

	/**
	 * 删除功能
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "删除功能", description = "删除指定功能")
	public ApiResponse<Void> deleteFeature(
		@Parameter(description = "功能ID") @PathVariable Long id) {

		log.info("删除功能请求: id={}", id);
		billFeatureService.deleteFeature(id);
		return ApiResponse.ok();
	}

	/**
	 * 创建系统级功能
	 */
	@PostMapping("/system")
	@Operation(summary = "创建系统功能", description = "创建系统级功能")
	public ApiResponse<BillFeature> createSystemFeature(
		@Parameter(description = "功能代码") @RequestParam String code,
		@Parameter(description = "功能名称") @RequestParam String name,
		@Parameter(description = "功能类型") @RequestParam FeatureType type,
		@Parameter(description = "计量单位", required = false) @RequestParam(required = false) String unit) {

		log.info("创建系统功能请求: code={}, name={}, type={}", code, name, type);

		return ApiResponse.ok(billFeatureService.createSystemFeature(code, name, type, unit));
	}

	/**
	 * 复制系统功能到租户
	 */
	@PostMapping("/tenant/{tenantId}/copy/{systemFeatureCode}")
	@Operation(summary = "复制系统功能", description = "将系统功能复制到指定租户")
	public ApiResponse<BillFeature> copySystemFeature(
		@Parameter(description = "租户ID") @PathVariable String tenantId,
		@Parameter(description = "系统功能代码") @PathVariable String systemFeatureCode) {

		log.info("复制系统功能请求: tenantId={}, systemFeatureCode={}", tenantId, systemFeatureCode);

		BillFeature feature = billFeatureService.copySystemFeatureToTenant(tenantId, systemFeatureCode);
		return ApiResponse.ok(feature);
	}

	/**
	 * 批量激活功能
	 */
	@PostMapping("/batch/activate")
	@Operation(summary = "批量激活功能", description = "批量激活多个功能")
	public ApiResponse<Void> batchActivateFeatures(
		@Parameter(description = "功能ID列表") @RequestBody List<Long> featureIds) {
		log.info("批量激活功能请求: count={}", featureIds != null ? featureIds.size() : 0);

		billFeatureService.activateFeatures(featureIds);
		return ApiResponse.ok();
	}

	/**
	 * 批量禁用功能
	 */
	@PostMapping("/batch/deactivate")
	@Operation(summary = "批量禁用功能", description = "批量禁用多个功能")
	public ApiResponse<Void> batchDeactivateFeatures(
		@Parameter(description = "功能ID列表") @RequestBody List<Long> featureIds) {

		log.info("批量禁用功能请求: count={}", featureIds != null ? featureIds.size() : 0);

		billFeatureService.deactivateFeatures(featureIds);
		return ApiResponse.ok();
	}

	/**
	 * 检查功能代码是否存在
	 */
	@GetMapping("/tenant/{tenantId}/exists/{code}")
	@Operation(summary = "检查功能代码", description = "检查功能代码是否已存在")
	public ApiResponse<Boolean> checkFeatureExists(
		@Parameter(description = "租户ID") @PathVariable String tenantId,
		@Parameter(description = "功能代码") @PathVariable String code) {
		log.debug("检查功能代码请求: tenantId={}, code={}", tenantId, code);

		boolean exists = billFeatureService.existsByTenantAndCode(tenantId, code);
		return ApiResponse.ok(exists);
	}

	/**
	 * 验证功能有效性
	 */
	@PostMapping("/validate")
	@Operation(summary = "验证功能", description = "验证功能数据是否有效")
	public ApiResponse<Boolean> validateFeature(
		@Parameter(description = "功能信息") @RequestBody BillFeature feature) {
		log.debug("验证功能请求: code={}", feature != null ? feature.getCode() : null);
		return ApiResponse.ok(billFeatureService.isValidFeature(feature));
	}
}
