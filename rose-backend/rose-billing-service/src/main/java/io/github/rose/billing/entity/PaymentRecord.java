package io.github.rose.billing.entity;

import io.github.rose.billing.enums.PaymentRecordStatus;
import io.github.rose.core.entity.BaseTenantEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付记录实体
 */
@Data
public class PaymentRecord extends BaseTenantEntity{
    private String id;

    private String invoiceId;
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
