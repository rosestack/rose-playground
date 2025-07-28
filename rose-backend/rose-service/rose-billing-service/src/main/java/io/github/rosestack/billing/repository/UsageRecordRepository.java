package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用量记录数据访问接口
 *
 * @author rose
 */
@Mapper
public interface UsageRecordRepository extends BaseMapper<UsageRecord> {

    /**
     * 统计指定时间段内的使用量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM usage_record " +
            "WHERE tenant_id = #{tenantId} AND metric_type = #{metricType} " +
            "AND record_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0")
    BigDecimal sumUsageByTenantAndMetricAndPeriod(
            @Param("tenantId") String tenantId,
            @Param("metricType") String metricType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查找租户的使用量记录
     */
    default List<UsageRecord> findByTenantIdAndMetricTypeOrderByRecordTimeDesc(
            String tenantId, String metricType) {
        LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageRecord::getTenantId, tenantId)
                .eq(UsageRecord::getMetricType, metricType)
                .eq(UsageRecord::getDeleted, false)
                .orderByDesc(UsageRecord::getRecordTime);
        return selectList(wrapper);
    }

    /**
     * 查找未计费的使用量记录
     */
    default List<UsageRecord> findByTenantIdAndBilledFalseAndRecordTimeBetween(
            String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageRecord::getTenantId, tenantId)
                .eq(UsageRecord::getBilled, false)
                .between(UsageRecord::getRecordTime, startTime, endTime)
                .eq(UsageRecord::getDeleted, false);
        return selectList(wrapper);
    }

    /**
     * 标记使用量为已计费
     */
    @Update("UPDATE usage_record SET billed = 1, billed_at = #{billedAt}, invoice_id = #{invoiceId} " +
            "WHERE tenant_id = #{tenantId} AND record_time BETWEEN #{startTime} AND #{endTime} " +
            "AND billed = 0 AND deleted = 0")
    int markAsBilled(@Param("tenantId") String tenantId,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime,
                     @Param("invoiceId") String invoiceId,
                     @Param("billedAt") LocalDateTime billedAt);

    /**
     * 删除过期的使用量记录
     */
    @Update("UPDATE usage_record SET deleted = 1 WHERE record_time < #{cutoffDate} AND billed = 1")
    int deleteOldBilledRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 获取租户当月使用量统计
     */
    @Select("SELECT metric_type as metricType, SUM(quantity) as totalQuantity FROM usage_record " +
            "WHERE tenant_id = #{tenantId} AND record_time >= #{monthStart} AND deleted = 0 " +
            "GROUP BY metric_type")
    List<java.util.Map<String, Object>> getMonthlyUsageStats(@Param("tenantId") String tenantId,
                                                            @Param("monthStart") LocalDateTime monthStart);

    /**
     * 获取租户使用量趋势统计
     */
    @Select("SELECT " +
            "DATE(record_time) as recordDate, " +
            "metric_type as metricType, " +
            "SUM(quantity) as dailyQuantity " +
            "FROM usage_record " +
            "WHERE tenant_id = #{tenantId} AND record_time BETWEEN #{startDate} AND #{endDate} AND deleted = 0 " +
            "GROUP BY DATE(record_time), metric_type " +
            "ORDER BY recordDate, metricType")
    List<java.util.Map<String, Object>> getUsageTrendStats(@Param("tenantId") String tenantId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 获取租户当前使用量汇总
     */
    @Select("SELECT " +
            "metric_type as metricType, " +
            "COUNT(*) as recordCount, " +
            "SUM(quantity) as totalQuantity, " +
            "AVG(quantity) as avgQuantity, " +
            "MAX(quantity) as maxQuantity " +
            "FROM usage_record " +
            "WHERE tenant_id = #{tenantId} AND record_time >= #{periodStart} AND deleted = 0 " +
            "GROUP BY metric_type")
    List<java.util.Map<String, Object>> getCurrentPeriodUsageSummary(@Param("tenantId") String tenantId,
                                                                    @Param("periodStart") LocalDateTime periodStart);
}
