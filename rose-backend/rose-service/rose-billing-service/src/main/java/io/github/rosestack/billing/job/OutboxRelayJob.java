package io.github.rosestack.billing.job;

import io.github.rosestack.billing.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayJob {

    private final OutboxService outboxService;

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void relay() {
        try {
            int n = outboxService.relayPending(100);
            if (n > 0) {
                log.info("Outbox relay sent {} events", n);
            }
        } catch (Exception e) {
            log.warn("Outbox relay error", e);
        }
    }
}

