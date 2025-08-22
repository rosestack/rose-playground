package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.invoice.BillInvoice;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.trial.BillTrialRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 计费通知服务
 *
 * 集成通知系统，为计费相关事件提供通知功能
 * 包括账单提醒、试用到期、支付成功/失败、配额预警等通知
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingNotificationService {

    // 通知模板常量
    private static final String TEMPLATE_TRIAL_START = "BILLING_TRIAL_START";
    private static final String TEMPLATE_TRIAL_EXPIRING = "BILLING_TRIAL_EXPIRING";
    private static final String TEMPLATE_TRIAL_EXPIRED = "BILLING_TRIAL_EXPIRED";
    private static final String TEMPLATE_INVOICE_GENERATED = "BILLING_INVOICE_GENERATED";
    private static final String TEMPLATE_INVOICE_DUE_REMINDER = "BILLING_INVOICE_DUE_REMINDER";
    private static final String TEMPLATE_PAYMENT_SUCCESS = "BILLING_PAYMENT_SUCCESS";
    private static final String TEMPLATE_PAYMENT_FAILED = "BILLING_PAYMENT_FAILED";
    private static final String TEMPLATE_QUOTA_WARNING = "BILLING_QUOTA_WARNING";
    private static final String TEMPLATE_QUOTA_EXCEEDED = "BILLING_QUOTA_EXCEEDED";
    private static final String TEMPLATE_SUBSCRIPTION_RENEWED = "BILLING_SUBSCRIPTION_RENEWED";
    private static final String TEMPLATE_SUBSCRIPTION_CANCELLED = "BILLING_SUBSCRIPTION_CANCELLED";

    /**
     * 发送试用开始通知
     */
    public void sendTrialStartNotification(BillTrialRecord trialRecord, BillSubscription subscription) {
        log.info("Sending trial start notification for subscription: {}", subscription.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("trialDays", trialRecord.getTrialDays());
        params.put("trialEndDate", formatDate(trialRecord.getTrialEndTime()));

        sendNotification(subscription.getTenantId(), TEMPLATE_TRIAL_START,
                        "试用开始通知", params);
    }

    /**
     * 发送试用即将到期通知
     */
    public void sendTrialExpiringNotification(BillTrialRecord trialRecord, BillSubscription subscription) {
        log.info("Sending trial expiring notification for subscription: {}", subscription.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("remainingDays", trialRecord.getRemainingDays());
        params.put("trialEndDate", formatDate(trialRecord.getTrialEndTime()));

        sendNotification(subscription.getTenantId(), TEMPLATE_TRIAL_EXPIRING,
                        "试用即将到期提醒", params);
    }

    /**
     * 发送试用已过期通知
     */
    public void sendTrialExpiredNotification(BillTrialRecord trialRecord, BillSubscription subscription) {
        log.info("Sending trial expired notification for subscription: {}", subscription.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("trialEndDate", formatDate(trialRecord.getTrialEndTime()));

        sendNotification(subscription.getTenantId(), TEMPLATE_TRIAL_EXPIRED,
                        "试用已过期通知", params);
    }

    /**
     * 发送账单生成通知
     */
    public void sendInvoiceGeneratedNotification(BillInvoice invoice, BillSubscription subscription) {
        log.info("Sending invoice generated notification for invoice: {}", invoice.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("invoiceId", invoice.getId());
        params.put("invoiceNo", invoice.getBillNo());
        params.put("totalAmount", invoice.getTotalAmount());
        params.put("currency", invoice.getCurrency());
        params.put("dueDate", formatDate(invoice.getDueDate()));
        params.put("periodStart", formatDate(invoice.getPeriodStart()));
        params.put("periodEnd", formatDate(invoice.getPeriodEnd()));

        sendNotification(subscription.getTenantId(), TEMPLATE_INVOICE_GENERATED,
                        "账单生成通知", params);
    }

    /**
     * 发送账单到期提醒
     */
    public void sendInvoiceDueReminderNotification(BillInvoice invoice, BillSubscription subscription,
                                                  int daysUntilDue) {
        log.info("Sending invoice due reminder for invoice: {}, days until due: {}",
                invoice.getId(), daysUntilDue);

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("invoiceId", invoice.getId());
        params.put("invoiceNo", invoice.getBillNo());
        params.put("totalAmount", invoice.getTotalAmount());
        params.put("currency", invoice.getCurrency());
        params.put("dueDate", formatDate(invoice.getDueDate()));
        params.put("daysUntilDue", daysUntilDue);

        String subject = String.format("账单到期提醒（%d天后到期）", daysUntilDue);
        sendNotification(subscription.getTenantId(), TEMPLATE_INVOICE_DUE_REMINDER,
                        subject, params);
    }

    /**
     * 发送支付成功通知
     */
    public void sendPaymentSuccessNotification(BillInvoice invoice, BillSubscription subscription,
                                             BigDecimal paidAmount) {
        log.info("Sending payment success notification for invoice: {}", invoice.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("invoiceId", invoice.getId());
        params.put("invoiceNo", invoice.getBillNo());
        params.put("paidAmount", paidAmount);
        params.put("currency", invoice.getCurrency());
        params.put("paymentTime", formatDateTime(LocalDateTime.now()));

        sendNotification(subscription.getTenantId(), TEMPLATE_PAYMENT_SUCCESS,
                        "支付成功通知", params);
    }

    /**
     * 发送支付失败通知
     */
    public void sendPaymentFailedNotification(BillInvoice invoice, BillSubscription subscription,
                                            String failureReason) {
        log.info("Sending payment failed notification for invoice: {}", invoice.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("invoiceId", invoice.getId());
        params.put("invoiceNo", invoice.getBillNo());
        params.put("totalAmount", invoice.getTotalAmount());
        params.put("currency", invoice.getCurrency());
        params.put("failureReason", failureReason);
        params.put("dueDate", formatDate(invoice.getDueDate()));

        sendNotification(subscription.getTenantId(), TEMPLATE_PAYMENT_FAILED,
                        "支付失败通知", params);
    }

    /**
     * 发送配额预警通知
     */
    public void sendQuotaWarningNotification(BillSubscription subscription, Long featureId,
                                           String featureName, BigDecimal usagePercentage) {
        log.info("Sending quota warning notification for subscription: {}, feature: {}",
                subscription.getId(), featureId);

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("featureId", featureId);
        params.put("featureName", featureName);
        params.put("usagePercentage", usagePercentage);

        String subject = String.format("配额预警：%s 使用率已达 %.1f%%", featureName, usagePercentage);
        sendNotification(subscription.getTenantId(), TEMPLATE_QUOTA_WARNING,
                        subject, params);
    }

    /**
     * 发送配额超限通知
     */
    public void sendQuotaExceededNotification(BillSubscription subscription, Long featureId,
                                            String featureName, BigDecimal currentUsage,
                                            BigDecimal quotaLimit) {
        log.info("Sending quota exceeded notification for subscription: {}, feature: {}",
                subscription.getId(), featureId);

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("featureId", featureId);
        params.put("featureName", featureName);
        params.put("currentUsage", currentUsage);
        params.put("quotaLimit", quotaLimit);
        params.put("excessUsage", currentUsage.subtract(quotaLimit));

        String subject = String.format("配额超限：%s 已超出限制", featureName);
        sendNotification(subscription.getTenantId(), TEMPLATE_QUOTA_EXCEEDED,
                        subject, params);
    }

    /**
     * 发送订阅续费成功通知
     */
    public void sendSubscriptionRenewedNotification(BillSubscription subscription) {
        log.info("Sending subscription renewed notification for subscription: {}", subscription.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("subscriptionNo", subscription.getSubNo());
        params.put("nextBillingDate", formatDate(subscription.getCurrentPeriodEndTime()));

        sendNotification(subscription.getTenantId(), TEMPLATE_SUBSCRIPTION_RENEWED,
                        "订阅续费成功", params);
    }

    /**
     * 发送订阅取消通知
     */
    public void sendSubscriptionCancelledNotification(BillSubscription subscription, String cancelReason) {
        log.info("Sending subscription cancelled notification for subscription: {}", subscription.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", subscription.getTenantId());
        params.put("subscriptionId", subscription.getId());
        params.put("subscriptionNo", subscription.getSubNo());
        params.put("cancelReason", cancelReason);
        params.put("endDate", formatDate(subscription.getEndTime()));

        sendNotification(subscription.getTenantId(), TEMPLATE_SUBSCRIPTION_CANCELLED,
                        "订阅取消通知", params);
    }

    /**
     * 发送通知的核心方法
     */
    private void sendNotification(String tenantId, String templateCode, String subject,
                                Map<String, Object> params) {
        try {
            // TODO: 集成实际的通知系统
            // 这里可以集成 Rose 通知系统，发送邮件、短信、站内信等
            // 示例代码：
            // notificationService.send(NotificationRequest.builder()
            //     .tenantId(tenantId)
            //     .templateCode(templateCode)
            //     .subject(subject)
            //     .params(params)
            //     .channels(Arrays.asList(CHANNEL_EMAIL, CHANNEL_SMS))
            //     .build());

            log.info("Notification sent: tenant={}, template={}, subject={}",
                    tenantId, templateCode, subject);
        } catch (Exception e) {
            log.error("Failed to send notification: tenant={}, template={}, subject={}",
                     tenantId, templateCode, subject, e);
        }
    }

    /**
     * 格式化日期
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 格式化日期
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
