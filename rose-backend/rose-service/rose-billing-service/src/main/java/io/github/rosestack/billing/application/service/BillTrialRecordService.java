package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.TrialStatus;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.billing.domain.trial.BillTrialRecord;
import io.github.rosestack.billing.domain.trial.BillTrialRecordMapper;
import io.github.rosestack.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试用记录管理服务
 *
 * 提供试用记录的创建、状态管理、转换跟踪等核心业务功能
 * 支持试用资格检查、转换率统计、试用期提醒等企业级功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillTrialRecordService {

    private final BillTrialRecordMapper trialRecordMapper;
    private final BillPlanMapper planMapper;

    /**
     * 开始试用
     */
    @Transactional(rollbackFor = Exception.class)
    public BillTrialRecord startTrial(String tenantId, Long planId) {
        log.info("Starting trial for tenant: {}, plan: {}", tenantId, planId);

        // 验证套餐是否存在且支持试用
        BillPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("套餐不存在: " + planId);
        }
        if (!plan.getTrialEnabled()) {
            throw new BusinessException("套餐不支持试用: " + plan.getName());
        }

        // 检查试用资格
        if (!checkTrialEligibility(tenantId, planId)) {
            throw new BusinessException("不符合试用资格，可能已达到试用次数上限或存在活跃试用");
        }

        // 创建试用记录
        BillTrialRecord trialRecord = BillTrialRecord.createTrialRecord(
                tenantId, planId, plan.getTrialDays());

        trialRecordMapper.insert(trialRecord);
        log.info("Trial started successfully: id={}, tenant={}, plan={}",
                trialRecord.getId(), tenantId, planId);

        return trialRecord;
    }

    /**
     * 检查试用资格
     */
    public boolean checkTrialEligibility(String tenantId, Long planId) {
        // 检查是否有活跃的试用
        if (trialRecordMapper.hasActiveTrial(tenantId, planId)) {
            log.warn("Tenant {} already has active trial for plan {}", tenantId, planId);
            return false;
        }

        // 获取套餐配置
        BillPlan plan = planMapper.selectById(planId);
        if (plan == null || !plan.getTrialEnabled()) {
            return false;
        }

        // 检查试用次数限制
        long trialCount = trialRecordMapper.countTrialsByTenantAndPlan(tenantId, planId);
        if (trialCount >= plan.getTrialLimitPerUser()) {
            log.warn("Tenant {} has reached trial limit {} for plan {}",
                    tenantId, plan.getTrialLimitPerUser(), planId);
            return false;
        }

        return true;
    }

    /**
     * 试用转为付费
     */
    @Transactional(rollbackFor = Exception.class)
    public BillTrialRecord convertTrialToPaid(String tenantId, Long planId) {
        log.info("Converting trial to paid for tenant: {}, plan: {}", tenantId, planId);

        BillTrialRecord latestTrial = trialRecordMapper.findLatestByTenantAndPlan(tenantId, planId);
        if (latestTrial == null) {
            throw new BusinessException("未找到试用记录");
        }

        if (!latestTrial.isActive()) {
            throw new BusinessException("试用记录不是活跃状态: " + latestTrial.getStatus());
        }

        latestTrial.convertToPaid();
        trialRecordMapper.updateById(latestTrial);

        log.info("Trial converted to paid successfully: id={}", latestTrial.getId());
        return latestTrial;
    }

    /**
     * 取消试用
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTrial(String tenantId, Long planId, String reason) {
        log.info("Cancelling trial for tenant: {}, plan: {}, reason: {}", tenantId, planId, reason);

        BillTrialRecord latestTrial = trialRecordMapper.findLatestByTenantAndPlan(tenantId, planId);
        if (latestTrial == null) {
            throw new BusinessException("未找到试用记录");
        }

        if (!latestTrial.isActive()) {
            throw new BusinessException("试用记录不是活跃状态: " + latestTrial.getStatus());
        }

        latestTrial.cancel(reason);
        trialRecordMapper.updateById(latestTrial);

        log.info("Trial cancelled successfully: id={}", latestTrial.getId());
    }

    /**
     * 处理过期试用
     */
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredTrials() {
        log.info("Processing expired trials");

        List<BillTrialRecord> expiredTrials = trialRecordMapper.findExpiredTrialsNotUpdated();
        for (BillTrialRecord trial : expiredTrials) {
            trial.markExpired();
            trialRecordMapper.updateById(trial);
            log.info("Trial marked as expired: id={}, tenant={}, plan={}",
                    trial.getId(), trial.getTenantId(), trial.getPlanId());
        }

        log.info("Processed {} expired trials", expiredTrials.size());
    }

    /**
     * 获取试用统计信息
     */
    public TrialStatistics getTrialStatistics() {
        long activeCount = trialRecordMapper.countByStatus(TrialStatus.ACTIVE);
        long expiredCount = trialRecordMapper.countByStatus(TrialStatus.EXPIRED);
        long convertedCount = trialRecordMapper.countByStatus(TrialStatus.CONVERTED);
        long cancelledCount = trialRecordMapper.countByStatus(TrialStatus.CANCELLED);

        // 计算转换率
        long totalTrials = activeCount + expiredCount + convertedCount + cancelledCount;
        double conversionRate = totalTrials > 0 ? (double) convertedCount / totalTrials * 100 : 0.0;

        return new TrialStatistics(activeCount, expiredCount, convertedCount,
                cancelledCount, totalTrials, conversionRate);
    }

    /**
     * 获取即将过期的试用记录
     */
    public List<BillTrialRecord> getTrialsExpiringSoon(int days) {
        return trialRecordMapper.findTrialsExpiringSoon(days);
    }

    /**
     * 根据套餐获取试用统计
     */
    public Map<Long, Long> getTrialCountsByPlan() {
        List<BillTrialRecord> allTrials = trialRecordMapper.selectList(null);
        return allTrials.stream()
                .collect(Collectors.groupingBy(
                        BillTrialRecord::getPlanId,
                        Collectors.counting()
                ));
    }

    /**
     * 获取转换率分析
     */
    public List<ConversionAnalysis> getConversionAnalysis() {
        List<BillTrialRecord> convertedTrials = trialRecordMapper.findConvertedTrials();

        Map<Long, List<BillTrialRecord>> trialsByPlan = convertedTrials.stream()
                .collect(Collectors.groupingBy(BillTrialRecord::getPlanId));

        return trialsByPlan.entrySet().stream()
                .map(entry -> {
                    Long planId = entry.getKey();
                    List<BillTrialRecord> trials = entry.getValue();

                    long totalTrials = trialRecordMapper.countTrialsByTenantAndPlan("", planId);
                    int convertedTrialSize = trials.size();
                    double conversionRate = totalTrials > 0 ?
                            (double) convertedTrialSize / totalTrials * 100 : 0.0;

                    // 计算平均转换时间
                    double avgConversionDays = trials.stream()
                            .filter(trial -> trial.getConvertedTime() != null && trial.getTrialStartTime() != null)
                            .mapToLong(trial -> java.time.Duration.between(
                                    trial.getTrialStartTime(), trial.getConvertedTime()).toDays())
                            .average()
                            .orElse(0.0);

                    return new ConversionAnalysis(planId, totalTrials, convertedTrialSize,
                            conversionRate, avgConversionDays);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查找试用记录
     */
    public BillTrialRecord findById(Long id) {
        return trialRecordMapper.selectById(id);
    }

    /**
     * 根据租户和套餐查找试用记录
     */
    public List<BillTrialRecord> findByTenantAndPlan(String tenantId, Long planId) {
        return trialRecordMapper.findByTenantAndPlan(tenantId, planId);
    }

    /**
     * 获取最新试用记录
     */
    public BillTrialRecord getLatestTrialRecord(String tenantId, Long planId) {
        return trialRecordMapper.findLatestByTenantAndPlan(tenantId, planId);
    }

    /**
     * 试用统计信息
     */
    public static class TrialStatistics {
        private final long activeCount;
        private final long expiredCount;
        private final long convertedCount;
        private final long cancelledCount;
        private final long totalTrials;
        private final double conversionRate;

        public TrialStatistics(long activeCount, long expiredCount, long convertedCount,
                              long cancelledCount, long totalTrials, double conversionRate) {
            this.activeCount = activeCount;
            this.expiredCount = expiredCount;
            this.convertedCount = convertedCount;
            this.cancelledCount = cancelledCount;
            this.totalTrials = totalTrials;
            this.conversionRate = conversionRate;
        }

        // Getters
        public long getActiveCount() { return activeCount; }
        public long getExpiredCount() { return expiredCount; }
        public long getConvertedCount() { return convertedCount; }
        public long getCancelledCount() { return cancelledCount; }
        public long getTotalTrials() { return totalTrials; }
        public double getConversionRate() { return conversionRate; }
    }

    /**
     * 转换分析
     */
    public static class ConversionAnalysis {
        private final Long planId;
        private final long totalTrials;
        private final long convertedTrials;
        private final double conversionRate;
        private final double avgConversionDays;

        public ConversionAnalysis(Long planId, long totalTrials, long convertedTrials,
                                 double conversionRate, double avgConversionDays) {
            this.planId = planId;
            this.totalTrials = totalTrials;
            this.convertedTrials = convertedTrials;
            this.conversionRate = conversionRate;
            this.avgConversionDays = avgConversionDays;
        }

        // Getters
        public Long getPlanId() { return planId; }
        public long getTotalTrials() { return totalTrials; }
        public long getConvertedTrials() { return convertedTrials; }
        public double getConversionRate() { return conversionRate; }
        public double getAvgConversionDays() { return avgConversionDays; }
    }
}
