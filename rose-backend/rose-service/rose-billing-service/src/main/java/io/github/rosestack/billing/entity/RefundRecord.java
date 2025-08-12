package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.enums.RefundStatus;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_record")
public class RefundRecord extends BaseTenantEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String invoiceId;
    private String paymentMethod;
    private String transactionId; // 原支付交易号
    private String refundId; // 通道退款单号
    private String currency; // 币种（ISO 代码）

    private String idempotencyKey; // 幂等键
    private BigDecimal refundAmount;
    private String reason;
    private RefundStatus status;
    private String rawCallback; // 通道回调原文
    private LocalDateTime requestedTime;
    private LocalDateTime completedTime;
}
