package io.github.rosestack.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map; /**
 * 支付请求对象
 */
@Data
public class PaymentRequest {
    private String invoiceId;
    private String tenantId;
    private BigDecimal amount;
    private String paymentMethod;
    private Map<String, Object> paymentData;
}
