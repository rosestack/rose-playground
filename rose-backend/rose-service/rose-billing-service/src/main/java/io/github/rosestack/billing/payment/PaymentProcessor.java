package io.github.rosestack.billing.payment;


import io.github.rosestack.billing.dto.PaymentRequest;
import io.github.rosestack.billing.dto.PaymentResult;
import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付处理器接口
 */
public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest request);

    String createPaymentLink(String invoiceId, BigDecimal amount);

    boolean verifyCallback(Map<String, Object> callbackData);

    RefundResult processRefund(String transactionId, BigDecimal amount, String reason);

    PaymentStatus queryPaymentStatus(String transactionId);
}
