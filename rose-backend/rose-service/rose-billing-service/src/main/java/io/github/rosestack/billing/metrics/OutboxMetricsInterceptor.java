package io.github.rosestack.billing.metrics;

import io.github.rosestack.billing.entity.OutboxRecord;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * 为 Outbox 投递过程增加指标：成功/失败次数、payload大小分布、投递延迟
 */
@Component
@RequiredArgsConstructor
@ConditionalOnClass(MeterRegistry.class)
public class OutboxMetricsInterceptor {

    private final MeterRegistry registry;

    private DistributionSummary payloadSize;
    private Counter sendSuccess;
    private Counter sendFailure;
    private Timer deliveryLatency;

    @PostConstruct
    public void init() {
        payloadSize = DistributionSummary.builder("billing.outbox.payload.size")
                .baseUnit("bytes")
                .description("outbox payload size distribution")
                .register(registry);

        sendSuccess = Counter.builder("billing.outbox.send.success")
                .description("number of outbox messages sent successfully")
                .register(registry);

        sendFailure = Counter.builder("billing.outbox.send.failure")
                .description("number of outbox messages failed to send")
                .register(registry);

        deliveryLatency = Timer.builder("billing.outbox.delivery.latency")
                .description("time from outbox record creation to successful delivery")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void recordPayload(String payload) {
        if (payload != null) {
            payloadSize.record(payload.length());
        }
    }

    public void onSendSuccess(OutboxRecord rec, LocalDateTime now) {
        sendSuccess.increment();
        if (rec != null && rec.getCreatedTime() != null) {
            Duration d = Duration.between(rec.getCreatedTime(), now != null ? now : LocalDateTime.now());
            deliveryLatency.record(d);
        }
    }

    public void onSendFailure(OutboxRecord rec) {
        sendFailure.increment();
    }
}
