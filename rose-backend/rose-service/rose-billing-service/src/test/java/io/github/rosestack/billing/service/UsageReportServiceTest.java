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
class UsageReportServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private FinancialReportService service;

    @Test
    void testUsageReport() {
        LocalDateTime start = LocalDateTime.of(2025, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 3, 31, 23, 59);

        when(usageRecordRepository.sumUsageByType(eq(start), eq(end)))
                .thenReturn(List.of(
                        Map.of("metricType", "API_CALLS", "totalQuantity", new BigDecimal("1000")),
                        Map.of("metricType", "STORAGE", "totalQuantity", new BigDecimal("50"))
                ));
        when(usageRecordRepository.getTopTenantsByUsage(eq(start), eq(end), eq(10)))
                .thenReturn(List.of(
                        Map.of("tenantId", "t1", "totalUsage", new BigDecimal("600")),
                        Map.of("tenantId", "t2", "totalUsage", new BigDecimal("400"))
                ));

        var report = service.generateUsageReport(start, end);
        assertEquals(new BigDecimal("1000"), report.getUsageByType().get("API_CALLS"));
        assertEquals(2, report.getTopTenantsByUsage().size());
        assertEquals(new BigDecimal("600"), report.getTopTenantsByUsage().get(0).getTotalUsage());
    }
}

