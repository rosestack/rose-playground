package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.PaymentMethod;
import io.github.rosestack.billing.domain.enums.PaymentStatus;
import io.github.rosestack.billing.domain.invoice.BillInvoice;
import io.github.rosestack.billing.domain.invoice.BillInvoiceMapper;
import io.github.rosestack.billing.domain.payment.BillPayment;
import io.github.rosestack.billing.domain.payment.BillPaymentMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 支付处理服务
 *
 * 提供支付的创建、处理、查询、退款等核心功能
 * 支持多种支付方式和支付网关集成
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillPaymentService {

    private final BillPaymentMapper paymentMapper;
    private final BillInvoiceMapper invoiceMapper;

    /**
     * 创建支付记录
     */
    @Transactional
    public BillPayment createPayment(PaymentRequest request) {
        log.info("创建支付记录: invoiceId={}, amount={}, method={}",
                request.getInvoiceId(), request.getAmount(), request.getPaymentMethod());

        // 验证请求参数
        validatePaymentRequest(request);

        // 如果有账单ID，验证账单状态
        if (request.getInvoiceId() != null) {
            BillInvoice invoice = invoiceMapper.selectById(request.getInvoiceId());
            if (invoice == null) {
                throw new IllegalArgumentException("账单不存在: " + request.getInvoiceId());
            }

            if (!invoice.canBePaid()) {
                throw new IllegalStateException("账单状态不允许支付: " + invoice.getStatus());
            }

            // 检查支付金额是否超过未支付金额
            if (request.getAmount().compareTo(invoice.getUnpaidAmount()) > 0) {
                throw new IllegalArgumentException("支付金额不能超过未支付金额");
            }
        }

        // 生成支付编号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        BillPayment payment = new BillPayment();
        payment.setPaymentNo(paymentNo);
        payment.setTenantId(request.getTenantId());
        payment.setInvoiceId(request.getInvoiceId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentGateway(request.getPaymentGateway());
        payment.setStatus(PaymentStatus.PENDING);

        paymentMapper.insert(payment);

        log.info("支付记录创建成功: id={}, paymentNo={}", payment.getId(), paymentNo);
        return payment;
    }

    /**
     * 处理支付成功
     */
    @Transactional
    public boolean processPaymentSuccess(Long paymentId, String gatewayTransactionId) {
        log.info("处理支付成功: paymentId={}, gatewayTransactionId={}", paymentId, gatewayTransactionId);

        BillPayment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            log.error("支付记录不存在: {}", paymentId);
            return false;
        }

        if (!payment.isPending()) {
            log.warn("支付记录状态不是处理中，无法标记为成功: {} - {}", paymentId, payment.getStatus());
            return false;
        }

        // 更新支付记录状态
        payment.markAsSuccessful(gatewayTransactionId);
        paymentMapper.updateById(payment);

        // 更新账单支付状态
        if (payment.getInvoiceId() != null) {
            updateInvoicePaymentStatus(payment.getInvoiceId());
        }

        log.info("支付成功处理完成: paymentId={}", paymentId);
        return true;
    }

    /**
     * 处理支付失败
     */
    @Transactional
    public boolean processPaymentFailure(Long paymentId, String failureReason) {
        log.info("处理支付失败: paymentId={}, reason={}", paymentId, failureReason);

        BillPayment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            log.error("支付记录不存在: {}", paymentId);
            return false;
        }

        if (!payment.isPending()) {
            log.warn("支付记录状态不是处理中，无法标记为失败: {} - {}", paymentId, payment.getStatus());
            return false;
        }

        // 更新支付记录状态
        payment.markAsFailed(failureReason);
        paymentMapper.updateById(payment);

        log.info("支付失败处理完成: paymentId={}", paymentId);
        return true;
    }

    /**
     * 取消支付
     */
    @Transactional
    public boolean cancelPayment(Long paymentId, String reason) {
        log.info("取消支付: paymentId={}, reason={}", paymentId, reason);

        BillPayment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            log.error("支付记录不存在: {}", paymentId);
            return false;
        }

        if (!payment.isPending()) {
            log.warn("支付记录状态不是处理中，无法取消: {} - {}", paymentId, payment.getStatus());
            return false;
        }

        // 更新支付记录状态
        payment.markAsCancelled(reason);
        paymentMapper.updateById(payment);

        log.info("支付取消完成: paymentId={}", paymentId);
        return true;
    }

    /**
     * 处理退款
     */
    @Transactional
    public boolean processRefund(Long paymentId) {
        log.info("处理退款: paymentId={}", paymentId);

        BillPayment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            log.error("支付记录不存在: {}", paymentId);
            return false;
        }

        if (!payment.canRefund()) {
            log.warn("支付记录状态不允许退款: {} - {}", paymentId, payment.getStatus());
            return false;
        }

        // 更新支付记录状态
        payment.markAsRefunded();
        paymentMapper.updateById(payment);

        // 更新账单支付状态
        if (payment.getInvoiceId() != null) {
            updateInvoicePaymentStatus(payment.getInvoiceId());
        }

        log.info("退款处理完成: paymentId={}", paymentId);
        return true;
    }

    /**
     * 根据支付编号查询支付记录
     */
    public BillPayment getPaymentByNo(String paymentNo) {
        if (paymentNo == null || paymentNo.trim().isEmpty()) {
            return null;
        }
        return paymentMapper.findByPaymentNo(paymentNo);
    }

    /**
     * 根据ID查询支付记录
     */
    public BillPayment getPaymentById(Long paymentId) {
        if (paymentId == null) {
            return null;
        }
        return paymentMapper.selectById(paymentId);
    }

    /**
     * 查询账单的所有支付记录
     */
    public List<BillPayment> getPaymentsByInvoice(Long invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("账单ID不能为空");
        }
        return paymentMapper.findByInvoiceId(invoiceId);
    }

    /**
     * 查询租户的支付记录
     */
    public List<BillPayment> getPaymentsByTenant(String tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        return paymentMapper.findByTenantId(tenantId);
    }

    /**
     * 查询成功的支付记录
     */
    public List<BillPayment> getSuccessfulPayments() {
        return paymentMapper.findSuccessfulPayments();
    }

    /**
     * 查询失败的支付记录
     */
    public List<BillPayment> getFailedPayments() {
        return paymentMapper.findFailedPayments();
    }

    /**
     * 查询处理中的支付记录
     */
    public List<BillPayment> getPendingPayments() {
        return paymentMapper.findPendingPayments();
    }

    /**
     * 计算账单已支付金额
     */
    public BigDecimal calculatePaidAmount(Long invoiceId) {
        if (invoiceId == null) {
            return BigDecimal.ZERO;
        }
        return paymentMapper.sumPaidAmountByInvoice(invoiceId);
    }

    /**
     * 计算租户总支付金额
     */
    public BigDecimal calculateTenantTotalPayment(String tenantId) {
        if (tenantId == null) {
            return BigDecimal.ZERO;
        }
        return paymentMapper.sumPaidAmountByTenant(tenantId);
    }

    /**
     * 查询超时的待处理支付
     */
    public List<BillPayment> getTimeoutPendingPayments(int timeoutMinutes) {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return paymentMapper.findTimeoutPendingPayments(timeoutThreshold);
    }

    /**
     * 批量处理超时支付
     */
    @Transactional
    public int processTimeoutPayments(int timeoutMinutes) {
        log.info("批量处理超时支付，超时时间: {} 分钟", timeoutMinutes);

        List<BillPayment> timeoutPayments = getTimeoutPendingPayments(timeoutMinutes);
        int processedCount = 0;

        for (BillPayment payment : timeoutPayments) {
            try {
                if (processPaymentFailure(payment.getId(), "支付超时")) {
                    processedCount++;
                }
            } catch (Exception e) {
                log.error("处理超时支付失败: paymentId={}", payment.getId(), e);
            }
        }

        log.info("批量处理超时支付完成，处理数量: {}", processedCount);
        return processedCount;
    }

    /**
     * 验证支付请求
     */
    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("支付请求不能为空");
        }

        if (request.getTenantId() == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("支付方式不能为空");
        }
    }

    /**
     * 生成支付编号
     */
    private String generatePaymentNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        return "PAY" + timestamp + uuid.toUpperCase();
    }

    /**
     * 更新账单支付状态
     */
    private void updateInvoicePaymentStatus(Long invoiceId) {
        BillInvoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            log.warn("账单不存在，无法更新支付状态: {}", invoiceId);
            return;
        }

        // 计算已支付金额
        BigDecimal paidAmount = calculatePaidAmount(invoiceId);
        BigDecimal unpaidAmount = invoice.getTotalAmount().subtract(paidAmount);

        // 更新账单支付状态
        invoice.updatePaymentStatus(paidAmount, unpaidAmount);
        invoiceMapper.updateById(invoice);

        log.debug("账单支付状态已更新: invoiceId={}, paidAmount={}, unpaidAmount={}, status={}",
                invoiceId, paidAmount, unpaidAmount, invoice.getStatus());
    }

    /**
     * 支付请求
     */
	@Data
    public static class PaymentRequest {
        private String tenantId;
        private Long invoiceId;
        private BigDecimal amount;
        private String currency;
        private PaymentMethod paymentMethod;
        private String paymentGateway;

    }
}
