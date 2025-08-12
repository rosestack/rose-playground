package io.github.rosestack.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.rosestack.billing.entity.OutboxRecord;
import io.github.rosestack.billing.enums.OutboxStatus;
import io.github.rosestack.billing.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final io.github.rosestack.billing.metrics.OutboxMetricsInterceptor outboxMetrics;

    private final OutboxRepository outboxRepository;
    private final OutboxPublisher publisher;

    @Transactional
    public void saveEvent(String tenantId, String eventType, String aggregateId, String payloadJson) {
        OutboxRecord rec = new OutboxRecord();
        rec.setTenantId(tenantId);
        rec.setEventType(eventType);
        rec.setAggregateId(aggregateId);
        rec.setPayload(payloadJson);
        rec.setStatus(OutboxStatus.PENDING);
        rec.setRetryCount(0);
        outboxRepository.insert(rec);
    }

    /**
     * 拉取可投递事件并投递（最小实现）
     */
    @Transactional
    public int relayPending(int limit) {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxRecord> list = outboxRepository.selectList(new LambdaQueryWrapper<OutboxRecord>()
                .in(OutboxRecord::getStatus, OutboxStatus.PENDING, OutboxStatus.FAILED)
                .and(q -> q.le(OutboxRecord::getNextRetryAt, now).or().isNull(OutboxRecord::getNextRetryAt))
                .last("limit " + limit));

        int success = 0;
        for (OutboxRecord rec : list) {
            try {
                // metrics: payload size
                if (outboxMetrics != null) outboxMetrics.recordPayload(rec.getPayload());
                publisher.publish(rec.getEventType(), rec.getPayload());
                rec.setStatus(OutboxStatus.SENT);
                int affected = outboxRepository.updateById(rec);
                if (affected == 1) {
                    success++;
                    if (outboxMetrics != null) outboxMetrics.onSendSuccess(rec, now);
                } else {
                    log.debug("Outbox update skipped due to concurrent update, id={}", rec.getId());
                }
            } catch (Exception e) {
                int curr = rec.getRetryCount() == null ? 0 : rec.getRetryCount();
                rec.setRetryCount(curr + 1);
                Duration backoff = Duration.ofSeconds((long) Math.min(300, Math.pow(2, Math.min(10, curr))));
                rec.setNextRetryAt(now.plus(backoff));
                rec.setStatus(OutboxStatus.FAILED);
                try {
                    int affected = outboxRepository.updateById(rec);
                    if (affected != 1) {
                        log.debug("Outbox retry update skipped due to concurrent update, id={}", rec.getId());
                    }
                } catch (Exception ex) {
                    log.warn("Outbox retry state persist failed, id={}", rec.getId(), ex);
                }
                if (outboxMetrics != null) outboxMetrics.onSendFailure(rec);
                log.warn("Outbox relay failed, id={}, eventType={}", rec.getId(), rec.getEventType(), e);
            }
        }
        return success;
    }
}
