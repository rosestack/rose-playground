package io.github.rosestack.billing.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rosestack.billing.entity.UsageRecord;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 使用量管理服务
 *
 * @author rose
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageService extends ServiceImpl<UsageRecordRepository, UsageRecord> {

    private final UsageRecordRepository usageRepository;

    /** 记录使用量 */
    @Transactional
    public void recordUsage(String tenantId, String metricType, BigDecimal quantity, String description) {
        UsageRecord record = new UsageRecord();
        record.setId(UUID.randomUUID().toString());
        record.setTenantId(tenantId);
        record.setMetricType(metricType);
        record.setQuantity(quantity);
        record.setDescription(description);
        record.setRecordTime(LocalDateTime.now());
        record.setBilled(false);

        usageRepository.insert(record);
        log.debug("记录使用量: 租户={}, 类型={}, 数量={}", tenantId, metricType, quantity);
    }

    /** 记录使用量（带订阅ID） */
    @Transactional
    public void recordUsage(
            String tenantId, String subscriptionId, String metricType, BigDecimal quantity, String description) {
        UsageRecord record = new UsageRecord();
        record.setId(UUID.randomUUID().toString());
        record.setTenantId(tenantId);
        record.setSubscriptionId(subscriptionId);
        record.setMetricType(metricType);
        record.setQuantity(quantity);
        record.setDescription(description);
        record.setRecordTime(LocalDateTime.now());
        record.setBilled(false);
        usageRepository.insert(record);
        log.debug("记录使用量(含订阅): 租户={}, 订阅={}, 类型={}, 数量={}", tenantId, subscriptionId, metricType, quantity);
    }

    /**
     * 批量记录使用量
     *
     * @param records 使用量记录列表，不能为空
     * @throws IllegalArgumentException 当记录列表为空或包含无效记录时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordUsageBatch(List<UsageRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("使用量记录列表不能为空");
        }

        // 性能优化：预先设置默认值，避免在循环中重复操作
        LocalDateTime now = LocalDateTime.now();

        for (UsageRecord record : records) {
            // 数据验证
            if (record.getTenantId() == null || record.getTenantId().trim().isEmpty()) {
                throw new IllegalArgumentException("租户ID不能为空");
            }
            if (record.getMetricType() == null || record.getMetricType().trim().isEmpty()) {
                throw new IllegalArgumentException("计量类型不能为空");
            }
            if (record.getQuantity() == null || record.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("使用量数值不能为空或负数");
            }

            // 设置默认值
            if (record.getId() == null) {
                record.setId(UUID.randomUUID().toString());
            }
            if (record.getRecordTime() == null) {
                record.setRecordTime(now);
            }
            if (record.getBilled() == null) {
                record.setBilled(false);
            }
        }

        // 使用批量插入提高性能
        saveBatch(records, 1000); // 每批1000条记录
        log.info("批量记录使用量: {} 条记录", records.size());
    }

    /** 获取租户指定时间段的使用量 */
    public BigDecimal getTenantUsage(
            String tenantId, String metricType, LocalDateTime startTime, LocalDateTime endTime) {
        return usageRepository.sumUsageByTenantAndMetricAndPeriod(tenantId, metricType, startTime, endTime);
    }

    /** 获取租户当月使用量统计 */
    public List<Map<String, Object>> getMonthlyUsageStats(String tenantId) {
        LocalDateTime monthStart =
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return usageRepository.getMonthlyUsageStats(tenantId, monthStart);
    }

    /** 获取租户使用量趋势 */
    public List<Map<String, Object>> getUsageTrend(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return usageRepository.getUsageTrendStats(tenantId, startDate, endDate);
    }

    /** 获取当前计费周期的使用量汇总 */
    public List<Map<String, Object>> getCurrentPeriodUsageSummary(String tenantId) {
        LocalDateTime periodStart =
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return usageRepository.getCurrentPeriodUsageSummary(tenantId, periodStart);
    }

    /** 获取未计费的使用量记录 */
    public List<UsageRecord> getUnbilledUsage(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        return usageRepository.findByTenantIdAndBilledFalseAndRecordTimeBetween(tenantId, startTime, endTime);
    }

    /** 标记使用量为已计费 */
    @Transactional
    public int markUsageAsBilled(String tenantId, LocalDateTime startTime, LocalDateTime endTime, String invoiceId) {
        int count = usageRepository.markAsBilled(tenantId, startTime, endTime, invoiceId, LocalDateTime.now());
        log.info("标记使用量为已计费: 租户={}, 时间段={} to {}, 账单={}, 记录数={}", tenantId, startTime, endTime, invoiceId, count);
        return count;
    }

    /** 清理过期的已计费使用量记录 */
    @Transactional
    public int cleanupOldBilledRecords(LocalDateTime cutoffDate) {
        int count = usageRepository.deleteOldBilledRecords(cutoffDate);
        log.info("清理过期使用量记录: 截止日期={}, 清理数量={}", cutoffDate, count);
        return count;
    }

    /** 获取租户的使用量历史 */
    public List<UsageRecord> getUsageHistory(String tenantId, String metricType, int limit) {
        List<UsageRecord> records =
                usageRepository.findByTenantIdAndMetricTypeOrderByRecordTimeDesc(tenantId, metricType);
        return records.size() > limit ? records.subList(0, limit) : records;
    }

    /** 聚合使用量数据 */
    public Map<String, BigDecimal> aggregateUsageByMetric(
            String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> stats = usageRepository.getUsageTrendStats(tenantId, startTime, endTime);
        return stats.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        stat -> (String) stat.get("metricType"),
                        java.util.stream.Collectors.reducing(
                                BigDecimal.ZERO, stat -> (BigDecimal) stat.get("dailyQuantity"), BigDecimal::add)));
    }

    /** 检查使用量是否异常 */
    public boolean isUsageAnomalous(String tenantId, String metricType, BigDecimal currentUsage) {
        // 获取历史平均使用量
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonth = now.minusMonths(1);
        BigDecimal historicalUsage = getTenantUsage(tenantId, metricType, lastMonth, now);

        // 如果当前使用量超过历史平均的3倍，认为异常
        BigDecimal threshold = historicalUsage.multiply(BigDecimal.valueOf(3));
        return currentUsage.compareTo(threshold) > 0;
    }
}
