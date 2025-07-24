package io.github.rose.billing.payment;


import io.github.rose.billing.dto.PaymentRequest;
import io.github.rose.billing.dto.PaymentResult;
import io.github.rose.billing.dto.RefundResult;
import io.github.rose.billing.enums.PaymentStatus;

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
