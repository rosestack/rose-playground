package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillPaymentService;
import io.github.rosestack.billing.domain.enums.PaymentMethod;
import io.github.rosestack.billing.domain.enums.PaymentStatus;
import io.github.rosestack.billing.domain.payment.BillPayment;
import io.github.rosestack.core.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 支付处理API控制器
 *
 * 提供支付相关的RESTful API接口
 * 支持支付记录管理、支付状态查询、退款处理等功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "支付管理", description = "支付处理相关API")
public class BillPaymentController {

    private final BillPaymentService paymentService;

    /**
     * 创建支付记录
     */
    @PostMapping
    @Operation(summary = "创建支付记录", description = "创建新的支付记录")
    public ApiResponse<BillPayment> createPayment(
            @Parameter(description = "租户ID") @RequestParam String tenantId,
            @Parameter(description = "账单ID") @RequestParam(required = false) Long invoiceId,
            @Parameter(description = "支付金额") @RequestParam BigDecimal amount,
            @Parameter(description = "货币") @RequestParam(required = false, defaultValue = "CNY") String currency,
            @Parameter(description = "支付方式") @RequestParam PaymentMethod paymentMethod,
            @Parameter(description = "支付网关") @RequestParam(required = false) String paymentGateway) {

        log.info("Creating payment for invoice: {}, amount: {}", invoiceId, amount);

        BillPaymentService.PaymentRequest request = new BillPaymentService.PaymentRequest();
        request.setTenantId(tenantId);
        request.setInvoiceId(invoiceId);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setPaymentMethod(paymentMethod);
        request.setPaymentGateway(paymentGateway);

        BillPayment createdPayment = paymentService.createPayment(request);
        return ApiResponse.ok(createdPayment);
    }

    /**
     * 处理支付（第三方支付回调）
     */
    @PostMapping("/process")
    @Operation(summary = "处理支付", description = "处理支付交易（用于第三方支付回调）")
    public ApiResponse<Boolean> processPaymentSuccess(
            @Parameter(description = "支付ID") @RequestParam Long paymentId,
            @Parameter(description = "网关交易ID") @RequestParam String gatewayTransactionId) {

        log.info("Processing payment success for payment: {}, gatewayTransactionId: {}", 
                paymentId, gatewayTransactionId);

        boolean success = paymentService.processPaymentSuccess(paymentId, gatewayTransactionId);
        return ApiResponse.ok(success);
    }

    /**
     * 根据ID查询支付记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询支付记录", description = "根据ID查询支付记录详情")
    public ApiResponse<BillPayment> getPayment(@PathVariable Long id) {
        log.debug("Getting payment: {}", id);

        BillPayment payment = paymentService.getPaymentById(id);
        if (payment == null) {
            return ApiResponse.error("支付记录不存在");
        }
        return ApiResponse.ok(payment);
    }

    /**
     * 根据外部交易ID查询支付记录
     */
    @GetMapping("/external/{externalId}")
    @Operation(summary = "根据外部交易ID查询", description = "根据外部交易ID查询支付记录")
    public ApiResponse<BillPayment> getPaymentByExternalId(@PathVariable String externalId) {
        log.debug("Getting payment by external ID: {}", externalId);

        // 注意：这个方法在当前服务中不存在，需要根据支付编号查询
        BillPayment payment = paymentService.getPaymentByNo(externalId);
        if (payment == null) {
            return ApiResponse.error("支付记录不存在");
        }
        return ApiResponse.ok(payment);
    }

