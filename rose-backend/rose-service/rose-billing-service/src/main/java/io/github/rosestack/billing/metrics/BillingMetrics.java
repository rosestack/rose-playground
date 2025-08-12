package io.github.rosestack.billing.metrics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.rosestack.billing.entity.OutboxRecord;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.OutboxStatus;
import io.github.rosestack.billing.repository.OutboxRepository;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/** 基础指标埋点：Outbox backlog、失败数；未入账支付数 */
@Component
@RequiredArgsConstructor
@ConditionalOnClass(MeterRegistry.class)
public class BillingMetrics {

    private final MeterRegistry registry;
    private final OutboxRepository outboxRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @PostConstruct
    public void init() {
        Gauge.builder("billing.outbox.backlog", this, s -> outboxRepository.selectCount(null))
                .description("outbox backlog size")
                .register(registry);

        Gauge.builder(
                        "billing.outbox.failed",
                        this,
                        s -> outboxRepository.selectCount(new LambdaQueryWrapper<OutboxRecord>()
                                .eq(OutboxRecord::getStatus, OutboxStatus.FAILED)))
                .description("outbox failed size")
                .register(registry);

        Gauge.builder(
                        "billing.payments.unposted",
                        this,
                        s -> paymentRecordRepository.selectCount(
                                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getPosted, Boolean.FALSE)))
                .description("payments not posted")
                .register(registry);
    }
}
