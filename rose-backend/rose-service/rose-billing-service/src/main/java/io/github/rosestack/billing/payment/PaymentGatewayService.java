package io.github.rosestack.billing.payment;

import io.github.rosestack.billing.dto.PaymentRequest;
import io.github.rosestack.billing.dto.PaymentResult;
import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付网关服务 集成多种支付方式
 *
 * @author rose
 */
@Slf4j
@Service
public class PaymentGatewayService {
    private final Map<String, PaymentProcessor> paymentProcessorMap;

    private final PaymentRecordRepository paymentRecordRepository;

    public PaymentGatewayService(
            List<PaymentProcessor> paymentProcessors, PaymentRecordRepository paymentRecordRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentProcessorMap = new HashMap<>();

        paymentProcessors.forEach(processor -> this.paymentProcessorMap.put(processor.getPaymentMethod(), processor));
    }

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

    // Overloads using enum for type safety
    public String createPaymentLink(String invoiceId, BigDecimal amount, PaymentMethod method, String tenantId) {
        return createPaymentLink(invoiceId, amount, method == null ? null : method.name(), tenantId);
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

    public boolean verifyPaymentCallback(PaymentMethod method, Map<String, Object> callbackData) {
        return verifyPaymentCallback(method == null ? null : method.name(), callbackData);
    }

    /**
     * 验证退款回调
     */
    public boolean verifyRefundCallback(String paymentMethod, Map<String, Object> callbackData) {
        try {
            PaymentProcessor processor = getPaymentProcessor(paymentMethod);
            boolean isValid = processor.verifyRefundCallback(callbackData);
            if (!isValid) log.warn("退款回调验证失败：{}", paymentMethod);
            return isValid;
        } catch (Exception e) {
            log.error("退款回调验证失败：{}", paymentMethod, e);
            return false;
        }
    }

    public boolean verifyRefundCallback(PaymentMethod method, Map<String, Object> callbackData) {
        return verifyRefundCallback(method == null ? null : method.name(), callbackData);
    }

    /**
     * 解析退款金额（回调）
     */
    public BigDecimal parseRefundAmount(String paymentMethod, Map<String, Object> data) {
        PaymentProcessor processor = getPaymentProcessor(paymentMethod);
        if (processor != null) {
            try {
                return processor.parseRefundAmount(data);
            } catch (Exception ignored) {
            }
        }
        Object ra = data.get("refund_amount");
        if (ra != null) return new BigDecimal(ra.toString());
        Object amt = data.get("amount");
        if (amt != null) return new BigDecimal(amt.toString());
        Object rf = data.get("refund_fee");
        if (rf != null) {
            try {
                return new BigDecimal(rf.toString()).movePointLeft(2);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 判断退款是否成功（回调）
     */
    public boolean isRefundSuccess(String paymentMethod, Map<String, Object> data) {
        PaymentProcessor processor = getPaymentProcessor(paymentMethod);
        if (processor != null) {
            try {
                return processor.isRefundSuccess(data);
            } catch (Exception ignored) {
            }
        }
        String statusRaw = String.valueOf(
                data.getOrDefault("refund_status", data.getOrDefault("status", data.getOrDefault("refundStatus", ""))));
        String s = statusRaw == null ? "" : statusRaw.trim().toUpperCase();
        switch (paymentMethod.toUpperCase()) {
            case "ALIPAY":
                return "REFUND_SUCCESS".equals(s) || "SUCCESS".equals(s);
            case "WECHAT":
                return "SUCCESS".equals(s);
            case "STRIPE":
                return "SUCCEEDED".equals(s) || "SUCCESS".equals(s);
            default:
                return "SUCCESS".equals(s) || "SUCCEEDED".equals(s) || "REFUND_SUCCESS".equals(s);
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
        return paymentProcessorMap.get(paymentMethod);
    }

    private PaymentProcessor getPaymentProcessor(PaymentMethod method) {
        if (method == null) return null;
        return getPaymentProcessor(method.name());
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
            record.setPaymentMethod(
                    request.getPaymentMethod() == null
                            ? null
                            : (request.getPaymentMethod() instanceof PaymentMethod
                                    ? ((PaymentMethod) request.getPaymentMethod()).name()
                                    : request.getPaymentMethod().toString()));
            record.setTransactionId(result.getTransactionId());
            record.setStatus(result.isSuccess() ? PaymentRecordStatus.SUCCESS : PaymentRecordStatus.PENDING);
            record.setGatewayResponse(result.getGatewayResponse());

            paymentRecordRepository.insert(record);

            if (result.isSuccess()) {
                record.setStatus(PaymentRecordStatus.SUCCESS);
                record.setPaidTime(LocalDateTime.now());
                paymentRecordRepository.updateById(record);
            } else if (!result.isSuccess()) {
                record.setStatus(PaymentRecordStatus.FAILED);
                paymentRecordRepository.updateById(record);
            }
        } catch (Exception e) {
            log.error("记录支付结果失败", e);
        }
    }

    private void recordRefundResult(
            String transactionId, BigDecimal amount, String reason, RefundResult result, String tenantId) {
        try {
            // 更新原支付记录的退款信息
            Optional<PaymentRecord> optionalRecord = paymentRecordRepository.findByTransactionId(transactionId);
            if (optionalRecord.isPresent() && result.isSuccess()) {
                PaymentRecord record = optionalRecord.get();
                record.setStatus(PaymentRecordStatus.REFUNDED);
                record.setRefundedTime(LocalDateTime.now());
                record.setRefundReason(reason);
                record.setRefundId(result.getRefundId());

                paymentRecordRepository.updateById(record);
            }

            log.info("记录退款结果：租户 {}, 交易 {}, 金额 {}, 结果 {}", tenantId, transactionId, amount, result.isSuccess());
        } catch (Exception e) {
            log.error("记录退款结果失败", e);
        }
    }
}
