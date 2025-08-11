package io.github.rosestack.billing.service;

import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialReportServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private FinancialReportService financialReportService;

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2025, 1, 1, 0, 0);
        end = LocalDateTime.of(2025, 1, 31, 23, 59);
    }

    @Test
    void testCalculateAverageOrderValue() {
        when(invoiceRepository.sumPaidAmountByPeriod(eq(start), eq(end)))
                .thenReturn(new BigDecimal("1000"));
        when(invoiceRepository.countPaidInvoicesByPeriod(eq(start), eq(end)))
                .thenReturn(10L);

        // 通过 generateRevenueReport 间接触发 AOV 计算
        var report = financialReportService.generateRevenueReport(start, end, "DAILY");
        assertEquals(new BigDecimal("100.00"), report.getAverageOrderValue());
    }

    @Test
    void testCalculateMRR() {
        // 由于 calculateMRR 使用当前月份范围，直接验证返回值是仓储计算值
        when(invoiceRepository.sumBaseAmountByPeriod(any(), any()))
                .thenReturn(new BigDecimal("2500.00"));
        var dashboard = financialReportService.generateDashboardData();
        assertEquals(new BigDecimal("2500.00"), dashboard.getMonthlyRecurringRevenue());
    }

    @Test
    void testRevenueByPeriodAggregation() {
        // 模拟每日统计数据，三天
        when(invoiceRepository.getRevenueStatsByPeriod(eq(start), eq(end)))
                .thenReturn(List.of(
                        Map.of("paymentDate", "2025-01-01", "dailyRevenue", new BigDecimal("100")),
                        Map.of("paymentDate", "2025-01-02", "dailyRevenue", new BigDecimal("200")),
                        Map.of("paymentDate", "2025-01-03", "dailyRevenue", new BigDecimal("300"))
                ));
        var report = financialReportService.generateRevenueReport(start, end, "MONTHLY");
        // 期望聚合为一个 key: 2025-01，总额 600
        assertEquals(new BigDecimal("600"), report.getRevenueByPeriod().get("2025-01"));
    }
}

