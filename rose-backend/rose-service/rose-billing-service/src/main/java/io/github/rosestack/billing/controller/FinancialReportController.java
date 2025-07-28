package io.github.rosestack.billing.controller;

import io.github.rosestack.billing.dto.*;
import io.github.rosestack.billing.service.FinancialReportService;
import io.github.rosestack.core.model.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 财务报表控制器
 *
 * @author rose
 */
@Slf4j
@RestController
@RequestMapping("/api/billing/reports")
@RequiredArgsConstructor
public class FinancialReportController {

    private final FinancialReportService reportService;

    /**
     * 获取实时仪表板数据
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardData> getDashboardData() {
        DashboardData dashboard = reportService.generateDashboardData();
        return ApiResponse.success(dashboard);
    }

    /**
     * 生成收入报表
     */
    @GetMapping("/revenue")
    public ApiResponse<RevenueReport> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "MONTHLY") String reportType) {

        RevenueReport report = reportService.generateRevenueReport(startDate, endDate, reportType);
        return ApiResponse.success(report);
    }

    /**
     * 生成订阅报表
     */
    @GetMapping("/subscriptions")
    public ApiResponse<SubscriptionReport> getSubscriptionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SubscriptionReport report = reportService.generateSubscriptionReport(startDate, endDate);
        return ApiResponse.success(report);
    }

    /**
     * 生成使用量报表
     */
    @GetMapping("/usage")
    public ApiResponse<UsageReport> getUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        UsageReport report = reportService.generateUsageReport(startDate, endDate);
        return ApiResponse.success(report);
    }

    /**
     * 生成综合财务报表
     */
    @GetMapping("/comprehensive")
    public ApiResponse<ComprehensiveFinancialReport> getComprehensiveReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        ComprehensiveFinancialReport report = reportService.generateComprehensiveReport(startDate, endDate);
        return ApiResponse.success(report);
    }

    /**
     * 导出财务报表（Excel格式）
     */
    @GetMapping("/export")
    public void exportReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "COMPREHENSIVE") String reportType,
            HttpServletResponse response) {

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=financial_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            // 生成并写入Excel文件
            reportService.exportReportToExcel(startDate, endDate, reportType, response.getOutputStream());

        } catch (Exception e) {
            log.error("导出财务报表失败", e);
            throw new RuntimeException("导出财务报表失败", e);
        }
    }
}
