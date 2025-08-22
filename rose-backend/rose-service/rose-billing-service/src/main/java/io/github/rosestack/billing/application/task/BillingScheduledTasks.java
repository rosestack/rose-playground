package io.github.rosestack.billing.application.task;

import io.github.rosestack.billing.application.service.BillInvoiceService;
import io.github.rosestack.billing.application.service.BillSubscriptionService;
import io.github.rosestack.billing.application.service.BillingNotificationService;
import io.github.rosestack.billing.domain.invoice.BillInvoice;
import io.github.rosestack.billing.domain.invoice.BillInvoiceMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 计费系统定时任务
 *
 * 提供自动处理过期订阅、逾期账单、账单生成等定时任务
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduledTasks {

    private final BillSubscriptionService subscriptionService;
    private final BillInvoiceService invoiceService;
    private final BillingNotificationService notificationService;
    private final BillInvoiceMapper invoiceMapper;
    private final BillSubscriptionMapper subscriptionMapper;

    /**
     * 每小时处理过期订阅
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void processExpiredSubscriptions() {
        log.info("Starting scheduled task: process expired subscriptions");
        try {
            subscriptionService.processExpiredSubscriptions();
            log.info("Completed scheduled task: process expired subscriptions");
        } catch (Exception e) {
            log.error("Failed to process expired subscriptions", e);
        }
    }

    /**
     * 每天处理逾期账单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void processOverdueBills() {
        log.info("Starting scheduled task: process overdue bills");
        try {
            invoiceService.processOverdueBills();
            log.info("Completed scheduled task: process overdue bills");
        } catch (Exception e) {
            log.error("Failed to process overdue bills", e);
        }
    }

    /**
     * 每月自动生成账单
     */
    @Scheduled(cron = "0 0 2 1 * ?") // 每月1日凌晨2点执行
    public void generateMonthlyBills() {
        log.info("Starting scheduled task: generate monthly bills");
        try {
            LocalDate now = LocalDate.now();
            LocalDate previousMonth = now.minusMonths(1);
            LocalDate periodStart = previousMonth.withDayOfMonth(1);
            LocalDate periodEnd = previousMonth.withDayOfMonth(previousMonth.lengthOfMonth());
            
            invoiceService.generateBillsForPeriod(periodStart, periodEnd);
            log.info("Completed scheduled task: generate monthly bills for period {} to {}", 
                    periodStart, periodEnd);
        } catch (Exception e) {
            log.error("Failed to generate monthly bills", e);
        }
    }

    /**
     * 每周清理历史数据（可选，根据需要开启）
     */
    // @Scheduled(cron = "0 0 3 * * SUN") // 每周日凌晨3点执行
    public void cleanupHistoricalData() {
        log.info("Starting scheduled task: cleanup historical data");
        try {
            // 保留12个月的用量数据
            // usageService.cleanupHistoricalUsage(12);
            log.info("Completed scheduled task: cleanup historical data");
        } catch (Exception e) {
            log.error("Failed to cleanup historical data", e);
        }
    }

    /**
     * 每天发送账单提醒
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天早上9点执行
    public void sendBillReminders() {
        log.info("Starting scheduled task: send bill reminders");
        try {
            LocalDate today = LocalDate.now();
            
            // 查找即将到期的账单（3天、7天、1天前提醒）
            sendRemindersForDaysBeforeDue(today, 7);
            sendRemindersForDaysBeforeDue(today, 3);
            sendRemindersForDaysBeforeDue(today, 1);
            
            log.info("Completed scheduled task: send bill reminders");
        } catch (Exception e) {
            log.error("Failed to send bill reminders", e);
        }
    }
    
    /**
     * 发送指定天数前的账单提醒
     */
    private void sendRemindersForDaysBeforeDue(LocalDate today, int daysBeforeDue) {
        LocalDate targetDueDate = today.plusDays(daysBeforeDue);
        List<BillInvoice> upcomingInvoices = invoiceMapper.findUpcomingDueInvoices(targetDueDate);
        
        for (BillInvoice invoice : upcomingInvoices) {
            try {
                BillSubscription subscription = subscriptionMapper.selectById(invoice.getSubscriptionId());
                if (subscription != null) {
                    notificationService.sendInvoiceDueReminderNotification(
                            invoice, subscription, daysBeforeDue);
                }
            } catch (Exception e) {
                log.error("Failed to send reminder for invoice: {}", invoice.getId(), e);
            }
        }
        
        log.info("Sent {} reminders for bills due in {} days", 
                upcomingInvoices.size(), daysBeforeDue);
    }

    /**
     * 每天检查续费订阅
     */
    @Scheduled(cron = "0 30 8 * * ?") // 每天早上8:30执行
    public void checkRenewalSubscriptions() {
        log.info("Starting scheduled task: check renewal subscriptions");
        try {
            // TODO: 实现续费检查逻辑
            // 1. 查找需要续费的订阅
            // 2. 自动续费或发送续费提醒
            // 3. 处理续费失败的订阅
            
            log.info("Completed scheduled task: check renewal subscriptions");
        } catch (Exception e) {
            log.error("Failed to check renewal subscriptions", e);
        }
    }
}