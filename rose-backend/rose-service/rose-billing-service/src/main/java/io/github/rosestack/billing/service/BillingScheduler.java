package io.github.rosestack.billing.service;

import io.github.rosestack.billing.entity.BaseTenantSubscription;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 计费定时任务调度器
 *
 * @author rose
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final BillingService billingService;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingNotificationService notificationService;

    /**
     * 每小时检查到期订阅并生成账单
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行
    public void generateInvoicesForDueSubscriptions() {
        log.info("开始执行自动账单生成任务");

        try {
            LocalDateTime now = LocalDateTime.now();
            List<BaseTenantSubscription> dueSubscriptions = subscriptionRepository
                .findByNextBillingDateBeforeAndStatusIn(now,
                    List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL));

            for (BaseTenantSubscription subscription : dueSubscriptions) {
                try {
                    // 试用期结束处理
                    if (subscription.getInTrial() &&
                        subscription.getTrialEndDate().isBefore(now)) {
                        handleTrialExpiry(subscription);
                        continue;
                    }

                    // 生成账单
                    billingService.generateInvoice(subscription.getId());
                    log.info("为租户 {} 生成账单成功", subscription.getTenantId());

                } catch (Exception e) {
                    log.error("为订阅 {} 生成账单失败", subscription.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("自动账单生成任务执行失败", e);
        }

        log.info("自动账单生成任务执行完成");
    }

    /**
     * 每天检查逾期账单
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点执行
    public void handleOverdueInvoices() {
        log.info("开始执行逾期账单处理任务");

        try {
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            List<Invoice> overdueInvoices = invoiceRepository
                .findByStatusAndDueDateBefore(InvoiceStatus.PENDING, threeDaysAgo.toLocalDate());

            for (Invoice invoice : overdueInvoices) {
                try {
                    // 标记为逾期
                    invoice.setStatus(InvoiceStatus.OVERDUE);
                    invoiceRepository.updateById(invoice);

                    // 暂停相关订阅
                    BaseTenantSubscription subscription = subscriptionRepository
                        .selectById(invoice.getSubscriptionId());
                    if (subscription != null && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                        subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
                        subscription.setPausedAt(LocalDateTime.now());
                        subscription.setPauseReason("逾期付款");
                        subscriptionRepository.updateById(subscription);

                        // 发送逾期通知
                        notificationService.sendOverdueNotification(subscription.getTenantId(), invoice);
                    }

                    log.info("处理逾期账单：{}, 租户：{}", invoice.getId(), invoice.getTenantId());

                } catch (Exception e) {
                    log.error("处理逾期账单 {} 失败", invoice.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("逾期账单处理任务执行失败", e);
        }

        log.info("逾期账单处理任务执行完成");
    }

    /**
     * 每天汇总使用量数据
     */
    @Scheduled(cron = "0 30 1 * * ?") // 每天凌晨1:30执行
    public void aggregateUsageData() {
        log.info("开始执行使用量数据汇总任务");

        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime startOfDay = yesterday.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = yesterday.toLocalDate().atTime(23, 59, 59);

            // 汇总各租户的日使用量
            billingService.aggregateDailyUsage(startOfDay, endOfDay);

            // 检查使用量限制并发送警告
            billingService.checkUsageLimitsAndNotify();

        } catch (Exception e) {
            log.error("使用量数据汇总任务执行失败", e);
        }

        log.info("使用量数据汇总任务执行完成");
    }

    /**
     * 每周生成财务报告
     */
    @Scheduled(cron = "0 0 8 ? * MON") // 每周一上午8点执行
    public void generateWeeklyFinancialReport() {
        log.info("开始生成周财务报告");

        try {
            LocalDateTime endOfWeek = LocalDateTime.now().minusDays(1);
            LocalDateTime startOfWeek = endOfWeek.minusDays(6);

            billingService.generateFinancialReport(startOfWeek, endOfWeek, "WEEKLY");

        } catch (Exception e) {
            log.error("生成周财务报告失败", e);
        }

        log.info("周财务报告生成完成");
    }

    /**
     * 清理过期数据
     */
    @Scheduled(cron = "0 0 2 1 * ?") // 每月1号凌晨2点执行
    public void cleanupExpiredData() {
        log.info("开始清理过期数据");

        try {
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

            // 清理6个月前的详细使用量记录（保留聚合数据）
            int deletedRecords = billingService.cleanupOldUsageRecords(sixMonthsAgo);
            log.info("清理了 {} 条过期使用量记录", deletedRecords);

            // 清理已取消订阅的过期数据
            int deletedSubscriptions = billingService.cleanupCancelledSubscriptions(sixMonthsAgo);
            log.info("清理了 {} 个过期的已取消订阅", deletedSubscriptions);

        } catch (Exception e) {
            log.error("清理过期数据失败", e);
        }

        log.info("过期数据清理完成");
    }

    /**
     * 处理试用期到期
     */
    private void handleTrialExpiry(BaseTenantSubscription subscription) {
        try {
            subscription.setInTrial(false);
            subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
            subscriptionRepository.updateById(subscription);

            // 生成第一张正式账单
            billingService.generateInvoice(subscription.getId());

            // 发送试用期结束通知
            notificationService.sendTrialExpiryNotification(subscription.getTenantId(), subscription);

            log.info("处理试用期到期：租户 {}", subscription.getTenantId());

        } catch (Exception e) {
            log.error("处理试用期到期失败：订阅 {}", subscription.getId(), e);
        }
    }
}
