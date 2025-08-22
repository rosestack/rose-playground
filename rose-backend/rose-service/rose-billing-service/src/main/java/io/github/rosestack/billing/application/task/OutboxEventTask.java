package io.github.rosestack.billing.application.task;

import io.github.rosestack.billing.application.service.OutboxEventService;
import io.github.rosestack.billing.config.BillingConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox 事件处理定时任务
 * <p>
 * 定期处理待发布和失败的 Outbox 事件，清理旧事件
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "billing.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventTask {

    private final OutboxEventService outboxEventService;
    private final BillingConfiguration.BillingProperties billingProperties;

    /**
     * 处理待发布的事件
     * 每30秒执行一次
     */
    @Scheduled(fixedDelayString = "${billing.outbox.processing.delay:30000}")
    public void processPendingEvents() {
        try {
            int batchSize = billingProperties.getEngine().getBatchSize();
            int processedCount = outboxEventService.processPendingEvents(batchSize);
            if (processedCount > 0) {
                log.debug("Processed {} pending outbox events", processedCount);
            }
        } catch (Exception e) {
            log.error("Error processing pending outbox events", e);
        }
    }

    /**
     * 处理失败事件重试
     * 每2分钟执行一次
     */
    @Scheduled(fixedDelayString = "${billing.outbox.retry.delay:120000}")
    public void processFailedEvents() {
        try {
            int batchSize = billingProperties.getEngine().getBatchSize();
            int processedCount = outboxEventService.processFailedEvents(batchSize);
            if (processedCount > 0) {
                log.debug("Processed {} failed outbox events", processedCount);
            }
        } catch (Exception e) {
            log.error("Error processing failed outbox events", e);
        }
    }

    /**
     * 清理旧事件
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "${billing.outbox.cleanup.cron:0 0 2 * * ?}")
    public void cleanupOldEvents() {
        try {
            // 清理已发布的旧事件
            int deletedCount = outboxEventService.cleanupOldEvents();
            if (deletedCount > 0) {
                log.info("Cleaned up {} old outbox events", deletedCount);
            }
        } catch (Exception e) {
            log.error("Error cleaning up old outbox events", e);
        }
    }

    /**
     * 打印事件状态统计
     * 每小时执行一次
     */
    @Scheduled(fixedRateString = "${billing.outbox.stats.interval:3600000}")
    public void logEventStats() {
        try {
            var stats = outboxEventService.getEventStatusStats();
            if (!stats.isEmpty()) {
                log.info("Outbox event statistics: {}", stats);
            }
        } catch (Exception e) {
            log.error("Error getting outbox event statistics", e);
        }
    }
}