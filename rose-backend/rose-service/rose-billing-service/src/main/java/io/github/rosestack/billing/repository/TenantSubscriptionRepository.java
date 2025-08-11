package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.TenantSubscription;
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
public interface TenantSubscriptionRepository extends BaseMapper<TenantSubscription> {

    /**
     * 根据租户ID查找当前订阅
     */
    default Optional<TenantSubscription> findByTenantId(String tenantId) {
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getTenantId, tenantId)
                .eq(TenantSubscription::getDeleted, false);
        TenantSubscription subscription = selectOne(wrapper);
        return Optional.ofNullable(subscription);
    }

    /**
     * 查找活跃订阅
     */
    default Optional<TenantSubscription> findByTenantIdAndStatus(String tenantId, SubscriptionStatus status) {
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getTenantId, tenantId)
                .eq(TenantSubscription::getStatus, status)
                .eq(TenantSubscription::getDeleted, false);
        TenantSubscription subscription = selectOne(wrapper);
        return Optional.ofNullable(subscription);
    }

    /**
     * 根据租户ID查找活跃订阅
     */
    @Select("SELECT * FROM tenant_subscription WHERE tenant_id = #{tenantId} " +
            "AND status IN ('ACTIVE', 'TRIAL') AND deleted = 0 LIMIT 1")
    Optional<TenantSubscription> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * 查找需要计费的订阅
     */
    default List<TenantSubscription> findByNextBillingDateBeforeAndStatusIn(
            LocalDateTime date, List<SubscriptionStatus> statuses) {
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(TenantSubscription::getNextBillingDate, date)
                .in(TenantSubscription::getStatus, statuses)
                .eq(TenantSubscription::getDeleted, false);
        return selectList(wrapper);
    }

    /**
     * 查找试用期即将到期的订阅
     */
    @Select("SELECT * FROM tenant_subscription WHERE in_trial = 1 " +
            "AND trial_end_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    List<TenantSubscription> findTrialExpiringSoon(@Param("startDate") LocalDateTime startDate,
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
    List<TenantSubscription> findSubscriptionsExpiringBetween(@Param("startDate") LocalDateTime startDate,
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

    /**
     * 统计时间段内新增订阅数（按 start_date）
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE start_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    long countNewSubscriptions(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    /**
     * 统计时间段内取消订阅数（按 cancelled_at）
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE cancelled_at BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    long countCancelledSubscriptions(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 统计当前各计划的订阅数量
     */
    @Select("SELECT plan_id as planId, COUNT(*) as cnt FROM tenant_subscription WHERE deleted = 0 GROUP BY plan_id")
    List<java.util.Map<String, Object>> countSubscriptionsByPlan();

    /**
     * 统计时间段内从 TRIAL 转为 ACTIVE 的订阅数（试用转化）
     * 简化：以 upgraded_at 在区间内且当前状态为 ACTIVE 作为转化
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE upgraded_at BETWEEN #{startDate} AND #{endDate} AND status = 'ACTIVE' AND deleted = 0")
    long countTrialConverted(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);

    /**
     * 统计时间段内处于 TRIAL 状态的订阅快照（简化）
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE status = 'TRIAL' AND deleted = 0")
    long countTrialSnapshot();


    /**
     * 计算某个时间点的活跃订阅数（期初活跃订阅数）
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE deleted = 0 AND status = 'ACTIVE' " +
            "AND start_date <= #{asOf} AND (cancelled_at IS NULL OR cancelled_at > #{asOf})")
    long countActiveAtDate(@Param("asOf") LocalDateTime asOf);

    /**
     * 统计时间段内处于试用曝光的订阅数量（简化口径）
     * 包含：区间内处于试用状态的订阅 或 区间内完成升级的订阅
     */
    @Select("SELECT COUNT(*) FROM tenant_subscription WHERE deleted = 0 AND (" +
            "(in_trial = 1 AND trial_end_date >= #{startDate} AND start_date <= #{endDate}) " +
            "OR (upgraded_at BETWEEN #{startDate} AND #{endDate})" +
            ")")
    long countTrialExposedDuring(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);


    List<java.util.Map<String, Object>> getSubscriptionStatsByPlan();
}
