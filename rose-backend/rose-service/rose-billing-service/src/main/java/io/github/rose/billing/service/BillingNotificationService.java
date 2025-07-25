package io.github.rose.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 计费通知服务
 *
 * @author rose
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingNotificationService {

    public void sendSubscriptionConfirmation(String tenantId, Object subscription) {
        log.info("发送订阅确认通知: 租户 {}", tenantId);
        // TODO: 集成现有的rose-notification-service
        // 可以发送邮件、短信等多种通知方式
    }

    public void sendInvoiceGenerated(String tenantId, Object invoice) {
        log.info("发送账单生成通知: 租户 {}", tenantId);
        // TODO: 发送账单生成邮件通知
    }

    public void sendPaymentConfirmation(String tenantId, Object invoice) {
        log.info("发送支付确认通知: 租户 {}", tenantId);
        // TODO: 发送支付成功确认邮件
    }

    public void sendOverdueNotification(String tenantId, Object invoice) {
        log.info("发送逾期通知: 租户 {}", tenantId);
        // TODO: 发送逾期付款警告邮件
    }

    public void sendTrialExpiryNotification(String tenantId, Object subscription) {
        log.info("发送试用期到期通知: 租户 {}", tenantId);
        // TODO: 发送试用期结束提醒邮件
    }
}
