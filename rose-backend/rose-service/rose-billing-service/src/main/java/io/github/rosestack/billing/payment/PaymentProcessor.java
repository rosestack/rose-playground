package io.github.rosestack.billing.payment;


import io.github.rosestack.billing.dto.PaymentRequest;
import io.github.rosestack.billing.dto.PaymentResult;
import io.github.rosestack.billing.dto.RefundResult;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付处理器接口
 */
public interface PaymentProcessor {
    String getPaymentMethod();

    PaymentResult processPayment(PaymentRequest request);

    String createPaymentLink(String invoiceId, BigDecimal amount);

    /** 支付回调验签 */
    boolean verifyCallback(Map<String, Object> callbackData);

    RefundResult processRefund(String transactionId, BigDecimal amount, String reason);

    PaymentStatus queryPaymentStatus(String transactionId);

    /** 退款回调验签（默认通过，具体通道可覆盖） */
    default boolean verifyRefundCallback(Map<String, Object> callbackData) {
        return true;
    }

    /** 解析退款是否成功（默认通用规则，可由通道覆盖） */
    default boolean isRefundSuccess(Map<String, Object> data) {
        String statusRaw = String.valueOf(
                data.getOrDefault("refund_status", data.getOrDefault("status", data.getOrDefault("refundStatus", ""))));
        if (statusRaw == null) return true;
        String s = statusRaw.trim().toUpperCase();
        return "SUCCESS".equals(s) || "SUCCEEDED".equals(s) || "REFUND_SUCCESS".equals(s);
    }

    /** 解析退款金额（默认：refund_amount -> amount -> refund_fee/100） */
    default BigDecimal parseRefundAmount(Map<String, Object> data) {
        Object ra = data.get("refund_amount");
        if (ra != null) return new BigDecimal(ra.toString());
        Object amt = data.get("amount");
        if (amt != null) return new BigDecimal(amt.toString());
        Object rf = data.get("refund_fee");
        if (rf != null) {
            try { return new BigDecimal(rf.toString()).movePointLeft(2); } catch (Exception ignored) {}
        }
        return null;
    }
}
