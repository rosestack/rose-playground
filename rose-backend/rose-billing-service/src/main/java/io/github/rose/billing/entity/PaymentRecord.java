package io.github.rose.billing.entity;

import io.github.rose.billing.enums.PaymentRecordStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付记录实体
 */
@Data
public class PaymentRecord {
    private String invoiceId;
    private String tenantId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private PaymentRecordStatus status;
    private Map<String, Object> gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String refundReason;
    private String refundId;

    // getters and setters
}
