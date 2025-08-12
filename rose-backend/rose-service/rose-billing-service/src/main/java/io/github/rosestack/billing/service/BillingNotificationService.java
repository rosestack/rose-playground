package io.github.rosestack.billing.service;

import io.github.rosestack.billing.config.NotificationProperties;
import io.github.rosestack.billing.notification.BillingNotificationClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 计费通知服务（对接 notification-service）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingNotificationService {

    private final BillingNotificationClient notificationClient;
    private final NotificationProperties notificationProperties;

    private String resolveTargetEmail(String tenantId) {
        if (notificationProperties.getFallbackEmail() != null
                && !notificationProperties.getFallbackEmail().isEmpty()) {
            return notificationProperties.getFallbackEmail();
        }
        return tenantId + "@" + notificationProperties.getEmailDomain();
    }

    public void sendSubscriptionConfirmation(String tenantId, Object subscription) {
        String target = resolveTargetEmail(tenantId);
        notificationClient.send(target, "EMAIL", "BILLING_SUBSCRIPTION_CONFIRMED", Map.of("tenantId", tenantId));
        log.info("发送订阅确认通知: tenantId={}, target={}", tenantId, target);
    }

    public void sendInvoiceGenerated(String tenantId, Object invoice) {
        String target = resolveTargetEmail(tenantId);
        notificationClient.send(
                target,
                "EMAIL",
                "BILLING_INVOICE_GENERATED",
                Map.of("tenantId", tenantId, "invoiceId", String.valueOf(invoice)));
        log.info("发送账单生成通知: tenantId={}, target={}", tenantId, target);
    }

    public void sendPaymentConfirmation(String tenantId, Object invoice) {
        String target = resolveTargetEmail(tenantId);
        notificationClient.send(
                target,
                "EMAIL",
                "BILLING_PAYMENT_SUCCEEDED",
                Map.of("tenantId", tenantId, "invoiceId", String.valueOf(invoice)));
        log.info("发送支付确认通知: tenantId={}, target={}", tenantId, target);
    }

    public void sendOverdueNotification(String tenantId, Object invoice) {
        String target = resolveTargetEmail(tenantId);
        notificationClient.send(
                target,
                "EMAIL",
                "BILLING_INVOICE_OVERDUE",
                Map.of("tenantId", tenantId, "invoiceId", String.valueOf(invoice)));
        log.info("发送逾期通知: tenantId={}, target={}", tenantId, target);
    }

    public void sendTrialExpiryNotification(String tenantId, Object subscription) {
        String target = resolveTargetEmail(tenantId);
        notificationClient.send(target, "EMAIL", "BILLING_TRIAL_EXPIRES", Map.of("tenantId", tenantId));
        log.info("发送试用期到期通知: tenantId={}, target={}", tenantId, target);
    }
}
