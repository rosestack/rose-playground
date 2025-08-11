package io.github.rosestack.billing.job;

import io.github.rosestack.billing.service.PaymentPostingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconcilePostingJob {

    private final PaymentPostingService paymentPostingService;

    @Scheduled(fixedDelayString = "${rose.billing.posting.fixedDelay:300000}", initialDelayString = "${rose.billing.posting.initialDelay:20000}")
    public void postSuccessPayments() {
        try {
            int limit = Integer.parseInt(System.getProperty("rose.billing.posting.limit", "500"));
            int count = paymentPostingService.postSuccessPayments(limit);
            if (count > 0) {
                log.info("Posting marked {} payment records", count);
            }
        } catch (Exception e) {
            log.warn("Posting job error", e);
        }
    }
}

