package io.github.rosestack.billing.controller;

import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.service.RefundService;
import io.github.rosestack.core.model.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/billing/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final io.github.rosestack.billing.payment.PaymentGatewayService paymentGatewayService;

    @PostMapping
    public ApiResponse<?> requestRefund(@RequestBody RefundRequest request,
                                        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (request == null || request.getInvoiceId() == null || request.getInvoiceId().isBlank()) {
            return ApiResponse.error("invoiceId 不能为空");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponse.error("退款金额必须大于0");
        }
        RefundResult result = refundService.requestRefund(request.getInvoiceId(), request.getAmount(), request.getReason(), idempotencyKey);
        return result.isSuccess() ? ApiResponse.success(result) : ApiResponse.error(result.getErrorMessage());
    }

    /** 退款回调入口（轻量版） */
    @PostMapping("/callback/{paymentMethod}")
    public ApiResponse<Void> handleRefundCallback(@PathVariable String paymentMethod,
                                                  @RequestBody Map<String, Object> callbackData) {
        boolean ok = paymentGatewayService.verifyRefundCallback(paymentMethod, callbackData);
        if (!ok) return ApiResponse.error("invalid refund callback");
        boolean updated = refundService.processRefundCallback(paymentMethod, callbackData);
        return updated ? ApiResponse.success() : ApiResponse.error("callback update failed");
    }

    @Data
    public static class RefundRequest {
        private String invoiceId;
        private BigDecimal amount;
        private String reason;
    }
}

