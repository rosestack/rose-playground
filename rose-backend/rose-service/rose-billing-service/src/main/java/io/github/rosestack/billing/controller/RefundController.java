package io.github.rosestack.billing.controller;

import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.payment.PaymentGatewayService;
import io.github.rosestack.billing.service.RefundService;
import io.github.rosestack.core.model.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/billing/refund")
@Validated
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final PaymentGatewayService paymentGatewayService;

    @PostMapping
    public ApiResponse<?> requestRefund(
            @RequestBody @Valid RefundRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        RefundResult result = refundService.requestRefund(
                request.getInvoiceId(), request.getAmount(), request.getReason(), idempotencyKey);
        return result.isSuccess() ? ApiResponse.success(result) : ApiResponse.error(result.getErrorMessage());
    }

    /**
     * 退款回调入口（轻量版）
     */
    @PostMapping("/callback/{paymentMethod}")
    public ApiResponse<Void> handleRefundCallback(
            @PathVariable String paymentMethod, @RequestBody Map<String, Object> callbackData) {
        io.github.rosestack.billing.payment.PaymentMethod pm;
        try {
            pm = io.github.rosestack.billing.payment.PaymentMethod.valueOf(paymentMethod);
        } catch (Exception e) {
            return ApiResponse.error("invalid payment method: " + paymentMethod);
        }
        boolean ok = paymentGatewayService.verifyRefundCallback(pm, callbackData);
        if (!ok)
            return ApiResponse.error(
                    io.github.rosestack.billing.enums.BillingErrorCode.INVALID_REFUND_CALLBACK.getCode(),
                    io.github.rosestack.billing.enums.BillingErrorCode.INVALID_REFUND_CALLBACK.getMessage());
        boolean updated = refundService.processRefundCallback(paymentMethod, callbackData);
        return updated
                ? ApiResponse.success()
                : ApiResponse.error(
                io.github.rosestack.billing.enums.BillingErrorCode.CALLBACK_UPDATE_FAILED.getCode(),
                io.github.rosestack.billing.enums.BillingErrorCode.CALLBACK_UPDATE_FAILED.getMessage());
    }

    @Data
    public static class RefundRequest {
        @NotBlank
        private String invoiceId;

        @NotNull
        @DecimalMin(value = "0.01", message = "退款金额必须大于0")
        private BigDecimal amount;

        private String reason;
    }
}
