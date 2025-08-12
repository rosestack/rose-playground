package io.github.rosestack.billing.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.entity.TenantSubscription;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.event.PaymentSucceededEvent;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.SubscriptionPlanRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class BillingServiceProcessPaymentTest {

    private SubscriptionPlanRepository planRepository;
    private TenantSubscriptionRepository subscriptionRepository;
    private InvoiceRepository invoiceRepository;
    private UsageRecordRepository usageRepository;
    private PricingCalculator pricingCalculator;
    private BillingNotificationService notificationService;
    private ApplicationEventPublisher eventPublisher;
    private SubscriptionService subscriptionService;
    private UsageService usageService;
    private InvoiceService invoiceService;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        planRepository = mock(SubscriptionPlanRepository.class);
        subscriptionRepository = mock(TenantSubscriptionRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);
        usageRepository = mock(UsageRecordRepository.class);
        pricingCalculator = mock(PricingCalculator.class);
        notificationService = mock(BillingNotificationService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        subscriptionService = mock(SubscriptionService.class);
        usageService = mock(UsageService.class);
        invoiceService = mock(InvoiceService.class);

        billingService = new BillingService(
                planRepository,
                subscriptionRepository,
                invoiceRepository,
                usageRepository,
                pricingCalculator,
                notificationService,
                eventPublisher,
                subscriptionService,
                usageService,
                invoiceService);
    }

    @Test
    void processPayment_happyPath_sendsNotificationAndPublishesEvent() {
        String invoiceId = "inv-1";
        String tenantId = "tenant-1";
        String subId = "sub-1";
        String txId = "tx-1";
        String method = "WECHAT";

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setTenantId(tenantId);
        invoice.setSubscriptionId(subId);
        invoice.setPeriodStart(LocalDate.now().minusDays(30));
        invoice.setPeriodEnd(LocalDate.now());
        invoice.setTotalAmount(new BigDecimal("10.00"));

        when(invoiceService.getInvoiceDetails(invoiceId)).thenReturn(invoice);

        TenantSubscription sub = new TenantSubscription();
        sub.setId(subId);
        sub.setTenantId(tenantId);
        sub.setStatus(SubscriptionStatus.PENDING_PAYMENT);
        when(subscriptionRepository.selectById(subId)).thenReturn(sub);

        billingService.processPayment(invoiceId, method, txId);

        // 校验账单已支付
        verify(invoiceService).markInvoiceAsPaid(invoiceId, method, txId);
        // 校验使用量标记为已计费
        verify(usageService)
                .markUsageAsBilled(eq(tenantId), any(LocalDateTime.class), any(LocalDateTime.class), eq(invoiceId));
        // 校验同步通知
        verify(notificationService).sendPaymentConfirmation(eq(tenantId), eq(invoice));
        // 校验事件发布
        ArgumentCaptor<PaymentSucceededEvent> captor = ArgumentCaptor.forClass(PaymentSucceededEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
    }
}
