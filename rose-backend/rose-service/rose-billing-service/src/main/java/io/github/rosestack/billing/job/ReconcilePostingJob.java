package io.github.rosestack.billing.job;

import io.github.rosestack.billing.service.PaymentPostingService;
import io.github.rosestack.billing.service.TenantBillingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconcilePostingJob {

    private final PaymentPostingService paymentPostingService;
    private final TenantBillingConfigService configService;

    @Value("${rose.billing.posting.limit:500}")
    private int defaultPostingLimit;

    @Scheduled(
            fixedDelayString = "${rose.billing.posting.fixedDelay:300000}",
            initialDelayString = "${rose.billing.posting.initialDelay:20000}")
    public void postSuccessPayments() {
        try {
            // 优先级：DB 全局键 > JVM System property > application.yml 默认
            int limit = defaultPostingLimit;
            try {
                java.util.Optional<String> db = configService.getEffective(null, "billing.posting.limit");
                if (db.isPresent()) {
                    int v = Integer.parseInt(db.get().trim());
                    if (v > 0) limit = v;
                }
            } catch (Exception ignore) {
            }
            if (limit == defaultPostingLimit) {
                try {
                    String sys = System.getProperty("rose.billing.posting.limit");
                    if (sys != null && !sys.isEmpty()) {
                        int v = Integer.parseInt(sys.trim());
                        if (v > 0) limit = v;
                    }
                } catch (Exception ignore) {
                }
            }

            int count = paymentPostingService.postSuccessPayments(limit);
            if (count > 0) {
                log.info("Posting marked {} payment records", count);
            }
        } catch (Exception e) {
            log.warn("Posting job error", e);
        }
    }
}
