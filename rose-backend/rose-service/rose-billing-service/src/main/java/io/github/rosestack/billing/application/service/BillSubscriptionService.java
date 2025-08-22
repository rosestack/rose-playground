package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.SubscriptionStatus;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.core.exception.BusinessException;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订阅管理服务
 *
 * 提供订阅的创建、更新、状态管理、续费等核心业务功能
 * 支持试用转正、订阅升级降级、优雅取消等企业级功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class BillSubscriptionService {

    private final BillSubscriptionMapper subscriptionMapper;
    private final BillPlanMapper planMapper;
    private final Timer subscriptionCreationTimer;

    public BillSubscriptionService(BillSubscriptionMapper subscriptionMapper,
                                 BillPlanMapper planMapper,
                                 Timer subscriptionCreationTimer) {
        this.subscriptionMapper = subscriptionMapper;
        this.planMapper = planMapper;
        this.subscriptionCreationTimer = subscriptionCreationTimer;
    }

    /**
     * 创建新订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public BillSubscription createSubscription(BillSubscription subscription) {
        return subscriptionCreationTimer.record(() -> {
            log.info("Creating new subscription for plan: {}", subscription.getPlanId());

            // 验证套餐是否存在且可用
            BillPlan plan = planMapper.selectById(subscription.getPlanId());
            if (plan == null) {
                throw new BusinessException("套餐不存在: " + subscription.getPlanId());
            }
            if (!plan.isAvailable()) {
                throw new BusinessException("套餐不可用: " + plan.getName());
            }

            // 生成唯一订阅编号
            String subNo = generateSubscriptionNo();
            while (subscriptionMapper.existsBySubNo(subNo)) {
                subNo = generateSubscriptionNo();
            }
            subscription.setSubNo(subNo);

            // 设置默认值
            if (subscription.getQuantity() == null) {
                subscription.setQuantity(1);
            }
            if (subscription.getAutoRenew() == null) {
                subscription.setAutoRenew(true);
            }
            if (subscription.getCancelAtPeriodEnd() == null) {
                subscription.setCancelAtPeriodEnd(false);
            }

            // 设置计费周期
            LocalDateTime now = LocalDateTime.now();
            subscription.setStartTime(now);
            subscription.setCurrentPeriodStartTime(now);
            
            // 根据套餐计费模式设置周期结束时间
            LocalDateTime periodEndTime = calculatePeriodEndTime(now, plan);
            subscription.setCurrentPeriodEndTime(periodEndTime);
            subscription.setNextBillingTime(periodEndTime);

            // 如果套餐支持试用且用户申请试用，设置为试用状态
            if (plan.getTrialEnabled() && subscription.isTrial()) {
                subscription.setStatus(SubscriptionStatus.TRIAL);
                // 试用期结束时间
                if (plan.getTrialDays() != null && plan.getTrialDays() > 0) {
                    subscription.setEndTime(now.plusDays(plan.getTrialDays()));
                }
            } else {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }

            // 保存订阅
            subscriptionMapper.insert(subscription);
            log.info("Subscription created successfully: id={}, subNo={}", 
                    subscription.getId(), subscription.getSubNo());

            return subscription;
        });
    }

    /**
     * 试用转正
     */
    @Transactional(rollbackFor = Exception.class)
    public BillSubscription convertTrialToActive(Long subscriptionId) {
        log.info("Converting trial subscription to active: {}", subscriptionId);

        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        if (!subscription.isTrial()) {
            throw new BusinessException("订阅不是试用状态，无法转正: " + subscription.getStatus());
        }

        // 转为正式订阅
        subscription.activate();
        subscription.setEndTime(null); // 清除试用期限制

        // 重新计算计费周期
        LocalDateTime now = LocalDateTime.now();
        BillPlan plan = planMapper.selectById(subscription.getPlanId());
        LocalDateTime periodEndTime = calculatePeriodEndTime(now, plan);
        
        subscription.setCurrentPeriodStartTime(now);
        subscription.setCurrentPeriodEndTime(periodEndTime);
        subscription.setNextBillingTime(periodEndTime);

        subscriptionMapper.updateById(subscription);
        log.info("Trial subscription converted to active: {}", subscriptionId);

        return subscription;
    }

    /**
     * 续费订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public BillSubscription renewSubscription(Long subscriptionId) {
        log.info("Renewing subscription: {}", subscriptionId);

        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        if (!subscription.needsRenewal()) {
            throw new BusinessException("订阅不需要续费或已被取消: " + subscription.getStatus());
        }

        // 延长计费周期
        BillPlan plan = planMapper.selectById(subscription.getPlanId());
        LocalDateTime currentPeriodEnd = subscription.getCurrentPeriodEndTime();
        LocalDateTime newPeriodEnd = calculatePeriodEndTime(currentPeriodEnd, plan);

        subscription.setCurrentPeriodStartTime(currentPeriodEnd);
        subscription.setCurrentPeriodEndTime(newPeriodEnd);
        subscription.setNextBillingTime(newPeriodEnd);

        // 确保订阅状态为活跃
        if (!subscription.isActive()) {
            subscription.activate();
        }

        subscriptionMapper.updateById(subscription);
        log.info("Subscription renewed successfully: {}", subscriptionId);

        return subscription;
    }

    /**
     * 暂停订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void suspendSubscription(Long subscriptionId, String reason) {
        log.info("Suspending subscription: {}, reason: {}", subscriptionId, reason);

        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        subscription.suspend();
        subscriptionMapper.updateById(subscription);
        
        log.info("Subscription suspended: {}", subscriptionId);
    }

    /**
     * 取消订阅（优雅取消，服务到周期结束）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(Long subscriptionId) {
        log.info("Cancelling subscription: {}", subscriptionId);

        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        subscription.cancel();
        subscriptionMapper.updateById(subscription);
        
        log.info("Subscription cancelled gracefully: {}", subscriptionId);
    }

    /**
     * 立即取消订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscriptionImmediately(Long subscriptionId) {
        log.info("Cancelling subscription immediately: {}", subscriptionId);

        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        subscription.cancelImmediately();
        subscriptionMapper.updateById(subscription);
        
        log.info("Subscription cancelled immediately: {}", subscriptionId);
    }

    /**
     * 重新激活订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public BillSubscription reactivateSubscription(Long subscriptionId) {
        log.info("Reactivating subscription: {}", subscriptionId);

        BillSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        if (subscription.isActive()) {
            throw new BusinessException("订阅已经是活跃状态: " + subscription.getStatus());
        }

        subscription.activate();
        subscriptionMapper.updateById(subscription);
        
        log.info("Subscription reactivated: {}", subscriptionId);
        return subscription;
    }

    /**
     * 根据ID查找订阅
     */
    public BillSubscription findById(Long id) {
        return subscriptionMapper.selectById(id);
    }

    /**
     * 根据订阅编号查找订阅
     */
    public BillSubscription findBySubNo(String subNo) {
        return subscriptionMapper.findBySubNo(subNo);
    }

    /**
     * 查找活跃的订阅
     */
    public List<BillSubscription> findActiveSubscriptions() {
        return subscriptionMapper.findActiveSubscriptions();
    }

    /**
     * 查找需要续费的订阅
     */
    public List<BillSubscription> findSubscriptionsNeedRenewal() {
        return subscriptionMapper.findSubscriptionsNeedRenewal();
    }

    /**
     * 查找即将过期的订阅
     */
    public List<BillSubscription> findExpiringSoon(int days) {
        return subscriptionMapper.findExpiringSoon(days);
    }

    /**
     * 查找超期未付费的订阅
     */
    public List<BillSubscription> findPastDueSubscriptions() {
        return subscriptionMapper.findPastDueSubscriptions();
    }

    /**
     * 处理过期订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");

        List<BillSubscription> expiredSubscriptions = findExpiringSoon(0); // 今天到期的
        for (BillSubscription subscription : expiredSubscriptions) {
            if (subscription.needsRenewal()) {
                try {
                    renewSubscription(subscription.getId());
                } catch (Exception e) {
                    log.error("Failed to renew subscription: {}", subscription.getId(), e);
                    // 续费失败，标记为过期
                    subscription.expire();
                    subscriptionMapper.updateById(subscription);
                }
            } else {
                subscription.expire();
                subscriptionMapper.updateById(subscription);
            }
        }

        log.info("Processed {} expired subscriptions", expiredSubscriptions.size());
    }

    /**
     * 生成订阅编号
     */
    private String generateSubscriptionNo() {
        return "SUB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 计算计费周期结束时间
     */
    private LocalDateTime calculatePeriodEndTime(LocalDateTime startTime, BillPlan plan) {
        // 根据套餐的计费模式计算周期结束时间
        // 这里简化为月付，实际应该根据plan.getBillingMode()进行计算
        return startTime.plusMonths(1);
    }
}