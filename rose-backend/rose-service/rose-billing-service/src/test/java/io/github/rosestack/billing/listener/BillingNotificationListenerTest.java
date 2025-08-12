package io.github.rosestack.billing.listener;

import io.github.rosestack.billing.event.PaymentSucceededEvent;
import io.github.rosestack.billing.service.BillingNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BillingNotificationListenerTest {

    @Test
    void onPaymentSucceeded_callsNotificationService() {
        BillingNotificationService notificationService = mock(BillingNotificationService.class);
        BillingNotificationListener listener = new BillingNotificationListener(notificationService);

        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "tenant-1", "inv-1", "tx-1", "WECHAT", new BigDecimal("10.00"), LocalDateTime.now());

        listener.onPaymentSucceeded(event);

        ArgumentCaptor<String> tenantCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> invoiceCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationService, times(1)).sendPaymentConfirmation(tenantCaptor.capture(), invoiceCaptor.capture());

        assertEquals("tenant-1", tenantCaptor.getValue());
        assertEquals("inv-1", invoiceCaptor.getValue());
    }
}
