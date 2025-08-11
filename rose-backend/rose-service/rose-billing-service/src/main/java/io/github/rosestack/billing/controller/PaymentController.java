package io.github.rosestack.billing.controller;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.payment.PaymentStatus;
import io.github.rosestack.billing.payment.PaymentGatewayService;
import io.github.rosestack.billing.service.BillingService;
import io.github.rosestack.billing.service.InvoiceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/billing/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;
    private final BillingService billingService;
    private final InvoiceService invoiceService;

    /**
     * 为账单创建支付链接
     */
    @PostMapping("/invoices/{invoiceId}/link")
    public ApiResponse<String> createPaymentLink(@PathVariable String invoiceId,
                                                 @RequestBody CreatePaymentLinkRequest request) {
        Invoice invoice = invoiceService.getInvoiceDetails(invoiceId);
        BigDecimal amount = invoice.getTotalAmount();
        String link = paymentGatewayService.createPaymentLink(
                invoiceId, amount, request.getPaymentMethod(), invoice.getTenantId());
        return ApiResponse.success(link);
    }

    /**
     * 支付回调入口（统一回调）
     * paymentMethod 例如：ALIPAY、WECHAT、STRIPE
     */
    @PostMapping("/callback/{paymentMethod}")
    public ApiResponse<Void> handleCallback(@PathVariable String paymentMethod,
                                            @RequestBody Map<String, Object> callbackData) {
        log.info("收到支付回调：method={}, data={}", paymentMethod, callbackData);
        boolean ok = paymentGatewayService.verifyPaymentCallback(paymentMethod, callbackData);
        if (!ok) {
            return ApiResponse.error("invalid callback");
        }
        String invoiceId = extractInvoiceId(paymentMethod, callbackData);
        String transactionId = extractTransactionId(paymentMethod, callbackData);
        billingService.processPayment(invoiceId, paymentMethod, transactionId);
        return ApiResponse.success();
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{transactionId}")
    public ApiResponse<PaymentStatus> queryStatus(@PathVariable String transactionId) {
        return ApiResponse.success(paymentGatewayService.queryPaymentStatus(transactionId));
    }

    private String extractInvoiceId(String method, Map<String, Object> data) {
        // 通用字段优先
        Object v = data.getOrDefault("invoiceId",
                data.getOrDefault("invoice_id",
                        data.getOrDefault("out_trade_no",
                                data.getOrDefault("client_reference_id",
                                        data.getOrDefault("invoice", "")))));
        String s = v == null ? "" : v.toString();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("invoiceId not found in callback: " + method);
        }
        return s;
    }

    private String extractTransactionId(String method, Map<String, Object> data) {
        Object v = data.getOrDefault("transactionId",
                data.getOrDefault("transaction_id",
                        data.getOrDefault("trade_no",
                                data.getOrDefault("payment_intent_id",
                                        data.getOrDefault("id", "")))));
        String s = v == null ? "" : v.toString();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("transactionId not found in callback: " + method);
        }
        return s;
    }

    @Data
    static class CreatePaymentLinkRequest {
        private String paymentMethod;
    }
}

