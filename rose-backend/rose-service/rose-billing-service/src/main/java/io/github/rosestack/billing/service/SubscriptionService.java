package io.github.rosestack.billing.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.entity.TenantSubscription;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.exception.PlanNotFoundException;
import io.github.rosestack.billing.exception.SubscriptionNotFoundException;
import io.github.rosestack.billing.repository.SubscriptionPlanRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订阅管理服务
 *
 * @author rose
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService extends ServiceImpl<TenantSubscriptionRepository, TenantSubscription> {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PricingCalculator pricingCalculator;

    /**
     * 获取租户的活跃订阅
     *
     * @param tenantId 租户ID
     * @return 活跃的订阅信息，如果没有则返回空
     * @throws IllegalArgumentException 如果租户ID为空
     */
    @Cacheable(value = "activeSubscriptions", key = "#tenantId", unless = "#result.isEmpty()")
    public Optional<TenantSubscription> getActiveSubscription(
            @jakarta.validation.constraints.NotBlank String tenantId) {
        try {
            return subscriptionRepository.findActiveByTenantId(tenantId);
        } catch (Exception e) {
            log.error("获取租户活跃订阅失败: tenantId={}", tenantId, e);
            return Optional.empty();
        }
    }

    /**
     * 检查租户是否有活跃订阅
     */
    public boolean hasActiveSubscription(String tenantId) {
        return getActiveSubscription(tenantId).isPresent();
    }

    /**
     * 获取租户的订阅历史
     */
    public List<TenantSubscription> getSubscriptionHistory(String tenantId) {
        // 使用 LambdaQueryWrapper 查询租户的所有订阅
        return subscriptionRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TenantSubscription>()
                        .eq(TenantSubscription::getTenantId, tenantId)
                        .eq(TenantSubscription::getDeleted, false)
                        .orderByDesc(TenantSubscription::getCreatedTime));
    }

    /**
     * 暂停订阅
     */
    @Transactional
    @CacheEvict(value = "activeSubscriptions", key = "#subscription.tenantId")
    public void pauseSubscription(String subscriptionId, String reason) {
        TenantSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }

        subscription.setStatus(SubscriptionStatus.PAUSED);
        subscription.setPausedTime(LocalDateTime.now());
        subscription.setPauseReason(reason);

        subscriptionRepository.updateById(subscription);
        log.info("订阅已暂停: {}, 原因: {}", subscriptionId, reason);
    }

    /**
     * 恢复订阅
     */
    @Transactional
    public void resumeSubscription(String subscriptionId) {
        TenantSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPausedTime(null);
        subscription.setPauseReason(null);

        subscriptionRepository.updateById(subscription);
        log.info("订阅已恢复: {}", subscriptionId);
    }

    /**
     * 升级订阅计划
     */
    @Transactional
    public void upgradeSubscription(String subscriptionId, String newPlanId) {
        TenantSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }

        SubscriptionPlan newPlan = planRepository.selectById(newPlanId);
        if (newPlan == null) {
            throw new PlanNotFoundException(newPlanId);
        }

        SubscriptionPlan currentPlan = planRepository.selectById(subscription.getPlanId());
        if (currentPlan != null && newPlan.getBasePrice().compareTo(currentPlan.getBasePrice()) <= 0) {
            throw new IllegalArgumentException("新计划价格必须高于当前计划");
        }

        subscription.setPlanId(newPlanId);
        subscription.setCurrentPeriodAmount(newPlan.getBasePrice());
        subscription.setUpgradedTime(LocalDateTime.now());

        subscriptionRepository.updateById(subscription);
        log.info("订阅已升级: {} -> {}", subscription.getPlanId(), newPlanId);
    }

    /**
     * 检查试用期是否即将到期
     */
    public List<TenantSubscription> getTrialExpiringSoon(int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);
        return subscriptionRepository.findTrialExpiringSoon(startDate, endDate);
    }

    /**
     * 获取需要计费的订阅
     */
    public List<TenantSubscription> getSubscriptionsForBilling() {
        List<SubscriptionStatus> activeStatuses = List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL);
        return subscriptionRepository.findByNextBillingDateBeforeAndStatusIn(LocalDateTime.now(), activeStatuses);
    }

    /**
     * 验证使用量限制
     *
     * @param tenantId   租户ID
     * @param metricType 计量类型
     * @return true 如果在限制范围内，false 如果超出限制或无有效订阅
     */
    public boolean validateUsageLimit(String tenantId, String metricType) {
        if (tenantId == null || metricType == null) {
            log.warn("验证使用量限制参数无效: tenantId={}, metricType={}", tenantId, metricType);
            return false;
        }

        try {
            Optional<TenantSubscription> subscriptionOpt = getActiveSubscription(tenantId);
            if (subscriptionOpt.isEmpty()) {
                log.debug("租户 {} 没有活跃订阅，拒绝使用量", tenantId);
                return false;
            }

            TenantSubscription subscription = subscriptionOpt.get();
            SubscriptionPlan plan = planRepository.selectById(subscription.getPlanId());
            if (plan == null) {
                log.warn("订阅计划不存在: planId={}", subscription.getPlanId());
                return false;
            }

            boolean isValid = !pricingCalculator.isUsageExceeded(tenantId, plan, metricType);
            log.debug("使用量限制验证结果: tenantId={}, metricType={}, valid={}", tenantId, metricType, isValid);
            return isValid;
        } catch (Exception e) {
            log.error("验证使用量限制失败: tenantId={}, metricType={}", tenantId, metricType, e);
            return false;
        }
    }
}
