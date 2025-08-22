package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.PlanStatus;
import io.github.rosestack.billing.domain.enums.PlanType;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 套餐管理服务
 *
 * 提供套餐的创建、更新、查询、状态管理等业务功能
 * 重构后调用Mapper的自定义方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillPlanService {

    private final BillPlanMapper planMapper;

    /**
     * 创建新套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public BillPlan createPlan(BillPlan plan) {
        log.info("Creating new plan: {}", plan.getCode());

        // 验证套餐代码和版本的唯一性
        if (planMapper.existsByCodeAndVersion(plan.getCode(), plan.getVersion())) {
            throw new BusinessException("套餐代码和版本组合已存在: " + plan.getCode() + "-" + plan.getVersion());
        }

        // 设置默认值
        if (plan.getStatus() == null) {
            plan.setStatus(PlanStatus.DRAFT);
        }

        if (plan.getTrialEnabled() == null) {
            plan.setTrialEnabled(false);
        }

        if (plan.getTrialLimitPerUser() == null) {
            plan.setTrialLimitPerUser(1);
        }

        // 保存套餐
        planMapper.insert(plan);
        log.info("Plan created successfully: id={}, code={}", plan.getId(), plan.getCode());

        return plan;
    }

    /**
     * 更新套餐信息
     */
    @Transactional(rollbackFor = Exception.class)
    public BillPlan updatePlan(BillPlan plan) {
        log.info("Updating plan: id={}, code={}", plan.getId(), plan.getCode());

        BillPlan existingPlan = planMapper.selectById(plan.getId());
        if (existingPlan == null) {
            throw new BusinessException("套餐不存在: " + plan.getId());
        }

        // 如果修改了代码或版本，需要检查唯一性
        if (!existingPlan.getCode().equals(plan.getCode()) ||
            !existingPlan.getVersion().equals(plan.getVersion())) {
            if (planMapper.existsByCodeAndVersion(plan.getCode(), plan.getVersion())) {
                throw new BusinessException("套餐代码和版本组合已存在: " + plan.getCode() + "-" + plan.getVersion());
            }
        }

        planMapper.updateById(plan);
        log.info("Plan updated successfully: id={}, code={}", plan.getId(), plan.getCode());

        return plan;
    }

    /**
     * 根据ID查找套餐
     */
    public BillPlan findById(Long id) {
        return planMapper.selectById(id);
    }

    /**
     * 根据代码查找套餐（最新版本）
     */
    public BillPlan findByCode(String code) {
        return planMapper.findLatestByCode(code);
    }

    /**
     * 根据代码和版本查找套餐
     */
    public BillPlan findByCodeAndVersion(String code, String version) {
        return planMapper.findByCodeAndVersion(code, version);
    }

    /**
     * 查找所有可用的套餐
     */
    public List<BillPlan> findAvailablePlans() {
        return planMapper.findAvailablePlans();
    }

    /**
     * 根据套餐类型查找套餐
     */
    public List<BillPlan> findByPlanType(PlanType planType) {
        return planMapper.findByPlanType(planType);
    }

    /**
     * 查找免费套餐
     */
    public List<BillPlan> findFreePlans() {
        return planMapper.findFreePlans();
    }

    /**
     * 查找付费套餐
     */
    public List<BillPlan> findPaidPlans() {
        return planMapper.findPaidPlans();
    }

    /**
     * 查找支持试用的套餐
     */
    public List<BillPlan> findTrialEnabledPlans() {
        return planMapper.findTrialEnabledPlans();
    }

    /**
     * 激活套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public void activatePlan(Long planId) {
        log.info("Activating plan: {}", planId);

        BillPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("套餐不存在: " + planId);
        }

        plan.activate();
        planMapper.updateById(plan);

        log.info("Plan activated successfully: {}", planId);
    }

    /**
     * 禁用套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivatePlan(Long planId) {
        log.info("Deactivating plan: {}", planId);

        BillPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("套餐不存在: " + planId);
        }

        plan.deactivate();
        planMapper.updateById(plan);

        log.info("Plan deactivated successfully: {}", planId);
    }

    /**
     * 弃用套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public void deprecatePlan(Long planId) {
        log.info("Deprecating plan: {}", planId);

        BillPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("套餐不存在: " + planId);
        }

        plan.deprecate();
        planMapper.updateById(plan);

        log.info("Plan deprecated successfully: {}", planId);
    }

    /**
     * 归档套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public void archivePlan(Long planId) {
        log.info("Archiving plan: {}", planId);

        BillPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("套餐不存在: " + planId);
        }

        plan.archive();
        planMapper.updateById(plan);

        log.info("Plan archived successfully: {}", planId);
    }

    /**
     * 创建套餐新版本
     */
    @Transactional(rollbackFor = Exception.class)
    public BillPlan createNewVersion(String code, String newVersion, String description) {
        log.info("Creating new version for plan: code={}, version={}", code, newVersion);

        // 使用Mapper的自定义方法查找最新版本作为模板
        BillPlan latestPlan = planMapper.findLatestByCode(code);
        if (latestPlan == null) {
            throw new BusinessException("套餐不存在: " + code);
        }

        // 检查新版本号是否已存在
        if (planMapper.existsByCodeAndVersion(code, newVersion)) {
            throw new BusinessException("套餐版本已存在: " + code + "-" + newVersion);
        }

        // 创建新版本
        BillPlan newPlan = new BillPlan();
        // 复制基本信息
        newPlan.setCode(latestPlan.getCode());
        newPlan.setName(latestPlan.getName());
        newPlan.setVersion(newVersion);
        newPlan.setDescription(description);
        newPlan.setPlanType(latestPlan.getPlanType());
        newPlan.setBillingMode(latestPlan.getBillingMode());
        newPlan.setTrialEnabled(latestPlan.getTrialEnabled());
        newPlan.setTrialDays(latestPlan.getTrialDays());
        newPlan.setTrialLimitPerUser(latestPlan.getTrialLimitPerUser());
        newPlan.setTenantId(latestPlan.getTenantId());

        // 新版本默认为草稿状态
        newPlan.setStatus(PlanStatus.DRAFT);

        planMapper.insert(newPlan);
        log.info("New plan version created: id={}, code={}, version={}",
                newPlan.getId(), newPlan.getCode(), newPlan.getVersion());

        return newPlan;
    }

    /**
     * 设置套餐试用配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void setTrialConfig(Long planId, boolean enabled, Integer days, Integer limitPerUser) {
        log.info("Setting trial config for plan: id={}, enabled={}, days={}", planId, enabled, days);

        BillPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("套餐不存在: " + planId);
        }

        plan.setTrialConfig(enabled, days, limitPerUser);
        planMapper.updateById(plan);

        log.info("Trial config updated for plan: {}", planId);
    }

    /**
     * 检查套餐是否可以被订阅
     */
    public boolean canBeSubscribed(Long planId) {
        BillPlan plan = planMapper.selectById(planId);
        return plan != null && plan.isAvailable();
    }

    /**
     * 获取套餐统计信息
     */
    public List<PlanStatusCount> getPlanStatistics() {
        // 使用LambdaQueryWrapper查询所有套餐
        List<BillPlan> allPlans = planMapper.selectList(null);

        // 按状态分组统计
        Map<PlanStatus, Long> statusCountMap = allPlans.stream()
                .collect(Collectors.groupingBy(
                        BillPlan::getStatus,
                        Collectors.counting()
                ));

        // 转换为结果对象
        return statusCountMap.entrySet().stream()
                .map(entry -> new PlanStatusCount(entry.getKey().name(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 内部类：套餐状态统计（用于API返回）
     */
    public static class PlanStatusCount {
        private String status;
        private Long count;

        public PlanStatusCount() {}

        public PlanStatusCount(String status, Long count) {
            this.status = status;
            this.count = count;
        }

        // getter and setter
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    /**
     * 查找即将过期的套餐
     */
    public List<BillPlan> findExpiringSoon(int days) {
        return planMapper.findExpiringSoon(days);
    }
}
