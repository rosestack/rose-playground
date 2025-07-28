package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.BaseTenantSubscription;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 租户订阅数据访问接口
 *
 * @author rose
 */
@Mapper
public interface TenantSubscriptionRepository extends BaseMapper<BaseTenantSubscription> {

    /**
     * 根据租户ID查找当前订阅
     */
    default Optional<BaseTenantSubscription> findByTenantId(String tenantId) {
        LambdaQueryWrapper<BaseTenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseTenantSubscription::getTenantId, tenantId)
                .eq(BaseTenantSubscription::getDeleted, false);
        BaseTenantSubscription subscription = selectOne(wrapper);
        return Optional.ofNullable(subscription);
    }

    /**
     * 查找活跃订阅
     */
    default Optional<BaseTenantSubscription> findByTenantIdAndStatus(String tenantId, SubscriptionStatus status) {
        LambdaQueryWrapper<BaseTenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseTenantSubscription::getTenantId, tenantId)
                .eq(BaseTenantSubscription::getStatus, status)
                .eq(BaseTenantSubscription::getDeleted, false);
        BaseTenantSubscription subscription = selectOne(wrapper);
        return Optional.ofNullable(subscription);
    }

    /**
     * 根据租户ID查找活跃订阅
     */
    @Select("SELECT * FROM tenant_subscription WHERE tenant_id = #{tenantId} " +
            "AND status IN ('ACTIVE', 'TRIAL') AND deleted = 0 LIMIT 1")
    Optional<BaseTenantSubscription> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * 查找需要计费的订阅
     */
    default List<BaseTenantSubscription> findByNextBillingDateBeforeAndStatusIn(
            LocalDateTime date, List<SubscriptionStatus> statuses) {
        LambdaQueryWrapper<BaseTenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(BaseTenantSubscription::getNextBillingDate, date)
                .in(BaseTenantSubscription::getStatus, statuses)
                .eq(BaseTenantSubscription::getDeleted, false);
        return selectList(wrapper);
    }

    /**
     * 查找试用期即将到期的订阅
     */
    @Select("SELECT * FROM tenant_subscription WHERE in_trial = 1 " +
            "AND trial_end_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    List<BaseTenantSubscription> findTrialExpiringSoon(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 统计租户订阅数
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") SubscriptionStatus status);

    /**
     * 获取订阅状态统计
     */
    @Select("SELECT " +
            "status, " +
            "COUNT(*) as count, " +
            "SUM(current_period_amount) as totalAmount " +
            "FROM tenant_subscription " +
            "WHERE deleted = 0 " +
            "GROUP BY status")
    List<java.util.Map<String, Object>> getSubscriptionStatusStats();

    /**
     * 获取即将到期的订阅
     */
    @Select("SELECT * FROM tenant_subscription " +
            "WHERE next_billing_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status IN ('ACTIVE', 'TRIAL') AND deleted = 0 " +
            "ORDER BY next_billing_date")
    List<BaseTenantSubscription> findSubscriptionsExpiringBetween(@Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 获取按计划分组的订阅统计
     */
    @Select("SELECT " +
            "plan_id as planId, " +
            "COUNT(*) as subscriptionCount, " +
            "COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as activeCount, " +
            "COUNT(CASE WHEN status = 'TRIAL' THEN 1 END) as trialCount " +
            "FROM tenant_subscription " +
            "WHERE deleted = 0 " +
            "GROUP BY plan_id")
    List<java.util.Map<String, Object>> getSubscriptionStatsByPlan();
}
