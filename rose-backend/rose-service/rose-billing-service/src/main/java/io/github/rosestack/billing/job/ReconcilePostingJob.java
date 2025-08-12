package io.github.rosestack.billing.job;

import io.github.rosestack.billing.service.PaymentPostingService;
import io.github.rosestack.billing.service.TenantBillingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconcilePostingJob {

    private final PaymentPostingService paymentPostingService;
    private final TenantBillingConfigService configService;

    private final Environment env;

    @Value("${rose.billing.posting.limit:500}")
    private int defaultPostingLimit;

    @Scheduled(fixedDelayString = "${rose.billing.posting.fixedDelay:300000}", initialDelayString = "${rose.billing.posting.initialDelay:20000}")
    public void postSuccessPayments() {
        try {
            // 优先级：DB(tenant->global, key=billing.posting.limit) > JVM System property(rose.billing.posting.limit) > application.yml(defaultPostingLimit)
            int limit = -1;
            try {
                java.util.Optional<String> db = configService.getEffective(null, "billing.posting.limit");
                if (db.isPresent()) limit = Integer.parseInt(db.get().trim());
            } catch (Exception ignore) {}
            if (limit <= 0) {
            log.info("Posting job limit resolved to {} (db>{}>sys>{}default)", limit,
                    io.github.rosestack.billing.service.TenantBillingConfigService.class.getSimpleName(), defaultPostingLimit);

                try {
                    String sys = System.getProperty("rose.billing.posting.limit", env.getProperty("rose.billing.posting.limit", ""));
                    if (sys != null && !sys.isEmpty()) {
                        int v = Integer.parseInt(sys.trim());
                        if (v > 0) limit = v;
                    }
                } catch (Exception ignore) {}
            }
            if (limit <= 0) limit = defaultPostingLimit;
            log.info("Posting job limit resolved to {} (source={})", limit, limit == defaultPostingLimit ? "yml" : (System.getProperty("rose.billing.posting.limit") != null ? "sys" : "db"));

            int count = paymentPostingService.postSuccessPayments(limit);
            if (count > 0) {
                log.info("Posting marked {} payment records", count);
            }
        } catch (Exception e) {
            log.warn("Posting job error", e);
        }
    }
}

