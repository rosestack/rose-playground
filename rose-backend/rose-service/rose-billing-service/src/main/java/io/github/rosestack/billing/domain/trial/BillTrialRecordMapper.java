package io.github.rosestack.billing.domain.trial;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.TrialStatus;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试用记录Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillTrialRecordMapper extends BaseMapper<BillTrialRecord> {
    
    /**
     * 根据租户ID和套餐ID查找试用记录
     */
    default List<BillTrialRecord> findByTenantAndPlan(String tenantId, Long planId) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getTenantId, tenantId)
                .eq(BillTrialRecord::getPlanId, planId)
                .orderByDesc(BillTrialRecord::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找租户的最新试用记录
     */
    default BillTrialRecord findLatestByTenantAndPlan(String tenantId, Long planId) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getTenantId, tenantId)
                .eq(BillTrialRecord::getPlanId, planId)
                .orderByDesc(BillTrialRecord::getCreatedTime)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }
    
    /**
     * 根据状态查找试用记录
     */
    default List<BillTrialRecord> findByStatus(TrialStatus status) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getStatus, status)
                .orderByDesc(BillTrialRecord::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找活跃的试用记录
     */
    default List<BillTrialRecord> findActiveTrials() {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getStatus, TrialStatus.ACTIVE)
                .orderByAsc(BillTrialRecord::getTrialEndTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找即将过期的试用记录
     */
    default List<BillTrialRecord> findTrialsExpiringSoon(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().plusDays(days);
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getStatus, TrialStatus.ACTIVE)
                .le(BillTrialRecord::getTrialEndTime, cutoffTime)
                .orderByAsc(BillTrialRecord::getTrialEndTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找已过期但状态未更新的试用记录
     */
    default List<BillTrialRecord> findExpiredTrialsNotUpdated() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getStatus, TrialStatus.ACTIVE)
                .lt(BillTrialRecord::getTrialEndTime, now)
                .orderByAsc(BillTrialRecord::getTrialEndTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据套餐ID查找所有试用记录
     */
    default List<BillTrialRecord> findByPlanId(Long planId) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getPlanId, planId)
                .orderByDesc(BillTrialRecord::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 统计租户对指定套餐的试用次数
     */
    default long countTrialsByTenantAndPlan(String tenantId, Long planId) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getTenantId, tenantId)
                .eq(BillTrialRecord::getPlanId, planId);
        return selectCount(queryWrapper);
    }
    
    /**
     * 检查租户是否有活跃的试用
     */
    default boolean hasActiveTrial(String tenantId, Long planId) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getTenantId, tenantId)
                .eq(BillTrialRecord::getPlanId, planId)
                .eq(BillTrialRecord::getStatus, TrialStatus.ACTIVE);
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 查找指定时间范围内创建的试用记录
     */
    default List<BillTrialRecord> findByCreatedTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .ge(BillTrialRecord::getCreatedTime, startTime)
                .le(BillTrialRecord::getCreatedTime, endTime)
                .orderByDesc(BillTrialRecord::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找转换率统计（已转换的试用记录）
     */
    default List<BillTrialRecord> findConvertedTrials() {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getStatus, TrialStatus.CONVERTED)
                .orderByDesc(BillTrialRecord::getConvertedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 统计指定状态的试用记录数量
     */
    default long countByStatus(TrialStatus status) {
        LambdaQueryWrapper<BillTrialRecord> queryWrapper = new LambdaQueryWrapper<BillTrialRecord>()
                .eq(BillTrialRecord::getStatus, status);
        return selectCount(queryWrapper);
    }
}