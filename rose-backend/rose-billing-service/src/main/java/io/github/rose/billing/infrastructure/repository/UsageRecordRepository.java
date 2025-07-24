package io.github.rose.billing.infrastructure.repository;

import io.github.rose.billing.domain.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用量记录数据访问接口
 *
 * @author rose
 */
@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, String> {

    /**
     * 统计指定时间段内的使用量
     */
    @Query("SELECT COALESCE(SUM(u.quantity), 0) FROM UsageRecord u " +
           "WHERE u.tenantId = :tenantId AND u.metricType = :metricType " +
           "AND u.recordTime BETWEEN :startTime AND :endTime")
    BigDecimal sumUsageByTenantAndMetricAndPeriod(
        String tenantId, String metricType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找租户的使用量记录
     */
    List<UsageRecord> findByTenantIdAndMetricTypeOrderByRecordTimeDesc(
        String tenantId, String metricType);

    /**
     * 查找未计费的使用量记录
     */
    List<UsageRecord> findByTenantIdAndBilledFalseAndRecordTimeBetween(
        String tenantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 标记使用量为已计费
     */
    @Modifying
    @Query("UPDATE UsageRecord u SET u.billed = true, u.billedAt = :billedAt, u.invoiceId = :invoiceId " +
           "WHERE u.tenantId = :tenantId AND u.recordTime BETWEEN :startTime AND :endTime AND u.billed = false")
    int markAsBilled(String tenantId, LocalDateTime startTime, LocalDateTime endTime,
                     String invoiceId, LocalDateTime billedAt);

    /**
     * 删除过期的使用量记录
     */
    @Modifying
    @Query("DELETE FROM UsageRecord u WHERE u.recordTime < :cutoffDate AND u.billed = true")
    int deleteOldBilledRecords(LocalDateTime cutoffDate);

    /**
     * 获取租户当月使用量统计
     */
    @Query("SELECT u.metricType, SUM(u.quantity) FROM UsageRecord u " +
           "WHERE u.tenantId = :tenantId AND u.recordTime >= :monthStart " +
           "GROUP BY u.metricType")
    List<Object[]> getMonthlyUsageStats(String tenantId, LocalDateTime monthStart);
}
