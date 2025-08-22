package io.github.rosestack.billing.domain.feature;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.FeatureType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 计费功能Mapper接口
 *
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillFeatureMapper extends BaseMapper<BillFeature> {

    /**
     * 根据租户ID和功能代码查找功能
     */
    default BillFeature findByTenantAndCode(Long tenantId, String code) {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .eq(BillFeature::getTenantId, tenantId)
                .eq(BillFeature::getCode, code)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }

    /**
     * 根据功能代码查找系统级功能（租户ID=0）
     */
    default BillFeature findSystemFeatureByCode(String code) {
        return findByTenantAndCode(0L, code);
    }

    /**
     * 根据租户ID和功能类型查找功能列表
     */
    default List<BillFeature> findByTenantAndType(Long tenantId, FeatureType type) {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .eq(BillFeature::getTenantId, tenantId)
                .eq(BillFeature::getType, type);
        return selectList(queryWrapper);
    }

    /**
     * 查找租户的所有激活功能
     */
    default List<BillFeature> findEnabledFeaturesByTenant(Long tenantId) {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .eq(BillFeature::getTenantId, tenantId)
                .eq(BillFeature::getStatus, "ACTIVE")
                .orderByAsc(BillFeature::getCode);
        return selectList(queryWrapper);
    }

    /**
     * 查找所有系统级激活功能
     */
    default List<BillFeature> findEnabledSystemFeatures() {
        return findEnabledFeaturesByTenant(0L);
    }

    /**
     * 检查租户下功能代码是否存在
     */
    default boolean existsByTenantAndCode(Long tenantId, String code) {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .eq(BillFeature::getTenantId, tenantId)
                .eq(BillFeature::getCode, code);
        return selectCount(queryWrapper) > 0;
    }

    /**
     * 根据重置周期查找功能列表
     */
    default List<BillFeature> findByResetPeriod(String resetPeriod) {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .eq(BillFeature::getResetPeriod, resetPeriod)
                .eq(BillFeature::getStatus, "ACTIVE");
        return selectList(queryWrapper);
    }

    /**
     * 根据功能范围查找功能列表
     */
    default List<BillFeature> findByValueScope(String valueScope) {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .eq(BillFeature::getValueScope, valueScope)
                .eq(BillFeature::getStatus, "ACTIVE");
        return selectList(queryWrapper);
    }

    /**
     * 获取需要重置的功能列表（排除NEVER类型）
     */
    default List<BillFeature> findFeaturesNeedingReset() {
        LambdaQueryWrapper<BillFeature> queryWrapper = new LambdaQueryWrapper<BillFeature>()
                .ne(BillFeature::getResetPeriod, "NEVER")
                .eq(BillFeature::getStatus, "ACTIVE");
        return selectList(queryWrapper);
    }
}
