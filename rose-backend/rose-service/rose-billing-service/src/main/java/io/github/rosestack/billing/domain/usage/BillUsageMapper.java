package io.github.rosestack.billing.domain.usage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用量记录Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillUsageMapper extends BaseMapper<BillUsage> {
    
    /**
     * 根据订阅ID查找所有用量记录
     */
    default List<BillUsage> findBySubscriptionId(Long subscriptionId) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据功能ID查找用量记录
     */
    default List<BillUsage> findByFeatureId(Long featureId) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getFeatureId, featureId);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据订阅ID和功能ID查找用量记录
     */
    default List<BillUsage> findBySubscriptionAndFeature(Long subscriptionId, Long featureId) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId)
                .eq(BillUsage::getFeatureId, featureId);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据计费周期查找用量记录
     */
    default List<BillUsage> findByBillingPeriod(LocalDate billingPeriod) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getBillingPeriod, billingPeriod);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据订阅ID和计费周期查找用量记录
     */
    default List<BillUsage> findBySubscriptionAndPeriod(Long subscriptionId, LocalDate billingPeriod) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId)
                .eq(BillUsage::getBillingPeriod, billingPeriod);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据订阅ID、功能ID和计费周期查找用量记录
     */
    default List<BillUsage> findBySubscriptionFeatureAndPeriod(Long subscriptionId, Long featureId, LocalDate billingPeriod) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId)
                .eq(BillUsage::getFeatureId, featureId)
                .eq(BillUsage::getBillingPeriod, billingPeriod);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找指定时间范围内的用量记录
     */
    default List<BillUsage> findByUsageTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .ge(BillUsage::getUsageTime, startTime)
                .le(BillUsage::getUsageTime, endTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找指定订阅在时间范围内的用量记录
     */
    default List<BillUsage> findBySubscriptionAndTimeRange(Long subscriptionId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId)
                .ge(BillUsage::getUsageTime, startTime)
                .le(BillUsage::getUsageTime, endTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 计算指定订阅和功能在计费周期内的总用量
     */
    default BigDecimal sumUsageBySubscriptionFeatureAndPeriod(Long subscriptionId, Long featureId, LocalDate billingPeriod) {
        List<BillUsage> usages = findBySubscriptionFeatureAndPeriod(subscriptionId, featureId, billingPeriod);
        return usages.stream()
                .filter(usage -> usage.getUsageAmount() != null)
                .map(BillUsage::getUsageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 计算指定订阅在计费周期内的总用量（按功能分组）
     */
    default List<BillUsage> sumUsageBySubscriptionAndPeriodGroupByFeature(Long subscriptionId, LocalDate billingPeriod) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId)
                .eq(BillUsage::getBillingPeriod, billingPeriod)
                .groupBy(BillUsage::getFeatureId);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找当月的用量记录
     */
    default List<BillUsage> findCurrentMonthUsage() {
        LocalDate now = LocalDate.now();
        LocalDate currentMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        return findByBillingPeriod(currentMonth);
    }
    
    /**
     * 查找指定订阅当月的用量记录
     */
    default List<BillUsage> findCurrentMonthUsageBySubscription(Long subscriptionId) {
        LocalDate now = LocalDate.now();
        LocalDate currentMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        return findBySubscriptionAndPeriod(subscriptionId, currentMonth);
    }
    
    /**
     * 删除指定计费周期之前的历史用量记录
     */
    default void deleteUsageBeforePeriod(LocalDate billingPeriod) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .lt(BillUsage::getBillingPeriod, billingPeriod);
        delete(queryWrapper);
    }
    
    /**
     * 统计指定订阅的用量记录数量
     */
    default long countBySubscriptionId(Long subscriptionId) {
        LambdaQueryWrapper<BillUsage> queryWrapper = new LambdaQueryWrapper<BillUsage>()
                .eq(BillUsage::getSubscriptionId, subscriptionId);
        return selectCount(queryWrapper);
    }
}