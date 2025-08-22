package io.github.rosestack.billing.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.config.BillingConfiguration;
import io.github.rosestack.billing.domain.enums.OutboxEventStatus;
import io.github.rosestack.billing.domain.enums.OutboxEventType;
import io.github.rosestack.billing.domain.outbox.OutboxEvent;
import io.github.rosestack.billing.domain.outbox.OutboxEventMapper;
import io.github.rosestack.core.exception.BusinessException;
import io.github.rosestack.core.util.IdUtils;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Outbox 事件发布服务
 * <p>
 * 负责创建和管理 Outbox 事件，确保业务操作和消息发布的原子性
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class OutboxEventService {

    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    private final BillingNotificationService notificationService;
    private final Timer outboxEventProcessingTimer;
    private final BillingConfiguration.BillingProperties billingProperties;

    public OutboxEventService(OutboxEventMapper outboxEventMapper,
                            ObjectMapper objectMapper,
                            BillingNotificationService notificationService,
                            Timer outboxEventProcessingTimer,
                            BillingConfiguration.BillingProperties billingProperties) {
        this.outboxEventMapper = outboxEventMapper;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.outboxEventProcessingTimer = outboxEventProcessingTimer;
        this.billingProperties = billingProperties;
    }

    /**
     * 创建并保存 Outbox 事件
     * <p>
     * 该方法需要在业务事务中调用，确保与业务操作的原子性
     *
     * @param tenantId 租户ID
     * @param eventType 事件类型
     * @param aggregateType 聚合根类型
     * @param aggregateId 聚合根ID
     * @param eventData 事件数据
     * @param metadata 元数据
     * @return 创建的事件ID
     */
    @Transactional
    public String createEvent(String tenantId, OutboxEventType eventType,
                             String aggregateType, String aggregateId,
                             Object eventData, Map<String, Object> metadata) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setEventId(IdUtils.fastSimpleUUID());
            event.setTenantId(tenantId);
            event.setEventType(eventType);
            event.setAggregateType(aggregateType);
            event.setAggregateId(aggregateId);
            event.setEventData(objectMapper.writeValueAsString(eventData));
            event.setStatus(OutboxEventStatus.PENDING);
            event.setRetryCount(0);
            event.setMaxRetryCount(5); // 默认最大重试5次

            if (metadata != null) {
                event.setMetadata(objectMapper.writeValueAsString(metadata));
            }

            outboxEventMapper.insert(event);

            log.info("Created outbox event: eventId={}, type={}, aggregateType={}, aggregateId={}",
                    event.getEventId(), eventType, aggregateType, aggregateId);

            return event.getEventId();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data", e);
            throw new BusinessException("outbox.event.serialization.failed", e);
        }
    }

    /**
     * 创建订阅相关事件
     *
     * @param tenantId 租户ID
     * @param eventType 事件类型
     * @param subscriptionId 订阅ID
     * @param eventData 事件数据
     * @return 事件ID
     */
    public String createSubscriptionEvent(String tenantId, OutboxEventType eventType,
                                        Long subscriptionId, Object eventData) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "billing-service");
        metadata.put("timestamp", LocalDateTime.now());

        return createEvent(tenantId, eventType, "Subscription",
                          subscriptionId.toString(), eventData, metadata);
    }

    /**
     * 创建账单相关事件
     *
     * @param tenantId 租户ID
     * @param eventType 事件类型
     * @param invoiceId 账单ID
     * @param eventData 事件数据
     * @return 事件ID
     */
    public String createInvoiceEvent(String tenantId, OutboxEventType eventType,
                                   Long invoiceId, Object eventData) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "billing-service");
        metadata.put("timestamp", LocalDateTime.now());

        return createEvent(tenantId, eventType, "Invoice",
                          invoiceId.toString(), eventData, metadata);
    }

    /**
     * 创建支付相关事件
     *
     * @param tenantId 租户ID
     * @param eventType 事件类型
     * @param paymentId 支付ID
     * @param eventData 事件数据
     * @return 事件ID
     */
    public String createPaymentEvent(String tenantId, OutboxEventType eventType,
                                   Long paymentId, Object eventData) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "billing-service");
        metadata.put("timestamp", LocalDateTime.now());

        return createEvent(tenantId, eventType, "Payment",
                          paymentId.toString(), eventData, metadata);
    }

    /**
     * 处理待发布的事件
     *
     * @param batchSize 批处理大小
     * @return 处理的事件数量
     */
    @Transactional
    public int processPendingEvents(int batchSize) {
        return outboxEventProcessingTimer.record(() -> {
            List<OutboxEvent> pendingEvents = outboxEventMapper.findPendingEvents(batchSize);

            int processedCount = 0;
            for (OutboxEvent event : pendingEvents) {
                try {
                    // 标记为发布中
                    event.markAsPublishing();
                    outboxEventMapper.updateById(event);

                    // 发布事件
                    publishEvent(event);

                    // 标记为已发布
                    event.markAsPublished();
                    outboxEventMapper.updateById(event);

                    processedCount++;

                    log.debug("Published outbox event: eventId={}, type={}",
                             event.getEventId(), event.getEventType());

                } catch (Exception e) {
                    log.error("Failed to publish outbox event: eventId={}, type={}",
                             event.getEventId(), event.getEventType(), e);

                    // 标记为失败
                    event.markAsFailed(e.getMessage());
                    outboxEventMapper.updateById(event);
                }
            }

            if (processedCount > 0) {
                log.info("Processed {} pending outbox events", processedCount);
            }

            return processedCount;
        });
    }

    /**
     * 处理失败的事件重试
     *
     * @param batchSize 批处理大小
     * @return 处理的事件数量
     */
    @Transactional
    public int processFailedEvents(int batchSize) {
        return outboxEventProcessingTimer.record(() -> {
            List<OutboxEvent> retryableEvents = outboxEventMapper.findRetryableFailedEvents(
                    LocalDateTime.now(), batchSize);

            int processedCount = 0;
            for (OutboxEvent event : retryableEvents) {
                if (!event.canRetry()) {
                    continue;
                }

                try {
                    // 标记为发布中
                    event.markAsPublishing();
                    outboxEventMapper.updateById(event);

                    // 重新发布事件
                    publishEvent(event);

                    // 标记为已发布
                    event.markAsPublished();
                    outboxEventMapper.updateById(event);

                    processedCount++;

                    log.info("Retried and published outbox event: eventId={}, type={}, retryCount={}",
                            event.getEventId(), event.getEventType(), event.getRetryCount());

                } catch (Exception e) {
                    log.error("Failed to retry outbox event: eventId={}, type={}, retryCount={}",
                             event.getEventId(), event.getEventType(), event.getRetryCount(), e);

                    // 更新失败信息
                    event.markAsFailed(e.getMessage());

                    // 如果超过最大重试次数，标记为跳过
                    if (event.getMaxRetryCount() != null &&
                        event.getRetryCount() >= event.getMaxRetryCount()) {
                        event.markAsSkipped("Exceeded maximum retry count");
                    }

                    outboxEventMapper.updateById(event);
                }
            }

            if (processedCount > 0) {
                log.info("Processed {} failed outbox events", processedCount);
            }

            return processedCount;
        });
    }

    /**
     * 清理已发布的旧事件
     *
     * @return 清理的事件数量
     */
    @Transactional
    public int cleanupOldEvents() {
        int retentionDays = billingProperties.getOutbox().getRetentionDays();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = outboxEventMapper.deletePublishedEventsBefore(cutoffTime);

        if (deletedCount > 0) {
            log.info("Cleaned up {} old outbox events before {}", deletedCount, cutoffTime);
        }

        return deletedCount;
    }

    /**
     * 获取事件状态统计
     *
     * @return 状态统计
     */
    public List<OutboxEventMapper.OutboxEventStatusCount> getEventStatusStats() {
        return outboxEventMapper.countEventsByStatus();
    }

    /**
     * 实际发布事件到消息队列或通知服务
     *
     * @param event 要发布的事件
     */
    private void publishEvent(OutboxEvent event) {
        // 这里可以根据事件类型选择不同的发布方式
        // 例如：发送到消息队列、调用外部API、发送通知等

        switch (event.getEventType()) {
            case SUBSCRIPTION_CREATED:
            case SUBSCRIPTION_UPDATED:
            case SUBSCRIPTION_CANCELLED:
            case SUBSCRIPTION_EXPIRED:
                publishSubscriptionEvent(event);
                break;

            case INVOICE_GENERATED:
            case INVOICE_PAID:
            case INVOICE_OVERDUE:
                publishInvoiceEvent(event);
                break;

            case PAYMENT_SUCCEEDED:
            case PAYMENT_FAILED:
                publishPaymentEvent(event);
                break;

            case QUOTA_EXCEEDED:
                publishQuotaEvent(event);
                break;

            case TRIAL_STARTED:
            case TRIAL_ENDED:
            case TRIAL_CONVERTED:
                publishTrialEvent(event);
                break;

            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void publishSubscriptionEvent(OutboxEvent event) {
        // 发布订阅相关事件
        log.info("Publishing subscription event: {}", event.getEventType());
        // TODO: 实现具体的事件发布逻辑
    }

    private void publishInvoiceEvent(OutboxEvent event) {
        // 发布账单相关事件
        log.info("Publishing invoice event: {}", event.getEventType());
        // TODO: 实现具体的事件发布逻辑
    }

    private void publishPaymentEvent(OutboxEvent event) {
        // 发布支付相关事件
        log.info("Publishing payment event: {}", event.getEventType());
        // TODO: 实现具体的事件发布逻辑
    }

    private void publishQuotaEvent(OutboxEvent event) {
        // 发布配额相关事件，通常需要发送通知
        log.info("Publishing quota event: {}", event.getEventType());

        try {
            // 发送配额超限通知
            notificationService.sendQuotaExceededNotification(
                    event.getTenantId(), event.getEventData());
        } catch (Exception e) {
            log.error("Failed to send quota notification", e);
            throw e;
        }
    }

    private void publishTrialEvent(OutboxEvent event) {
        // 发布试用相关事件
        log.info("Publishing trial event: {}", event.getEventType());
        // TODO: 实现具体的事件发布逻辑
    }
}
