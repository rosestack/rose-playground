package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;


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
    default BigDecimal sumUsageByTenantAndMetricAndPeriod(
            String tenantId,
            String metricType,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("COALESCE(SUM(quantity), 0) AS total")
          .eq("tenant_id", tenantId)
          .eq("metric_type", metricType)
          .between("record_time", startTime, endTime);
        java.util.List<java.util.Map<String, Object>> list = selectMaps(qw);
        if (list.isEmpty() || list.get(0) == null) return java.math.BigDecimal.ZERO;
        Object v = list.get(0).get("total");
        return v == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(v.toString());
    }

    /**
     * 查找租户的使用量记录
     */
    default List<UsageRecord> findByTenantIdAndMetricTypeOrderByRecordTimeDesc(
            String tenantId, String metricType) {
        LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageRecord::getTenantId, tenantId)
                .eq(UsageRecord::getMetricType, metricType)
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
                .between(UsageRecord::getRecordTime, startTime, endTime);
        return selectList(wrapper);
    }

    /**
     * 标记使用量为已计费
     */
    default int markAsBilled(String tenantId,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              String invoiceId,
                              LocalDateTime billedAt) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UsageRecord> uw = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        uw.set("billed", 1)
          .set("billed_at", billedAt)
          .set("invoice_id", invoiceId)
          .eq("tenant_id", tenantId)
          .between("record_time", startTime, endTime)
          .eq("billed", 0);
        return update(null, uw);
    }

    /**
     * 删除过期的使用量记录
     */
    default int deleteOldBilledRecords(LocalDateTime cutoffDate) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UsageRecord> uw = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        uw.set("deleted", 1)
          .lt("record_time", cutoffDate)
          .eq("billed", 1);
        return update(null, uw);
    }

    /**
     * 获取租户当月使用量统计
     */
    default List<java.util.Map<String, Object>> getMonthlyUsageStats(String tenantId, LocalDateTime monthStart) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("metric_type as metricType", "SUM(quantity) as totalQuantity")
          .eq("tenant_id", tenantId)
          .ge("record_time", monthStart)
          .groupBy("metric_type");
        return selectMaps(qw);
    }

    /**
     * 获取租户使用量趋势统计
     */
    default List<java.util.Map<String, Object>> getUsageTrendStats(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("DATE(record_time) as recordDate",
                "metric_type as metricType",
                "SUM(quantity) as dailyQuantity")
          .eq("tenant_id", tenantId)
          .between("record_time", startDate, endDate)
          .groupBy("DATE(record_time)", "metric_type")
          .orderByAsc("recordDate", "metricType");
        return selectMaps(qw);
    }

    /**
     * 获取租户当前使用量汇总
     */
    default List<java.util.Map<String, Object>> getCurrentPeriodUsageSummary(String tenantId, LocalDateTime periodStart) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("metric_type as metricType",
                "COUNT(*) as recordCount",
                "SUM(quantity) as totalQuantity",
                "AVG(quantity) as avgQuantity",
                "MAX(quantity) as maxQuantity")
          .eq("tenant_id", tenantId)
          .ge("record_time", periodStart)
          .groupBy("metric_type");
        return selectMaps(qw);
    }

    /**
     * 获取时间段内各计量类型的全量使用量汇总
     */
    default List<java.util.Map<String, Object>> sumUsageByType(LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("metric_type as metricType", "COALESCE(SUM(quantity),0) as totalQuantity")
          .between("record_time", startDate, endDate)
          .groupBy("metric_type");
        return selectMaps(qw);
    }

    /**
     * 获取时间段内租户使用量 Top N（按总量）
     */
    default List<java.util.Map<String, Object>> getTopTenantsByUsage(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("tenant_id as tenantId", "COALESCE(SUM(quantity),0) as totalUsage")
          .between("record_time", startDate, endDate)
          .groupBy("tenant_id")
          .orderByDesc("totalUsage")
          .last("LIMIT " + limit);
        return selectMaps(qw);
    }

    /**
     * 获取时间段内的全局每日使用量趋势
     */
    default List<java.util.Map<String, Object>> sumDailyUsage(LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UsageRecord> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("DATE(record_time) as recordDate", "COALESCE(SUM(quantity),0) as dailyUsage")
          .between("record_time", startDate, endDate)
          .groupBy("DATE(record_time)")
          .orderByAsc("recordDate");
        return selectMaps(qw);
    }


}
