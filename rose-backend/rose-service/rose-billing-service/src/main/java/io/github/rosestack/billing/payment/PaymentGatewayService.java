package io.github.rosestack.billing.payment;

import io.github.rosestack.billing.dto.PaymentRequest;
import io.github.rosestack.billing.dto.PaymentResult;
import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.billing.enums.PaymentStatus;
import io.github.rosestack.billing.payment.processor.AlipayPaymentProcessor;
import io.github.rosestack.billing.payment.processor.StripePaymentProcessor;
import io.github.rosestack.billing.payment.processor.WechatPaymentProcessor;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 支付网关服务
 * 集成多种支付方式
 *
 * @author rose
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final StripePaymentProcessor stripeProcessor;
    private final AlipayPaymentProcessor alipayProcessor;
    private final WechatPaymentProcessor wechatProcessor;
    private final PaymentRecordRepository paymentRecordRepository;

    /**
     * 处理支付
     */
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            PaymentProcessor processor = getPaymentProcessor(request.getPaymentMethod());
            PaymentResult result = processor.processPayment(request);

            // 记录支付结果
            recordPaymentResult(request, result);

            return result;
        } catch (Exception e) {
            log.error("支付处理失败：{}", request.getInvoiceId(), e);
            return PaymentResult.failed("支付处理失败：" + e.getMessage());
        }
    }

    /**
     * 创建支付链接
     */
    public String createPaymentLink(String invoiceId, BigDecimal amount, String paymentMethod, String tenantId) {
        try {
            PaymentProcessor processor = getPaymentProcessor(paymentMethod);
            String paymentLink = processor.createPaymentLink(invoiceId, amount);

            log.info("创建支付链接成功：租户 {}, 账单 {}, 金额 {}", tenantId, invoiceId, amount);
            return paymentLink;
        } catch (Exception e) {
            log.error("创建支付链接失败：{}", invoiceId, e);
            throw new RuntimeException("创建支付链接失败", e);
        }
    }

    /**
     * 验证支付回调
     */
    public boolean verifyPaymentCallback(String paymentMethod, Map<String, Object> callbackData) {
        try {
            PaymentProcessor processor = getPaymentProcessor(paymentMethod);
            boolean isValid = processor.verifyCallback(callbackData);

            if (isValid) {
                log.info("支付回调验证成功：{}", paymentMethod);
            } else {
                log.warn("支付回调验证失败：{}", paymentMethod);
            }

            return isValid;
        } catch (Exception e) {
            log.error("支付回调验证失败：{}", paymentMethod, e);
            return false;
        }
    }

    /**
     * 处理退款
     */
    public RefundResult processRefund(String transactionId, BigDecimal amount, String reason, String tenantId) {
        try {
            // 根据交易ID查找支付方式
            String paymentMethod = getPaymentMethodByTransactionId(transactionId);
            PaymentProcessor processor = getPaymentProcessor(paymentMethod);
            RefundResult result = processor.processRefund(transactionId, amount, reason);

            // 记录退款结果
            recordRefundResult(transactionId, amount, reason, result, tenantId);

            return result;
        } catch (Exception e) {
            log.error("退款处理失败：{}", transactionId, e);
            return RefundResult.failed("退款处理失败：" + e.getMessage());
        }
    }

    /**
     * 查询支付状态
     */
    public PaymentStatus queryPaymentStatus(String transactionId) {
        try {
            String paymentMethod = getPaymentMethodByTransactionId(transactionId);
            PaymentProcessor processor = getPaymentProcessor(paymentMethod);
            return processor.queryPaymentStatus(transactionId);
        } catch (Exception e) {
            log.error("查询支付状态失败：{}", transactionId, e);
            return PaymentStatus.UNKNOWN;
        }
    }

    private PaymentProcessor getPaymentProcessor(String paymentMethod) {
        return switch (paymentMethod.toUpperCase()) {
            case "STRIPE", "CREDIT_CARD" -> stripeProcessor;
            case "ALIPAY" -> alipayProcessor;
            case "WECHAT", "WECHAT_PAY" -> wechatProcessor;
            default -> throw new IllegalArgumentException("不支持的支付方式：" + paymentMethod);
        };
    }

    private String getPaymentMethodByTransactionId(String transactionId) {
        // 从数据库查找支付方式
        Optional<PaymentRecord> record = paymentRecordRepository.findByTransactionId(transactionId);
        return record.map(PaymentRecord::getPaymentMethod).orElse("STRIPE");
    }

    private void recordPaymentResult(PaymentRequest request, PaymentResult result) {
        try {
            PaymentRecord record = new PaymentRecord();
            record.setInvoiceId(request.getInvoiceId());
            record.setTenantId(request.getTenantId());
            record.setAmount(request.getAmount());
            record.setPaymentMethod(request.getPaymentMethod());
            record.setTransactionId(result.getTransactionId());
            record.setStatus(result.isSuccess() ? PaymentRecordStatus.SUCCESS : PaymentRecordStatus.FAILED);
            record.setGatewayResponse(result.getGatewayResponse());

            if (result.isSuccess()) {
                record.setPaidAt(LocalDateTime.now());
            }

            paymentRecordRepository.save(record);
        } catch (Exception e) {
            log.error("记录支付结果失败", e);
        }
    }

    private void recordRefundResult(String transactionId, BigDecimal amount, String reason,
                                    RefundResult result, String tenantId) {
        try {
            // 更新原支付记录的退款信息
            Optional<PaymentRecord> optionalRecord = paymentRecordRepository.findByTransactionId(transactionId);
            if (optionalRecord.isPresent() && result.isSuccess()) {
                PaymentRecord record = optionalRecord.get();
                record.setStatus(PaymentRecordStatus.REFUNDED);
                record.setRefundedAt(LocalDateTime.now());
                record.setRefundReason(reason);
                record.setRefundId(result.getRefundId());

                paymentRecordRepository.save(record);
            }

            log.info("记录退款结果：租户 {}, 交易 {}, 金额 {}, 结果 {}",
                    tenantId, transactionId, amount, result.isSuccess());
        } catch (Exception e) {
            log.error("记录退款结果失败", e);
        }
    }
}
