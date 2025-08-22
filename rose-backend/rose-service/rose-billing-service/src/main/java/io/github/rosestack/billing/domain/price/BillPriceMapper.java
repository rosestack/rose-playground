package io.github.rosestack.billing.domain.price;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.PriceType;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定价Mapper接口
 *
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillPriceMapper extends BaseMapper<BillPrice> {

    /**
     * 查找生效的定价规则（优先租户专属，其次标准定价）
     * 
     * @param tenantId 租户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param billingCycle 计费周期
     * @return 定价规则
     */
    default BillPrice findEffectivePrice(Long tenantId, String targetType, Long targetId, BillingCycle billingCycle) {
        LocalDateTime now = LocalDateTime.now();
        
        // 首先查找租户专属定价
        BillPrice tenantPrice = findTenantSpecificPrice(tenantId, targetType, targetId, billingCycle, now);
        if (tenantPrice != null) {
            return tenantPrice;
        }
        
        // 如果没有租户专属定价，查找标准定价
        return findStandardPrice(targetType, targetId, billingCycle, now);
    }

    /**
     * 查找租户专属定价
     */
    default BillPrice findTenantSpecificPrice(Long tenantId, String targetType, Long targetId, BillingCycle billingCycle, LocalDateTime effectiveTime) {
        PriceType priceType = "PLAN".equals(targetType) ? PriceType.TENANT_PLAN : PriceType.TENANT_FEATURE;
        
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getType, priceType)
                .eq(BillPrice::getTargetType, targetType)
                .eq(BillPrice::getTargetId, targetId)
                .eq(BillPrice::getTenantId, tenantId)
                .eq(BillPrice::getBillingCycle, billingCycle)
                .eq(BillPrice::getStatus, "ACTIVE")
                .le(BillPrice::getEffectiveTime, effectiveTime)
                .and(wrapper -> wrapper.isNull(BillPrice::getExpireTime).or().gt(BillPrice::getExpireTime, effectiveTime))
                .orderByDesc(BillPrice::getEffectiveTime)
                .last("LIMIT 1");
        
        return selectOne(queryWrapper);
    }

    /**
     * 查找标准定价
     */
    default BillPrice findStandardPrice(String targetType, Long targetId, BillingCycle billingCycle, LocalDateTime effectiveTime) {
        PriceType priceType = "PLAN".equals(targetType) ? PriceType.PLAN : PriceType.FEATURE;
        
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getType, priceType)
                .eq(BillPrice::getTargetType, targetType)
                .eq(BillPrice::getTargetId, targetId)
                .isNull(BillPrice::getTenantId)
                .eq(BillPrice::getBillingCycle, billingCycle)
                .eq(BillPrice::getStatus, "ACTIVE")
                .le(BillPrice::getEffectiveTime, effectiveTime)
                .and(wrapper -> wrapper.isNull(BillPrice::getExpireTime).or().gt(BillPrice::getExpireTime, effectiveTime))
                .orderByDesc(BillPrice::getEffectiveTime)
                .last("LIMIT 1");
        
        return selectOne(queryWrapper);
    }

    /**
     * 根据目标查找所有定价规则
     */
    default List<BillPrice> findByTarget(String targetType, Long targetId) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getTargetType, targetType)
                .eq(BillPrice::getTargetId, targetId)
                .orderByDesc(BillPrice::getEffectiveTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据租户查找专属定价规则
     */
    default List<BillPrice> findTenantSpecificPrices(Long tenantId) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getTenantId, tenantId)
                .in(BillPrice::getType, PriceType.TENANT_PLAN, PriceType.TENANT_FEATURE)
                .eq(BillPrice::getStatus, "ACTIVE")
                .orderByDesc(BillPrice::getEffectiveTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找标准定价规则
     */
    default List<BillPrice> findStandardPrices() {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .isNull(BillPrice::getTenantId)
                .in(BillPrice::getType, PriceType.PLAN, PriceType.FEATURE)
                .eq(BillPrice::getStatus, "ACTIVE")
                .orderByDesc(BillPrice::getEffectiveTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据定价类型查找定价规则
     */
    default List<BillPrice> findByPriceType(PriceType priceType) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getType, priceType)
                .eq(BillPrice::getStatus, "ACTIVE")
                .orderByDesc(BillPrice::getEffectiveTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据计费周期查找定价规则
     */
    default List<BillPrice> findByBillingCycle(BillingCycle billingCycle) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getBillingCycle, billingCycle)
                .eq(BillPrice::getStatus, "ACTIVE")
                .orderByDesc(BillPrice::getEffectiveTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找即将过期的定价规则
     */
    default List<BillPrice> findExpiringPrices(LocalDateTime beforeTime) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getStatus, "ACTIVE")
                .isNotNull(BillPrice::getExpireTime)
                .le(BillPrice::getExpireTime, beforeTime)
                .orderByAsc(BillPrice::getExpireTime);
        return selectList(queryWrapper);
    }

    /**
     * 检查是否存在冲突的定价规则
     */
    default boolean existsConflictingPrice(BillPrice price) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getType, price.getType())
                .eq(BillPrice::getTargetType, price.getTargetType())
                .eq(BillPrice::getTargetId, price.getTargetId())
                .eq(BillPrice::getBillingCycle, price.getBillingCycle())
                .eq(BillPrice::getStatus, "ACTIVE");

        // 如果是租户专属定价，需要匹配租户ID
        if (price.getTenantId() != null) {
            queryWrapper.eq(BillPrice::getTenantId, price.getTenantId());
        } else {
            queryWrapper.isNull(BillPrice::getTenantId);
        }

        // 如果有ID，排除自己
        if (price.getId() != null) {
            queryWrapper.ne(BillPrice::getId, price.getId());
        }

        // 检查时间重叠
        if (price.getEffectiveTime() != null) {
            queryWrapper.and(wrapper -> 
                wrapper.isNull(BillPrice::getExpireTime)
                       .or()
                       .gt(BillPrice::getExpireTime, price.getEffectiveTime())
            );
        }

        if (price.getExpireTime() != null) {
            queryWrapper.le(BillPrice::getEffectiveTime, price.getExpireTime());
        }

        return selectCount(queryWrapper) > 0;
    }

    /**
     * 批量过期定价规则
     */
    default int expirePrices(List<Long> priceIds) {
        if (priceIds == null || priceIds.isEmpty()) {
            return 0;
        }

        BillPrice updateEntity = new BillPrice();
        updateEntity.setStatus("EXPIRED");
        updateEntity.setExpireTime(LocalDateTime.now());

        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .in(BillPrice::getId, priceIds);

        return update(updateEntity, queryWrapper);
    }

    /**
     * 根据目标删除定价规则
     */
    default int deleteByTarget(String targetType, Long targetId) {
        LambdaQueryWrapper<BillPrice> queryWrapper = new LambdaQueryWrapper<BillPrice>()
                .eq(BillPrice::getTargetType, targetType)
                .eq(BillPrice::getTargetId, targetId);
        return delete(queryWrapper);
    }
}