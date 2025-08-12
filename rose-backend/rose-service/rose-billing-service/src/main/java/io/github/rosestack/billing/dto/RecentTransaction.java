package io.github.rosestack.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 最近交易记录
 */
@Data
public class RecentTransaction {
    private String invoiceId;
    private String tenantId;
    private String tenantName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidTime;
}
