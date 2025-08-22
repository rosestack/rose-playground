package io.github.rosestack.billing.domain.plan;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.PlanStatus;
import io.github.rosestack.billing.domain.enums.PlanType;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 套餐计划Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillPlanMapper extends BaseMapper<BillPlan> {
    
    /**
     * 检查套餐代码和版本组合是否存在
     */
    default boolean existsByCodeAndVersion(String code, String version) {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getCode, code)
                .eq(BillPlan::getVersion, version);
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 根据代码查找套餐（最新版本）
     */
    default BillPlan findLatestByCode(String code) {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getCode, code)
                .orderByDesc(BillPlan::getCreatedTime)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }
    
    /**
     * 根据代码和版本查找套餐
     */
    default BillPlan findByCodeAndVersion(String code, String version) {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getCode, code)
                .eq(BillPlan::getVersion, version);
        return selectOne(queryWrapper);
    }
    
    /**
     * 查找所有可用的套餐
     */
    default List<BillPlan> findAvailablePlans() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getStatus, PlanStatus.ACTIVE.name())
                .and(wrapper -> wrapper
                        .isNull(BillPlan::getEffectiveTime)
                        .or()
                        .le(BillPlan::getEffectiveTime, now)
                )
                .and(wrapper -> wrapper
                        .isNull(BillPlan::getExpireTime)
                        .or()
                        .gt(BillPlan::getExpireTime, now)
                );
        return selectList(queryWrapper);
    }
    
    /**
     * 根据套餐类型查找套餐
     */
    default List<BillPlan> findByPlanType(PlanType planType) {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getPlanType, planType);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找免费套餐
     */
    default List<BillPlan> findFreePlans() {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getPlanType, PlanType.FREE);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找付费套餐
     */
    default List<BillPlan> findPaidPlans() {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .ne(BillPlan::getPlanType, PlanType.FREE);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找支持试用的套餐
     */
    default List<BillPlan> findTrialEnabledPlans() {
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .eq(BillPlan::getTrialEnabled, true)
                .gt(BillPlan::getTrialDays, 0);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找即将过期的套餐
     */
    default List<BillPlan> findExpiringSoon(int days) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(days);
        LambdaQueryWrapper<BillPlan> queryWrapper = new LambdaQueryWrapper<BillPlan>()
                .isNotNull(BillPlan::getExpireTime)
                .le(BillPlan::getExpireTime, expireTime)
                .eq(BillPlan::getStatus, PlanStatus.ACTIVE.name());
        return selectList(queryWrapper);
    }
}
