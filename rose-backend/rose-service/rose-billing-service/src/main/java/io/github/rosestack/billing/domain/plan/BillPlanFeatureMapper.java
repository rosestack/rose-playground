package io.github.rosestack.billing.domain.plan;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.PlanFeatureStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 套餐功能关联Mapper接口
 *
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillPlanFeatureMapper extends BaseMapper<BillPlanFeature> {

    /**
     * 根据套餐ID查找所有功能配置
     */
    default List<BillPlanFeature> findByPlanId(Long planId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId)
                .orderByAsc(BillPlanFeature::getFeatureId);
        return selectList(queryWrapper);
    }

    /**
     * 根据套餐ID查找所有激活的功能配置
     */
    default List<BillPlanFeature> findActiveByPlanId(Long planId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId)
                .eq(BillPlanFeature::getStatus, PlanFeatureStatus.ACTIVE)
                .orderByAsc(BillPlanFeature::getFeatureId);
        return selectList(queryWrapper);
    }

    /**
     * 根据功能ID查找所有套餐配置
     */
    default List<BillPlanFeature> findByFeatureId(Long featureId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getFeatureId, featureId)
                .eq(BillPlanFeature::getStatus, PlanFeatureStatus.ACTIVE)
                .orderByAsc(BillPlanFeature::getPlanId);
        return selectList(queryWrapper);
    }

    /**
     * 根据套餐ID和功能ID查找特定配置
     */
    default BillPlanFeature findByPlanAndFeature(Long planId, Long featureId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId)
                .eq(BillPlanFeature::getFeatureId, featureId)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }

    /**
     * 检查套餐和功能关联是否存在
     */
    default boolean existsByPlanAndFeature(Long planId, Long featureId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId)
                .eq(BillPlanFeature::getFeatureId, featureId);
        return selectCount(queryWrapper) > 0;
    }

    /**
     * 根据套餐ID和功能值查找配置
     * 用于查找具有特定功能值的套餐配置
     */
    default List<BillPlanFeature> findByPlanAndFeatureValue(Long planId, String featureValue) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId)
                .eq(BillPlanFeature::getFeatureValue, featureValue)
                .eq(BillPlanFeature::getStatus, PlanFeatureStatus.ACTIVE);
        return selectList(queryWrapper);
    }

    /**
     * 查找启用状态的开关功能
     * 用于查找套餐中启用的开关类型功能
     */
    default List<BillPlanFeature> findEnabledSwitchFeatures(Long planId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId)
                .eq(BillPlanFeature::getFeatureValue, "enabled")
                .eq(BillPlanFeature::getStatus, PlanFeatureStatus.ACTIVE);
        return selectList(queryWrapper);
    }

    /**
     * 根据套餐ID批量删除功能配置
     */
    default int deleteByPlanId(Long planId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId);
        return delete(queryWrapper);
    }

    /**
     * 根据功能ID批量删除套餐配置
     */
    default int deleteByFeatureId(Long featureId) {
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getFeatureId, featureId);
        return delete(queryWrapper);
    }

    /**
     * 批量禁用套餐的功能配置
     */
    default int deactivateByPlanId(Long planId) {
        BillPlanFeature updateEntity = new BillPlanFeature();
        updateEntity.setStatus(PlanFeatureStatus.INACTIVE);
        
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId);
        return update(updateEntity, queryWrapper);
    }

    /**
     * 批量激活套餐的功能配置
     */
    default int activateByPlanId(Long planId) {
        BillPlanFeature updateEntity = new BillPlanFeature();
        updateEntity.setStatus(PlanFeatureStatus.ACTIVE);
        
        LambdaQueryWrapper<BillPlanFeature> queryWrapper = new LambdaQueryWrapper<BillPlanFeature>()
                .eq(BillPlanFeature::getPlanId, planId);
        return update(updateEntity, queryWrapper);
    }
}