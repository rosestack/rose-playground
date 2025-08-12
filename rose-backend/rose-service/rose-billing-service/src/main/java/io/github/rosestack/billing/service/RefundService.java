package io.github.rosestack.billing.service;

import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.entity.RefundRecord;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.enums.RefundStatus;
import io.github.rosestack.billing.payment.PaymentGatewayService;
import io.github.rosestack.billing.repository.RefundRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefundService {

    private final InvoiceService invoiceService;
    private final PaymentGatewayService paymentGatewayService;
    private final RefundRecordRepository refundRecordRepository;
    private final io.github.rosestack.billing.repository.PaymentRecordRepository paymentRecordRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private OutboxService outboxService;

    @Transactional
    public RefundResult requestRefund(String invoiceId, BigDecimal amount, String reason) {
        return requestRefund(invoiceId, amount, reason, null);
    }

    /**
     * 幂等退款请求（可选 idempotencyKey）
     */
    @Transactional
    public RefundResult requestRefund(String invoiceId, BigDecimal amount, String reason, String idempotencyKey) {
        Invoice invoice = invoiceService.getInvoiceDetails(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.PAID) {
            return RefundResult.failed("仅支持对已支付账单发起退款");
        }
        if (invoice.getPaymentTransactionId() == null
                || invoice.getPaymentTransactionId().isBlank()) {
            return RefundResult.failed("账单缺少交易号，无法退款");
        }
        // 幂等：如传入幂等键且已存在成功记录，直接返回成功
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RefundRecord>()
                    .eq(RefundRecord::getInvoiceId, invoiceId)
                    .eq(RefundRecord::getIdempotencyKey, idempotencyKey)
                    .eq(RefundRecord::getStatus, RefundStatus.SUCCESS);
            if (refundRecordRepository.selectCount(qw) > 0) {
                return RefundResult.success("idempotent");
            }
        }
        // 校验累计退款金额不超过总额
        BigDecimal refunded = refundRecordRepository.sumSucceededAmountByInvoiceId(invoiceId);
        BigDecimal remain = invoice.getTotalAmount().subtract(refunded);
        if (amount.compareTo(remain) > 0) {
            return RefundResult.failed("退款金额超出可退余额");
        }

        // 调用网关处理退款
        RefundResult result = paymentGatewayService.processRefund(
                invoice.getPaymentTransactionId(), amount, reason, invoice.getTenantId());

        // 记录 RefundRecord
        RefundRecord rr = new RefundRecord();
        rr.setTenantId(invoice.getTenantId());
        rr.setInvoiceId(invoiceId);
        rr.setPaymentMethod(invoice.getPaymentMethod());
        rr.setTransactionId(invoice.getPaymentTransactionId());
        rr.setRefundAmount(amount);
        rr.setReason(reason);
        rr.setCurrency(invoice.getCurrency());
        rr.setRequestedTime(LocalDateTime.now());
        rr.setIdempotencyKey(idempotencyKey);
        if (result.isSuccess()) {
            rr.setRefundId(result.getRefundId());
            rr.setStatus(RefundStatus.SUCCESS);
            rr.setCompletedTime(LocalDateTime.now());
        } else {
            rr.setStatus(RefundStatus.FAILED);
        }
        refundRecordRepository.insert(rr);

        // 如为全额退款，将发票标记为 REFUNDED（部分退款保留 PAID）
        if (result.isSuccess()) {
            BigDecimal newRefunded = refunded.add(amount);
            if (newRefunded.compareTo(invoice.getTotalAmount()) >= 0) {
                invoice.setStatus(InvoiceStatus.REFUNDED);
                invoiceService.updateById(invoice);
            }
        }

        // Outbox: 退款请求同步成功时外发事件
        if (result.isSuccess() && outboxService != null) {
            try {
                String payload = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(java.util.Map.of(
                                "invoiceId", invoiceId,
                                "refundId", rr.getRefundId(),
                                "amount", amount,
                                "currency", rr.getCurrency(),
                                "occurredTime", java.time.LocalDateTime.now().toString()));
                outboxService.saveEvent(invoice.getTenantId(), "RefundSucceeded", invoiceId, payload);
            } catch (Exception ignore) {
            }
        }

        return result;
    }

    /**
     * 退款回调处理：更新 RefundRecord 状态与原文，必要时更新发票状态
     */
    @Transactional
    public boolean processRefundCallback(String paymentMethod, Map<String, Object> data) {
        String invoiceId = String.valueOf(data.getOrDefault("invoiceId", data.getOrDefault("invoice_id", "")));
        String refundId = String.valueOf(data.getOrDefault("refund_id", data.getOrDefault("id", "")));
        if (invoiceId.isBlank() || refundId.isBlank()) return false;

        // 金额与状态解析委托给网关（网关内再委派给具体 Processor）
        BigDecimal callbackAmount = paymentGatewayService.parseRefundAmount(paymentMethod, data);
        boolean isSuccess = paymentGatewayService.isRefundSuccess(paymentMethod, data);

        // 查或建 RefundRecord
        var qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RefundRecord>()
                .eq(RefundRecord::getInvoiceId, invoiceId)
                .eq(RefundRecord::getRefundId, refundId);
        RefundRecord rr = refundRecordRepository.selectOne(qw);
        if (rr == null) {
            rr = new RefundRecord();
            rr.setInvoiceId(invoiceId);
            rr.setPaymentMethod(paymentMethod);
            rr.setRefundId(refundId);
            rr.setRequestedTime(LocalDateTime.now());
        }
        // 序列化回调为 JSON 字符串并脱敏
        try {
            String raw = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
            rr.setRawCallback(io.github.rosestack.billing.util.SensitiveDataMasker.mask(raw));
        } catch (Exception e) {
            rr.setRawCallback(io.github.rosestack.billing.util.SensitiveDataMasker.mask(data.toString()));
        }
        if (callbackAmount != null) rr.setRefundAmount(callbackAmount);
        rr.setStatus(isSuccess ? RefundStatus.SUCCESS : RefundStatus.FAILED);
        if (isSuccess) rr.setCompletedTime(LocalDateTime.now());

        if (rr.getId() == null) {
            refundRecordRepository.insert(rr);
        } else {
            int affected = refundRecordRepository.updateById(rr);
            if (affected != 1) {
                // 乐观锁并发冲突短重试一次
                RefundRecord fresh = refundRecordRepository.selectById(rr.getId());
                if (fresh != null) {
                    fresh.setRefundAmount(rr.getRefundAmount());
                    fresh.setStatus(rr.getStatus());
                    fresh.setCompletedTime(rr.getCompletedTime());
                    fresh.setRawCallback(rr.getRawCallback());
                    try {
                        refundRecordRepository.updateById(fresh);
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        // 同步 PaymentRecord 渠道状态/金额（如有）
        try {
            paymentRecordRepository.findByTransactionId(rr.getTransactionId()).ifPresent(pr -> {
                if (callbackAmount != null) pr.setChannelAmount(callbackAmount);
                pr.setChannelStatus(isSuccess ? "SUCCESS" : "FAILED");
                paymentRecordRepository.updateById(pr);
            });
        } catch (Exception e) {
            log.warn("同步 PaymentRecord 渠道字段失败", e);
        }

        // 计算累计退款（含本次）以判定是否全额
        Invoice invoice = invoiceService.getInvoiceDetails(invoiceId);
        BigDecimal refunded = refundRecordRepository.sumSucceededAmountByInvoiceId(invoiceId);
        if (isSuccess && callbackAmount != null) {
            refunded = refunded.add(callbackAmount);
        }
        if (isSuccess
                && refunded.compareTo(invoice.getTotalAmount()) >= 0
                && invoice.getStatus() != InvoiceStatus.REFUNDED) {
            invoice.setStatus(InvoiceStatus.REFUNDED);
            invoiceService.updateById(invoice);
            if (outboxService != null) {
                try {
                    String payload = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(java.util.Map.of(
                                    "invoiceId",
                                    invoiceId,
                                    "refundId",
                                    rr.getRefundId(),
                                    "totalRefunded",
                                    refunded,
                                    "currency",
                                    invoice.getCurrency(),
                                    "occurredTime",
                                    java.time.LocalDateTime.now().toString()));
                    outboxService.saveEvent(invoice.getTenantId(), "InvoiceRefunded", invoiceId, payload);
                } catch (Exception ignore) {
                }
            }
        }
        return true;
    }
}
