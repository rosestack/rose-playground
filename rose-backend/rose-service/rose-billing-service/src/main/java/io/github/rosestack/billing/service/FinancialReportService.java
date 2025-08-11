package io.github.rosestack.billing.service;

import io.github.rosestack.billing.dto.*;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

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
@ConditionalOnProperty(prefix = "rose.billing", name = "enabled", havingValue = "true", matchIfMissing = true)
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

        // 计算总收入
        BigDecimal totalRevenue = invoiceRepository.sumPaidAmountByPeriod(startDate, endDate);
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
        BigDecimal todayRevenue = invoiceRepository.sumPaidAmountByPeriod(
            now.toLocalDate().atStartOfDay(), now);
        dashboard.setTodayRevenue(todayRevenue);

        // 本月收入
        BigDecimal monthRevenue = invoiceRepository.sumPaidAmountByPeriod(
            now.toLocalDate().withDayOfMonth(1).atStartOfDay(), now);
        dashboard.setMonthRevenue(monthRevenue);

        // 总活跃订阅
        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        dashboard.setActiveSubscriptions(activeSubscriptions);

        // 试用转化率（最近30天）
        dashboard.setTrialConversionRate(calculateTrialConversionRate(thirtyDaysAgo, now));

        // 月度经常性收入 (MRR)
        dashboard.setMonthlyRecurringRevenue(calculateMRR());

        // 年度经常性收入 (ARR)
        dashboard.setAnnualRecurringRevenue(dashboard.getMonthlyRecurringRevenue().multiply(new BigDecimal("12")));

        return dashboard;
    }

    /**
     * 导出报表到Excel
     */
    public void exportReportToExcel(LocalDateTime startDate, LocalDateTime endDate,
                                   String reportType, java.io.OutputStream outputStream) {
        try {
            log.info("导出财务报表到Excel：{} - {}，类型：{}", startDate, endDate, reportType);

            // TODO: 实现Excel导出功能
            // 可以使用Apache POI库来生成Excel文件
            // 这里提供一个基础的实现框架

            ComprehensiveFinancialReport report = generateComprehensiveReport(startDate, endDate);

            // 创建Excel工作簿
            // Workbook workbook = new XSSFWorkbook();
            //
            // // 创建收入报表工作表
            // Sheet revenueSheet = workbook.createSheet("收入报表");
            // createRevenueSheet(revenueSheet, report.getRevenueReport());
            //
            // // 创建订阅报表工作表
            // Sheet subscriptionSheet = workbook.createSheet("订阅报表");
            // createSubscriptionSheet(subscriptionSheet, report.getSubscriptionReport());
            //
            // // 创建使用量报表工作表
            // Sheet usageSheet = workbook.createSheet("使用量报表");
            // createUsageSheet(usageSheet, report.getUsageReport());
            //
            // // 写入输出流
            // workbook.write(outputStream);
            // workbook.close();

            // 临时实现：写入简单的CSV格式数据
            String csvContent = generateCsvReport(report);
            outputStream.write(csvContent.getBytes("UTF-8"));
            outputStream.flush();

            log.info("Excel报表导出完成");

        } catch (Exception e) {
            log.error("导出Excel报表失败", e);
            throw new RuntimeException("导出Excel报表失败", e);
        }
    }

    /**
     * 生成CSV格式报表（临时实现）
     */
    private String generateCsvReport(ComprehensiveFinancialReport report) {
        StringBuilder csv = new StringBuilder();

        // CSV头部
        csv.append("报表类型,开始日期,结束日期,生成时间\n");
        csv.append("综合财务报表,")
           .append(report.getStartDate())
           .append(",")
           .append(report.getEndDate())
           .append(",")
           .append(report.getGeneratedAt())
           .append("\n\n");

        // 收入数据
        if (report.getRevenueReport() != null) {
            csv.append("收入报表\n");
            csv.append("总收入,").append(report.getRevenueReport().getTotalRevenue()).append("\n");
            csv.append("增长率,").append(report.getRevenueReport().getGrowthRate()).append("\n");
            csv.append("平均订单价值,").append(report.getRevenueReport().getAverageOrderValue()).append("\n\n");
        }

        // 订阅数据
        if (report.getSubscriptionReport() != null) {
            csv.append("订阅报表\n");
            csv.append("总订阅数,").append(report.getSubscriptionReport().getTotalSubscriptions()).append("\n");
            csv.append("活跃订阅数,").append(report.getSubscriptionReport().getActiveSubscriptions()).append("\n");
            csv.append("试用订阅数,").append(report.getSubscriptionReport().getTrialSubscriptions()).append("\n");
            csv.append("流失率,").append(report.getSubscriptionReport().getChurnRate()).append("\n\n");
        }

        return csv.toString();
    }

    // 私有辅助方法
    private Map<String, BigDecimal> calculateRevenueByPeriod(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        // TODO: 实现按时间维度的收入统计
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        switch (reportType.toUpperCase()) {
            case "DAILY":
                // 按天统计
                break;
            case "WEEKLY":
                // 按周统计
                break;
            case "MONTHLY":
                // 按月统计
                break;
            default:
                // 默认按天统计
                break;
        }

        return result;
    }

    private Map<String, BigDecimal> calculateRevenueByPlan(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现按订阅计划的收入统计
        return new HashMap<>();
    }

    private List<TenantRevenueData> calculateTopTenantsByRevenue(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        // TODO: 实现Top租户收入统计
        return new ArrayList<>();
    }

    private BigDecimal calculateGrowthRate(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        // TODO: 实现增长率计算
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现平均订单价值计算
        return BigDecimal.ZERO;
    }

    private Map<String, Long> calculateSubscriptionsByPlan() {
        // TODO: 实现按计划统计订阅数
        return new HashMap<>();
    }

    private BigDecimal calculateChurnRate(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现流失率计算
        return BigDecimal.ZERO;
    }

    private long calculateNewSubscriptions(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现新增订阅统计
        return 0;
    }

    private Map<String, BigDecimal> calculateUsageByType(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("计算使用量统计: {} - {}", startDate, endDate);

        try {
            // 聚合所有租户的使用量统计
            Map<String, BigDecimal> result = new HashMap<>();

            // 这里简化实现，实际应该查询所有租户的数据
            // 由于没有全局统计方法，返回空结果
            log.warn("使用量统计功能需要进一步实现全局查询方法");

            return result;
        } catch (Exception e) {
            log.error("计算使用量统计失败", e);
            return new HashMap<>();
        }
    }

    private List<TenantUsageData> calculateTopTenantsByUsage(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        // TODO: 实现Top租户使用量统计
        return new ArrayList<>();
    }

    private Map<String, BigDecimal> calculateUsageTrend(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现使用量趋势计算
        return new HashMap<>();
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
        // TODO: 实现试用转化率计算
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateMRR() {
        // TODO: 实现月度经常性收入计算
        return BigDecimal.ZERO;
    }
}
