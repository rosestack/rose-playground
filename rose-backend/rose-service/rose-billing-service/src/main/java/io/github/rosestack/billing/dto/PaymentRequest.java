package io.github.rosestack.billing.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

/**
 * 支付请求对象
 */
@Data
public class PaymentRequest {
    private String invoiceId;
    private String tenantId;
    private BigDecimal amount;
    private io.github.rosestack.billing.payment.PaymentMethod paymentMethod;
    private Map<String, Object> paymentData;
}
