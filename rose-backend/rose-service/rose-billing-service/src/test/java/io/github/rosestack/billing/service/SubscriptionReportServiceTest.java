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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionReportServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private FinancialReportService service;

    @Test
    void testSubscriptionsStats() {
        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 2, 28, 23, 59);

        when(subscriptionRepository.countSubscriptionsByPlan())
                .thenReturn(List.of(
                        Map.of("planId", "basic", "cnt", 5),
                        Map.of("planId", "pro", "cnt", 10)
                ));
        when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(12L);
        when(subscriptionRepository.countCancelledSubscriptions(eq(start), eq(end))).thenReturn(3L);
        when(subscriptionRepository.countActiveAtDate(eq(start))).thenReturn(12L);

        when(subscriptionRepository.countNewSubscriptions(eq(start), eq(end))).thenReturn(7L);

        var report = service.generateSubscriptionReport(start, end);
        assertEquals(2, report.getSubscriptionsByPlan().size());
        assertEquals(new BigDecimal("0.2500"), report.getChurnRate());
        assertEquals(7L, report.getNewSubscriptions());
    }
}

