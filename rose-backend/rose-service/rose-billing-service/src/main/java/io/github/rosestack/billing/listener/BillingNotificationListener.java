package io.github.rosestack.billing.listener;

import io.github.rosestack.billing.event.PaymentSucceededEvent;
import io.github.rosestack.billing.service.BillingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingNotificationListener {

    private final BillingNotificationService notificationService;

    @EventListener
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        try {
            notificationService.sendPaymentConfirmation(event.getTenantId(), event.getInvoiceId());
        } catch (Exception e) {
            log.error("发送支付成功通知失败: tenantId={}, invoiceId={}", event.getTenantId(), event.getInvoiceId(), e);
        }
    }
}
