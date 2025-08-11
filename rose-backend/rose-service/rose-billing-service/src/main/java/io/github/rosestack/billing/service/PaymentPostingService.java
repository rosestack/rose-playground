package io.github.rosestack.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPostingService {

    private final PaymentRecordRepository paymentRecordRepository;

    /**
     * 将成功支付的记录记入总账（posted=true），并设置 postedTime。
     * 使用乐观锁避免并发覆盖。
     *
     * @param limit 每次处理的最大条数
     * @return 实际处理条数
     */
    @Transactional
    public int postSuccessPayments(int limit) {
        List<PaymentRecord> list = paymentRecordRepository.selectList(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getStatus, PaymentRecordStatus.SUCCESS)
                .eq(PaymentRecord::getPosted, Boolean.FALSE)
                .last("limit " + Math.max(1, limit))
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
        return count;
    }
}

