package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.SubscriptionPlan;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订阅计划数据访问接口
 *
 * @author rose
 */
@Mapper
public interface SubscriptionPlanRepository extends BaseMapper<SubscriptionPlan> {

    /**
     * 根据代码查找计划
     */
    default Optional<SubscriptionPlan> findByCode(String code) {
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubscriptionPlan::getCode, code);
        SubscriptionPlan plan = selectOne(wrapper);
        return Optional.ofNullable(plan);
    }

    /**
     * 查找启用的计划
     */
    default List<SubscriptionPlan> findByEnabledTrueOrderByBasePrice() {
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubscriptionPlan::getEnabled, true).orderByAsc(SubscriptionPlan::getBasePrice);
        return selectList(wrapper);
    }

    /**
     * 根据租户查找计划
     */
    default List<SubscriptionPlan> findByTenantIdOrTenantIdIsNull(String tenantId) {
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(SubscriptionPlan::getTenantId, tenantId).or().isNull(SubscriptionPlan::getTenantId));
        return selectList(wrapper);
    }

    /**
     * 查找有效期内的计划
     */
    @Select("SELECT * FROM subscription_plan WHERE enabled = 1 "
            + "AND (effective_time IS NULL OR effective_time <= #{now}) "
            + "AND (expiry_time IS NULL OR expiry_time > #{now}) "
            + "AND deleted = 0")
    List<SubscriptionPlan> findValidPlans(@Param("now") LocalDateTime now);
}
