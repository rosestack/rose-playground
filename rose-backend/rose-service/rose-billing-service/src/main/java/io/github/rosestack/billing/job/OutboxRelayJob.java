package io.github.rosestack.billing.job;

import io.github.rosestack.billing.service.OutboxService;
import io.github.rosestack.billing.service.TenantBillingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayJob {

    private final OutboxService outboxService;
    private final TenantBillingConfigService configService;

    @Value("${rose.billing.outbox.limit:100}")
    private int defaultOutboxLimit;

    @Scheduled(
            fixedDelayString = "${rose.billing.outbox.fixedDelay:5000}",
            initialDelayString = "${rose.billing.outbox.initialDelay:10000}")
    public void relay() {
        try {
            // 优先级：DB 全局键 > JVM System property > application.yml 默认
            int limit = defaultOutboxLimit;

            try {
                java.util.Optional<String> db = configService.getEffective(null, "billing.outbox.limit");
                if (db.isPresent()) {
                    int v = Integer.parseInt(db.get().trim());
                    if (v > 0) {
                        limit = v;
                    }
                }
            } catch (Exception ignore) {
            }
            if (limit == defaultOutboxLimit) {
                try {
                    String sys = System.getProperty("rose.billing.outbox.limit");
                    if (sys != null && !sys.isEmpty()) {
                        int v = Integer.parseInt(sys.trim());
                        if (v > 0) limit = v;
                    }
                } catch (Exception ignore) {
                }
            }

            int n = outboxService.relayPending(limit);
            if (n > 0) {
                log.info("Outbox relay sent {} events", n);
            }
        } catch (Exception e) {
            log.warn("Outbox relay error", e);
        }
    }
}
