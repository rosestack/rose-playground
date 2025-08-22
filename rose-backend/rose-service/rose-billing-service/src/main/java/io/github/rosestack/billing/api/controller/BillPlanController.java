package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillPlanService;
import io.github.rosestack.billing.domain.enums.PlanType;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.core.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理API控制器
 *
 * 提供套餐的增删改查、状态管理等API接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/plans")
@RequiredArgsConstructor
@Slf4j
public class BillPlanController {

    private final BillPlanService planService;

    /**
     * 创建套餐
     */
    @PostMapping
    public ApiResponse<BillPlan> createPlan(@Valid @RequestBody BillPlan plan) {
        log.info("Creating plan: {}", plan.getCode());
        BillPlan createdPlan = planService.createPlan(plan);
        return ApiResponse.ok(createdPlan);
    }

    /**
     * 更新套餐
     */
    @PutMapping("/{id}")
    public ApiResponse<BillPlan> updatePlan(@PathVariable Long id, @Valid @RequestBody BillPlan plan) {
        log.info("Updating plan: {}", id);
        plan.setId(id);
        BillPlan updatedPlan = planService.updatePlan(plan);
        return ApiResponse.ok(updatedPlan);
    }

    /**
     * 根据ID查询套餐
     */
    @GetMapping("/{id}")
    public ApiResponse<BillPlan> getPlan(@PathVariable Long id) {
        BillPlan plan = planService.findById(id);
        if (plan == null) {
            return ApiResponse.error("套餐不存在");
        }
        return ApiResponse.ok(plan);
    }

    /**
     * 根据代码查询套餐（最新版本）
     */
    @GetMapping("/code/{code}")
    public ApiResponse<BillPlan> getPlanByCode(@PathVariable String code) {
        BillPlan plan = planService.findByCode(code);
        if (plan == null) {
            return ApiResponse.error("套餐不存在");
        }
        return ApiResponse.ok(plan);
    }

    /**
     * 根据代码和版本查询套餐
     */
    @GetMapping("/code/{code}/version/{version}")
    public ApiResponse<BillPlan> getPlanByCodeAndVersion(@PathVariable String code, @PathVariable String version) {
        BillPlan plan = planService.findByCodeAndVersion(code, version);
        if (plan == null) {
            return ApiResponse.error("套餐不存在");
        }
        return ApiResponse.ok(plan);
    }

    /**
     * 查询所有可用套餐
     */
    @GetMapping("/available")
    public ApiResponse<List<BillPlan>> getAvailablePlans() {
        List<BillPlan> plans = planService.findAvailablePlans();
        return ApiResponse.ok(plans);
    }

    /**
     * 根据套餐类型查询套餐
     */
    @GetMapping("/type/{planType}")
    public ApiResponse<List<BillPlan>> getPlansByType(@PathVariable PlanType planType) {
        List<BillPlan> plans = planService.findByPlanType(planType);
        return ApiResponse.ok(plans);
    }

    /**
     * 查询免费套餐
     */
    @GetMapping("/free")
    public ApiResponse<List<BillPlan>> getFreePlans() {
        List<BillPlan> plans = planService.findFreePlans();
        return ApiResponse.ok(plans);
    }

    /**
     * 查询付费套餐
     */
    @GetMapping("/paid")
    public ApiResponse<List<BillPlan>> getPaidPlans() {
        List<BillPlan> plans = planService.findPaidPlans();
        return ApiResponse.ok(plans);
    }

    /**
     * 查询支持试用的套餐
     */
    @GetMapping("/trial-enabled")
    public ApiResponse<List<BillPlan>> getTrialEnabledPlans() {
        List<BillPlan> plans = planService.findTrialEnabledPlans();
        return ApiResponse.ok(plans);
    }

    /**
     * 激活套餐
     */
    @PostMapping("/{id}/activate")
    public ApiResponse<Void> activatePlan(@PathVariable Long id) {
        log.info("Activating plan: {}", id);
        planService.activatePlan(id);
        return ApiResponse.ok();
    }

    /**
     * 禁用套餐
     */
    @PostMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivatePlan(@PathVariable Long id) {
        log.info("Deactivating plan: {}", id);
        planService.deactivatePlan(id);
        return ApiResponse.ok();
    }

    /**
     * 弃用套餐
     */
    @PostMapping("/{id}/deprecate")
    public ApiResponse<Void> deprecatePlan(@PathVariable Long id) {
        log.info("Deprecating plan: {}", id);
        planService.deprecatePlan(id);
        return ApiResponse.ok();
    }

    /**
     * 归档套餐
     */
    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archivePlan(@PathVariable Long id) {
        log.info("Archiving plan: {}", id);
        planService.archivePlan(id);
        return ApiResponse.ok();
    }

    /**
     * 创建套餐新版本
     */
    @PostMapping("/{code}/versions")
    public ApiResponse<BillPlan> createNewVersion(
            @PathVariable String code,
            @RequestParam String newVersion,
            @RequestParam(required = false) String description) {
        log.info("Creating new version for plan: {}, version: {}", code, newVersion);
        BillPlan plan = planService.createNewVersion(code, newVersion, description);
        return ApiResponse.ok(plan);
    }

    /**
     * 设置试用配置
     */
    @PostMapping("/{id}/trial-config")
    public ApiResponse<Void> setTrialConfig(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limitPerUser) {
        log.info("Setting trial config for plan: {}", id);
        planService.setTrialConfig(id, enabled, days, limitPerUser);
        return ApiResponse.ok();
    }

    /**
     * 检查套餐是否可以被订阅
     */
    @GetMapping("/{id}/can-subscribe")
    public ApiResponse<Boolean> canBeSubscribed(@PathVariable Long id) {
        boolean canSubscribe = planService.canBeSubscribed(id);
        return ApiResponse.ok(canSubscribe);
    }

    /**
     * 获取套餐统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<List<BillPlanService.PlanStatusCount>> getPlanStatistics() {
        List<BillPlanService.PlanStatusCount> statistics = planService.getPlanStatistics();
        return ApiResponse.ok(statistics);
    }

    /**
     * 查询即将过期的套餐
     */
    @GetMapping("/expiring-soon")
    public ApiResponse<List<BillPlan>> getExpiringSoonPlans(@RequestParam(defaultValue = "30") int days) {
        List<BillPlan> plans = planService.findExpiringSoon(days);
        return ApiResponse.ok(plans);
    }
}