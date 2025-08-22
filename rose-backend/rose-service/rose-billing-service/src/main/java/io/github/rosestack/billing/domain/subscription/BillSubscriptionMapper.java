package io.github.rosestack.billing.domain.subscription;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.SubscriptionStatus;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订阅Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillSubscriptionMapper extends BaseMapper<BillSubscription> {
    
    /**
     * 根据订阅编号查找订阅
     */
    default BillSubscription findBySubNo(String subNo) {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getSubNo, subNo)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }
    
    /**
     * 根据套餐ID查找所有订阅
     */
    default List<BillSubscription> findByPlanId(Long planId) {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getPlanId, planId);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据状态查找订阅
     */
    default List<BillSubscription> findByStatus(SubscriptionStatus status) {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getStatus, status);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找活跃的订阅
     */
    default List<BillSubscription> findActiveSubscriptions() {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .in(BillSubscription::getStatus, 
                    SubscriptionStatus.TRIAL, 
                    SubscriptionStatus.ACTIVE, 
                    SubscriptionStatus.PAST_DUE);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找试用中的订阅
     */
    default List<BillSubscription> findTrialSubscriptions() {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getStatus, SubscriptionStatus.TRIAL);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找需要续费的订阅
     */
    default List<BillSubscription> findSubscriptionsNeedRenewal() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getAutoRenew, true)
                .ne(BillSubscription::getCancelAtPeriodEnd, true)
                .le(BillSubscription::getCurrentPeriodEndTime, now.plusDays(3)) // 提前3天检查
                .in(BillSubscription::getStatus, 
                    SubscriptionStatus.ACTIVE, 
                    SubscriptionStatus.TRIAL);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找即将过期的订阅
     */
    default List<BillSubscription> findExpiringSoon(int days) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(days);
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .isNotNull(BillSubscription::getEndTime)
                .le(BillSubscription::getEndTime, expireTime)
                .in(BillSubscription::getStatus, 
                    SubscriptionStatus.ACTIVE, 
                    SubscriptionStatus.TRIAL);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找超期未付费的订阅
     */
    default List<BillSubscription> findPastDueSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getStatus, SubscriptionStatus.PAST_DUE)
                .lt(BillSubscription::getNextBillingTime, now);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据租户ID和套餐ID查找活跃订阅
     */
    default List<BillSubscription> findActiveByTenantAndPlan(String tenantId, Long planId) {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getTenantId, tenantId)
                .eq(BillSubscription::getPlanId, planId)
                .in(BillSubscription::getStatus, 
                    SubscriptionStatus.TRIAL, 
                    SubscriptionStatus.ACTIVE, 
                    SubscriptionStatus.PAST_DUE);
        return selectList(queryWrapper);
    }
    
    /**
     * 检查订阅编号是否存在
     */
    default boolean existsBySubNo(String subNo) {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getSubNo, subNo);
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 统计指定状态的订阅数量
     */
    default long countByStatus(SubscriptionStatus status) {
        LambdaQueryWrapper<BillSubscription> queryWrapper = new LambdaQueryWrapper<BillSubscription>()
                .eq(BillSubscription::getStatus, status);
        return selectCount(queryWrapper);
    }
}