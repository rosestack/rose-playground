package io.github.rosestack.notice;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;

/** 发送指标封装（可选 Micrometer）。 */
public final class NoticeMetrics {
    private final MeterRegistry registry;
    private final Counter sendSuccess;
    private final Counter sendFailure;
    private final Timer sendTimer;

    public NoticeMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.sendSuccess = Counter.builder("notice.send.success")
                .description("notice send success")
                .register(registry);
        this.sendFailure = Counter.builder("notice.send.failure")
                .description("notice send failure")
                .register(registry);
        this.sendTimer = Timer.builder("notice.send.duration")
                .description("notice send duration")
                .register(registry);
    }

    public void recordSuccess(long nanos) {
        sendSuccess.increment();
        sendTimer.record(nanos, TimeUnit.NANOSECONDS);
    }

    public void recordFailure(long nanos) {
        sendFailure.increment();
        sendTimer.record(nanos, TimeUnit.NANOSECONDS);
    }
}
