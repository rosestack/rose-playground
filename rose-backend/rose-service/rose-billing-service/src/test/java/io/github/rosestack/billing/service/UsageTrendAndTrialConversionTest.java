package io.github.rosestack.billing.service;

import io.github.rosestack.billing.enums.SubscriptionStatus;
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
class UsageTrendAndTrialConversionTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private FinancialReportService service;

    @Test
    void testUsageTrendAndTrialConversion() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 3, 23, 59);

        when(usageRecordRepository.sumDailyUsage(eq(start), eq(end)))
                .thenReturn(List.of(
                        Map.of("recordDate", "2025-04-01", "dailyUsage", new BigDecimal("10")),
                        Map.of("recordDate", "2025-04-02", "dailyUsage", new BigDecimal("20"))
                ));

        when(subscriptionRepository.countTrialConverted(any(), any())).thenReturn(4L);
        when(subscriptionRepository.countTrialExposedDuring(any(), any())).thenReturn(20L);

        var report = service.generateUsageReport(start, end);
        assertEquals(new BigDecimal("10"), report.getUsageTrend().get("2025-04-01"));

        // 仪表盘上的试用转化率来自 generateDashboardData（最近30天），这里只测试计算方法通过 generateDashboardData 的返回
        when(invoiceRepository.sumPaidAmountByPeriod(any(), any())).thenReturn(BigDecimal.ZERO);
        when(invoiceRepository.sumBaseAmountByPeriod(any(), any())).thenReturn(BigDecimal.ZERO); // 防止 MRR 为空
        when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(0L);
        var dashboard = service.generateDashboardData();
        assertNotNull(dashboard.getTrialConversionRate());
    }
}

