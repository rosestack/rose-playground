package io.github.rosestack.billing.service;

import io.github.rosestack.billing.dto.*;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 财务报表服务
 *
 * @author rose
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final InvoiceRepository invoiceRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final UsageRecordRepository usageRepository;

    /**
     * 生成收入报表
     */
    public RevenueReport generateRevenueReport(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        log.info("生成收入报表：{} - {}，类型：{}", startDate, endDate, reportType);

        RevenueReport report = new RevenueReport();
        report.setReportType(reportType);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        // 计算总收入（空值兜底为0）
        BigDecimal totalRevenue = Optional.ofNullable(
                invoiceRepository.sumPaidAmountByPeriod(startDate, endDate)
        ).orElse(BigDecimal.ZERO);
        report.setTotalRevenue(totalRevenue);

        // 按时间维度统计收入
        report.setRevenueByPeriod(calculateRevenueByPeriod(startDate, endDate, reportType));

        // 按订阅计划统计收入
        report.setRevenueByPlan(calculateRevenueByPlan(startDate, endDate));

        // 按租户统计收入（Top 10）
        report.setTopTenantsByRevenue(calculateTopTenantsByRevenue(startDate, endDate, 10));

        // 计算增长率
        report.setGrowthRate(calculateGrowthRate(startDate, endDate, reportType));

        // 计算平均订单价值
        report.setAverageOrderValue(calculateAverageOrderValue(startDate, endDate));

        log.info("收入报表生成完成，总收入：{}", totalRevenue);
        return report;
    }

    /**
     * 生成订阅报表
     */
    public SubscriptionReport generateSubscriptionReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("生成订阅报表：{} - {}", startDate, endDate);

        SubscriptionReport report = new SubscriptionReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        // 总订阅数
        long totalSubscriptions = subscriptionRepository.selectCount(null);
        report.setTotalSubscriptions(totalSubscriptions);

        // 活跃订阅数
        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        report.setActiveSubscriptions(activeSubscriptions);

        // 试用中订阅数
        long trialSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.TRIAL);
        report.setTrialSubscriptions(trialSubscriptions);

        // 已取消订阅数
        long cancelledSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        report.setCancelledSubscriptions(cancelledSubscriptions);

        // 按计划统计订阅数
        report.setSubscriptionsByPlan(calculateSubscriptionsByPlan());

        // 计算流失率
        report.setChurnRate(calculateChurnRate(startDate, endDate));

        // 计算新增订阅
        report.setNewSubscriptions(calculateNewSubscriptions(startDate, endDate));

        log.info("订阅报表生成完成，总订阅：{}, 活跃：{}", totalSubscriptions, activeSubscriptions);
        return report;
    }

    /**
     * 生成使用量报表
     */
    public UsageReport generateUsageReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("生成使用量报表：{} - {}", startDate, endDate);

        UsageReport report = new UsageReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        // 按类型统计使用量
        report.setUsageByType(calculateUsageByType(startDate, endDate));

        // 按租户统计使用量（Top 10）
        report.setTopTenantsByUsage(calculateTopTenantsByUsage(startDate, endDate, 10));

        // 使用量趋势
        report.setUsageTrend(calculateUsageTrend(startDate, endDate));

        log.info("使用量报表生成完成");
        return report;
    }

    /**
     * 生成综合财务报表
     */
    public ComprehensiveFinancialReport generateComprehensiveReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("生成综合财务报表：{} - {}", startDate, endDate);

        ComprehensiveFinancialReport report = new ComprehensiveFinancialReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        // 收入报表
        report.setRevenueReport(generateRevenueReport(startDate, endDate, "COMPREHENSIVE"));

        // 订阅报表
        report.setSubscriptionReport(generateSubscriptionReport(startDate, endDate));

        // 使用量报表
        report.setUsageReport(generateUsageReport(startDate, endDate));

        // 关键财务指标
        report.setKeyMetrics(calculateKeyMetrics(startDate, endDate));

        log.info("综合财务报表生成完成");
        return report;
    }

    /**
     * 生成实时仪表板数据
     */
    public DashboardData generateDashboardData() {
        DashboardData dashboard = new DashboardData();
        dashboard.setGeneratedAt(LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        // 今日收入
        BigDecimal todayRevenue = Optional.ofNullable(
            invoiceRepository.sumPaidAmountByPeriod(now.toLocalDate().atStartOfDay(), now)
        ).orElse(BigDecimal.ZERO);
        dashboard.setTodayRevenue(todayRevenue);

        // 本月收入
        BigDecimal monthRevenue = Optional.ofNullable(
            invoiceRepository.sumPaidAmountByPeriod(now.toLocalDate().withDayOfMonth(1).atStartOfDay(), now)
        ).orElse(BigDecimal.ZERO);
        dashboard.setMonthRevenue(monthRevenue);

        // 总活跃订阅
        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        dashboard.setActiveSubscriptions(activeSubscriptions);

        // 试用转化率（最近30天）
        dashboard.setTrialConversionRate(calculateTrialConversionRate(thirtyDaysAgo, now));

        // 月度经常性收入 (MRR)
        BigDecimal mrr = Optional.ofNullable(calculateMRR()).orElse(BigDecimal.ZERO);
        dashboard.setMonthlyRecurringRevenue(mrr);

        // 年度经常性收入 (ARR)
        dashboard.setAnnualRecurringRevenue(mrr.multiply(new BigDecimal("12")));

        return dashboard;
    }



    // 私有辅助方法
    private Map<String, BigDecimal> calculateRevenueByPeriod(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        // 复用按天统计的底层数据，再进行聚合
        List<Map<String, Object>> dailyStats = invoiceRepository.getRevenueStatsByPeriod(startDate, endDate);
        if (dailyStats == null) return result;

        switch (reportType == null ? "DAILY" : reportType.toUpperCase()) {
            case "MONTHLY": {
                // key: yyyy-MM
                for (Map<String, Object> row : dailyStats) {
                    Object dateObj = row.get("paymentDate");
                    Object valObj = row.get("dailyRevenue");
                    if (dateObj == null || valObj == null) continue;
                    String monthKey = dateObj.toString().substring(0, 7);
                    BigDecimal val = toBigDecimal(valObj);
                    result.merge(monthKey, val, BigDecimal::add);
                }
                break;
            }
            case "WEEKLY": {
                // key: yyyy-ww（简单处理：按 ISO 周序号聚合，若格式不可用则退化为按7日块）
                for (Map<String, Object> row : dailyStats) {
                    Object dateObj = row.get("paymentDate");
                    Object valObj = row.get("dailyRevenue");
                    if (dateObj == null || valObj == null) continue;
                    String dateStr = dateObj.toString();
                    String weekKey = toWeekKey(dateStr); // 例: 2025-W32
                    BigDecimal val = toBigDecimal(valObj);
                    result.merge(weekKey, val, BigDecimal::add);
                }
                break;
            }
            case "DAILY":
            default: {
                for (Map<String, Object> row : dailyStats) {
                    Object dateObj = row.get("paymentDate");
                    Object valObj = row.get("dailyRevenue");
                    if (dateObj == null || valObj == null) continue;
                    result.put(dateObj.toString(), toBigDecimal(valObj));
                }
                break;
            }
        }
        return result;
    }

    private Map<String, BigDecimal> calculateRevenueByPlan(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        List<Map<String, Object>> rows = invoiceRepository.getRevenueByPlan(startDate, endDate);
        if (rows == null) return map;
        for (Map<String, Object> r : rows) {
            String planId = Objects.toString(r.get("planId"), null);
            BigDecimal revenue = toBigDecimal(r.get("revenue"));
            if (planId != null) map.put(planId, revenue);
        }
        return map;
    }

    private List<TenantRevenueData> calculateTopTenantsByRevenue(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Map<String, Object>> rows = invoiceRepository.getTopTenantsByRevenue(startDate, endDate, limit);
        List<TenantRevenueData> list = new ArrayList<>();
        if (rows == null) return list;
        for (Map<String, Object> row : rows) {
            TenantRevenueData d = new TenantRevenueData();
            d.setTenantId(Objects.toString(row.get("tenantId"), null));
            d.setRevenue(toBigDecimal(row.get("revenue")));
            Object cnt = row.get("invoiceCount");
            d.setInvoiceCount(cnt == null ? 0L : Long.parseLong(cnt.toString()));
            d.setAverageInvoiceValue(toBigDecimal(row.get("averageInvoiceValue")));
            list.add(d);
        }
        return list;
    }

    private BigDecimal calculateGrowthRate(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        // 简化：与上一同等时长周期对比（支持 DAILY/WEEKLY/MONTHLY 任意类型，均以时间跨度为准）
        long days = java.time.Duration.between(startDate, endDate).toDays();
        if (days <= 0) return BigDecimal.ZERO;
        LocalDateTime prevEnd = startDate;
        LocalDateTime prevStart = startDate.minusDays(days);
        BigDecimal current = invoiceRepository.sumPaidAmountByPeriod(startDate, endDate);
        BigDecimal previous = invoiceRepository.sumPaidAmountByPeriod(prevStart, prevEnd);
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current == null || current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : new BigDecimal("1.00");
        }
        return current.subtract(previous)
                .divide(previous, 4, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = invoiceRepository.sumPaidAmountByPeriod(startDate, endDate);
        long count = invoiceRepository.countPaidInvoicesByPeriod(startDate, endDate);
        if (count == 0) return BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    private Map<String, Long> calculateSubscriptionsByPlan() {
        Map<String, Long> map = new LinkedHashMap<>();
        List<Map<String, Object>> rows = subscriptionRepository.countSubscriptionsByPlan();
        if (rows == null) return map;
        for (Map<String, Object> r : rows) {
            String planId = Objects.toString(r.get("planId"), null);
            Object cnt = r.get("cnt");
            if (planId != null) map.put(planId, cnt == null ? 0L : Long.parseLong(cnt.toString()));
        }
        return map;
    }

    private BigDecimal calculateChurnRate(LocalDateTime startDate, LocalDateTime endDate) {
        // churn = 取消数 / 期初活跃订阅数（更精确：按期初活跃订阅数计算）
        long cancelled = subscriptionRepository.countCancelledSubscriptions(startDate, endDate);
        long activeAtStart = subscriptionRepository.countActiveAtDate(startDate);
        if (activeAtStart == 0) return BigDecimal.ZERO;
        return new BigDecimal(cancelled).divide(new BigDecimal(activeAtStart), 4, java.math.RoundingMode.HALF_UP);
    }

    private long calculateNewSubscriptions(LocalDateTime startDate, LocalDateTime endDate) {
        return subscriptionRepository.countNewSubscriptions(startDate, endDate);
    }

    private Map<String, BigDecimal> calculateUsageByType(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("计算使用量统计: {} - {}", startDate, endDate);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> rows = usageRepository.sumUsageByType(startDate, endDate);
            if (rows != null) {
                for (Map<String, Object> r : rows) {
                    String metric = Objects.toString(r.get("metricType"), null);
                    BigDecimal qty = toBigDecimal(r.get("totalQuantity"));
                    if (metric != null) result.put(metric, qty);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("计算使用量统计失败", e);
            return result;
        }
    }

    private List<TenantUsageData> calculateTopTenantsByUsage(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<TenantUsageData> list = new ArrayList<>();
        List<Map<String, Object>> rows = usageRepository.getTopTenantsByUsage(startDate, endDate, limit);
        if (rows == null) return list;
        for (Map<String, Object> r : rows) {
            TenantUsageData d = new TenantUsageData();
            d.setTenantId(Objects.toString(r.get("tenantId"), null));
            d.setTotalUsage(toBigDecimal(r.get("totalUsage")));
            list.add(d);
        }
        return list;
    }

    private Map<String, BigDecimal> calculateUsageTrend(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        List<Map<String, Object>> rows = usageRepository.sumDailyUsage(startDate, endDate);
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                String day = Objects.toString(r.get("recordDate"), null);
                BigDecimal qty = toBigDecimal(r.get("dailyUsage"));
                if (day != null) trend.put(day, qty);
            }
        }
        return trend;
    }

    private FinancialKeyMetrics calculateKeyMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        FinancialKeyMetrics metrics = new FinancialKeyMetrics();
        // 客户获取成本 (CAC)
        metrics.setCustomerAcquisitionCost(BigDecimal.ZERO);
        // 客户生命周期价值 (LTV)
        metrics.setCustomerLifetimeValue(BigDecimal.ZERO);
        // LTV/CAC 比率
        metrics.setLtvToCacRatio(BigDecimal.ZERO);
        // 净收入留存率 (NRR)
        metrics.setNetRevenueRetention(BigDecimal.ZERO);
        return metrics;
    }

    private BigDecimal calculateTrialConversionRate(LocalDateTime startDate, LocalDateTime endDate) {
        long converted = subscriptionRepository.countTrialConverted(startDate, endDate);
        long exposed = subscriptionRepository.countTrialExposedDuring(startDate, endDate);
        if (exposed == 0) return BigDecimal.ZERO;
        return new BigDecimal(converted).divide(new BigDecimal(exposed), 4, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMRR() {
        // MRR 近似：统计最近一个计费月内所有已支付账单的 base_amount 之和（周期性收入部分）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        return invoiceRepository.sumBaseAmountByPeriod(monthStart, now);
    }

    // 安全转换工具方法
    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    // 简单周Key计算：yyyy-Www（ISO 周序号）
    private String toWeekKey(String isoDate) {
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(isoDate);
            java.time.temporal.WeekFields wf = java.time.temporal.WeekFields.ISO;
            int week = d.get(wf.weekOfWeekBasedYear());
            int year = d.get(wf.weekBasedYear());
            return String.format("%d-W%02d", year, week);
        } catch (Exception e) {
            return isoDate;
        }
    }
}
