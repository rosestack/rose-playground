package io.github.rosestack.billing.job;

import io.github.rosestack.billing.service.OutboxService;
import io.github.rosestack.billing.service.TenantBillingConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayJob {

    @Value("${rose.billing.outbox.limit:100}")
    private int defaultOutboxLimit;

    private final OutboxService outboxService;
    private final Environment env;

    private final io.github.rosestack.billing.service.TenantBillingConfigService configService;

    @Scheduled(fixedDelayString = "${rose.billing.outbox.fixedDelay:5000}", initialDelayString = "${rose.billing.outbox.initialDelay:10000}")
    public void relay() {
        try {
            // 优先级：DB(tenant->global, key=billing.outbox.limit) > JVM System property(rose.billing.outbox.limit) > application.yml(defaultOutboxLimit)
            int limit = -1;
            String source = "yml";
            try {
                java.util.Optional<String> db = configService.getEffective(null, "billing.outbox.limit");
                if (db.isPresent()) {
                    int v = Integer.parseInt(db.get().trim());
                    if (v > 0) { limit = v; source = "db"; }
                }
            } catch (Exception ignore) {}
            if (limit <= 0) {
                try {
                    String sys = System.getProperty("rose.billing.outbox.limit", env.getProperty("rose.billing.outbox.limit", ""));
                    if (sys != null && !sys.isEmpty()) {
                        int v = Integer.parseInt(sys.trim());
                        if (v > 0) { limit = v; source = sys.equals(env.getProperty("rose.billing.outbox.limit")) ? "yml" : "sys"; }
                    }
                } catch (Exception ignore) {}
            }
            if (limit <= 0) { limit = defaultOutboxLimit; source = "yml"; }

            log.info("Outbox relay limit resolved to {} (source={})", limit, source);
            int n = outboxService.relayPending(limit);
            if (n > 0) {
                log.info("Outbox relay sent {} events", n);
            }
        } catch (Exception e) {
            log.warn("Outbox relay error", e);
        }
    }
}

