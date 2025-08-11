package io.github.rosestack.billing.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconcilePostingJob {

    private final PaymentRecordRepository paymentRecordRepository;

    @Scheduled(fixedDelayString = "${rose.billing.posting.fixedDelay:300000}", initialDelayString = "${rose.billing.posting.initialDelay:20000}")
    public void postSuccessPayments() {
        try {
            List<PaymentRecord> list = paymentRecordRepository.selectList(new LambdaQueryWrapper<PaymentRecord>()
                    .eq(PaymentRecord::getStatus, PaymentRecordStatus.SUCCESS)
                    .eq(PaymentRecord::getPosted, Boolean.FALSE)
                    .last("limit 500")
            );
            int count = 0;
            for (PaymentRecord pr : list) {
                pr.setPosted(true);
                pr.setPostedTime(LocalDateTime.now());
                try {
                    int affected = paymentRecordRepository.updateById(pr);
                    if (affected == 1) {
                        count++;
                    } else {
                        log.debug("Posting skipped due to concurrent update, id={}", pr.getId());
                    }
                } catch (Exception e) {
                    log.warn("Posting update failed, id={}", pr.getId(), e);
                }
            }
            if (count > 0) log.info("Posting marked {} payment records", count);
        } catch (Exception e) {
            log.warn("Posting job error", e);
        }
    }
}