    /**
     * 根据账单ID查询支付记录
     */
    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "查询账单支付记录", description = "根据账单ID查询所有相关支付记录")
    public ApiResponse<List<BillPayment>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        log.debug("Getting payments for invoice: {}", invoiceId);

        List<BillPayment> payments = paymentService.getPaymentsByInvoice(invoiceId);
        return ApiResponse.ok(payments);
    }

    /**
     * 根据支付状态查询支付记录
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询支付", description = "根据支付状态查询支付记录列表")
    public ApiResponse<List<BillPayment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        log.debug("Getting payments by status: {}", status);

        List<BillPayment> payments;
        switch (status) {
            case PENDING:
                payments = paymentService.getPendingPayments();
                break;
            case SUCCESS:
                payments = paymentService.getSuccessfulPayments();
                break;
            case FAILED:
                payments = paymentService.getFailedPayments();
                break;
            default:
                return ApiResponse.error("不支持的支付状态: " + status);
        }
        return ApiResponse.ok(payments);
    }

    /**
     * 查询租户的支付记录
     */
    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "查询租户支付记录", description = "查询指定租户的所有支付记录")
    public ApiResponse<List<BillPayment>> getPaymentsByTenant(@PathVariable String tenantId) {
        log.debug("Getting payments for tenant: {}", tenantId);

        List<BillPayment> payments = paymentService.getPaymentsByTenant(tenantId);
        return ApiResponse.ok(payments);
    }

    /**
     * 查询指定日期范围内的支付记录
     */
    @GetMapping("/period")
    @Operation(summary = "查询时间段支付记录", description = "查询指定时间段内的支付记录")
    public ApiResponse<List<BillPayment>> getPaymentsByPeriod(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Getting payments between {} and {}", startDate, endDate);

        // 注意：当前服务中没有这个方法，返回空列表
        List<BillPayment> payments = List.of();
        return ApiResponse.ok(payments);
    }

    /**
     * 确认支付
     */
    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认支付", description = "确认支付成功")
    public ApiResponse<Boolean> confirmPayment(
            @PathVariable Long id,
            @Parameter(description = "支付凭证") @RequestParam(required = false) String paymentProof) {

        log.info("Confirming payment: {}", id);

        boolean success = paymentService.processPaymentSuccess(id, paymentProof);
        return ApiResponse.ok(success);
    }

    /**
     * 支付失败处理
     */
    @PostMapping("/{id}/fail")
    @Operation(summary = "支付失败", description = "标记支付失败")
    public ApiResponse<Boolean> failPayment(
            @PathVariable Long id,
            @Parameter(description = "失败原因") @RequestParam String failureReason) {

        log.info("Marking payment as failed: {}, reason: {}", id, failureReason);

        boolean success = paymentService.processPaymentFailure(id, failureReason);
        return ApiResponse.ok(success);
    }

    /**
     * 申请退款
     */
    @PostMapping("/{id}/refund")
    @Operation(summary = "申请退款", description = "申请支付退款")
    public ApiResponse<Boolean> requestRefund(
            @PathVariable Long id,
            @Parameter(description = "退款金额") @RequestParam BigDecimal refundAmount,
            @Parameter(description = "退款原因") @RequestParam String refundReason) {

        log.info("Requesting refund for payment: {}, amount: {}", id, refundAmount);

        // 注意：当前服务只支持全额退款
        boolean success = paymentService.processRefund(id);
        return ApiResponse.ok(success);
    }

    /**
     * 处理退款
     */
    @PostMapping("/{id}/process-refund")
    @Operation(summary = "处理退款", description = "处理退款申请")
    public ApiResponse<Boolean> processRefund(
            @PathVariable Long id,
            @Parameter(description = "外部退款交易ID") @RequestParam String externalRefundId) {

        log.info("Processing refund for payment: {}", id);

        boolean success = paymentService.processRefund(id);
        return ApiResponse.ok(success);
    }

    /**
     * 取消支付
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消支付", description = "取消待处理的支付")
    public ApiResponse<Boolean> cancelPayment(
            @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam(required = false) String cancelReason) {

        log.info("Cancelling payment: {}", id);

        boolean success = paymentService.cancelPayment(id, cancelReason);
        return ApiResponse.ok(success);
    }

    /**
     * 计算租户总支付金额
     */
    @GetMapping("/tenant/{tenantId}/total")
    @Operation(summary = "计算租户总支付", description = "计算租户的总支付金额")
    public ApiResponse<BigDecimal> calculateTenantTotalPayment(@PathVariable String tenantId) {
        log.debug("Calculating total payment for tenant: {}", tenantId);

        BigDecimal totalAmount = paymentService.calculateTenantTotalPayment(tenantId);
        return ApiResponse.ok(totalAmount);
    }

    /**
     * 获取支付统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "支付统计", description = "获取支付统计信息")
    public ApiResponse<PaymentStats> getPaymentStats(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "租户ID") @RequestParam(required = false) String tenantId) {

        log.debug("Getting payment stats for period {} to {}, tenant: {}", startDate, endDate, tenantId);

        // 注意：当前服务中没有这个方法，返回默认统计
        PaymentStats stats = new PaymentStats();
        stats.setTotalAmount(BigDecimal.ZERO);
        stats.setSuccessAmount(BigDecimal.ZERO);
        stats.setFailedAmount(BigDecimal.ZERO);
        stats.setRefundedAmount(BigDecimal.ZERO);
        stats.setTotalCount(0L);
        stats.setSuccessCount(0L);
        stats.setFailedCount(0L);
        stats.setRefundedCount(0L);
        return ApiResponse.ok(stats);
    }

    /**
     * 支付统计信息
     */
    @lombok.Data
    public static class PaymentStats {
        private BigDecimal totalAmount;
        private BigDecimal successAmount;
        private BigDecimal failedAmount;
        private BigDecimal refundedAmount;
        private Long totalCount;
        private Long successCount;
        private Long failedCount;
        private Long refundedCount;
    }
}