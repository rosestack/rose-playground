package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseTenantEntity{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
}
