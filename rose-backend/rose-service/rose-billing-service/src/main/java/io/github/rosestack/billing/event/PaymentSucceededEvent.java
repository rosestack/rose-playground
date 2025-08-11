package io.github.rosestack.billing.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSucceededEvent {
    private final String tenantId;
    private final String invoiceId;
    private final String transactionId;
    private final String paymentMethod;
    private final BigDecimal amount;
    private final LocalDateTime occurredTime;

    public PaymentSucceededEvent(String tenantId, String invoiceId, String transactionId,
                                 String paymentMethod, BigDecimal amount, LocalDateTime occurredTime) {
        this.tenantId = tenantId;
        this.invoiceId = invoiceId;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.occurredTime = occurredTime;
    }

    public String getTenantId() { return tenantId; }
    public String getInvoiceId() { return invoiceId; }
    public String getTransactionId() { return transactionId; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getOccurredTime() { return occurredTime; }
}

