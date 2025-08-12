package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseTenantEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String invoiceId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private PaymentRecordStatus status;

    // 通道回执相关
    private String channelStatus; // SUCCESS/FAILED/PENDING 等
    private BigDecimal channelAmount; // 通道确认金额

    // 金额与币种
    private String currency; // ISO 货币代码
    private BigDecimal feeAmount; // 通道费用
    private BigDecimal netAmount; // 净额=amount-fee

    // 入账标记
    private Boolean posted; // 是否已记总账/完成账务入账
    private LocalDateTime postedTime;

    private Map<String, Object> gatewayResponse;
    private LocalDateTime paidTime;
    private LocalDateTime refundedTime;
    private String refundReason;
    private String refundId;
}
