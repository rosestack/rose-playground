package io.github.rosestack.billing.service;

import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import io.github.rosestack.billing.repository.UsageRecordRepository;
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
class FinancialReportServiceMoreTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private FinancialReportService svc;

    @Test
    void testRevenueByPlanAndGrowthRate() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 31, 23, 59);

        // 按计划收入
        when(invoiceRepository.getRevenueByPlan(eq(start), eq(end)))
                .thenReturn(List.of(
                        Map.of("planId", "plan-basic", "revenue", new BigDecimal("300"), "invoiceCount", 3),
                        Map.of("planId", "plan-pro", "revenue", new BigDecimal("700"), "invoiceCount", 7)
                ));

        // 当前区间与上一周期收入
        when(invoiceRepository.sumPaidAmountByPeriod(eq(start), eq(end)))
                .thenReturn(new BigDecimal("1000"));
        when(invoiceRepository.sumPaidAmountByPeriod(eq(start.minusDays(30)), eq(start)))
                .thenReturn(new BigDecimal("800"));

        var report = svc.generateRevenueReport(start, end, "MONTHLY");

        assertEquals(new BigDecimal("300"), report.getRevenueByPlan().get("plan-basic"));
        assertEquals(new BigDecimal("700"), report.getRevenueByPlan().get("plan-pro"));
        // 增长率 = (1000-800)/800 = 0.25
        assertEquals(0, new BigDecimal("0.2500").compareTo(report.getGrowthRate()));
    }
}

