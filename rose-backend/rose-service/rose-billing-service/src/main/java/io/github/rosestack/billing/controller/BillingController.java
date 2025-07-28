package io.github.rosestack.billing.controller;

import io.github.rosestack.billing.entity.BaseTenantSubscription;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.service.BillingService;
import io.github.rosestack.core.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 计费管理控制器
 *
 * @author rose
 */
@Slf4j
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /**
     * 创建订阅
     */
    @PostMapping("/subscriptions")
    public ApiResponse<BaseTenantSubscription> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        BaseTenantSubscription subscription = billingService.createSubscription(
                request.getTenantId(),
                request.getPlanId(),
                request.getStartTrial()
        );
        return ApiResponse.success(subscription);
    }

    /**
     * 获取租户订阅信息
     */
    @GetMapping("/subscriptions/tenant/{tenantId}")
    public ApiResponse<BaseTenantSubscription> getTenantSubscription(@PathVariable String tenantId) {
        BaseTenantSubscription subscription = billingService.getTenantSubscription(tenantId);
        return ApiResponse.success(subscription);
    }

    /**
     * 升级/降级订阅计划
     */
    @PutMapping("/subscriptions/{subscriptionId}/plan")
    public ApiResponse<BaseTenantSubscription> changePlan(
            @PathVariable String subscriptionId,
            @Valid @RequestBody ChangePlanRequest request) {
        BaseTenantSubscription subscription = billingService.changePlan(subscriptionId, request.getNewPlanId());
        return ApiResponse.success(subscription);
    }

    /**
     * 取消订阅
     */
    @PutMapping("/subscriptions/{subscriptionId}/cancel")
    public ApiResponse<Void> cancelSubscription(
            @PathVariable String subscriptionId,
            @RequestBody CancelSubscriptionRequest request) {
        billingService.cancelSubscription(subscriptionId, request.getReason());
        return ApiResponse.success();
    }

    /**
     * 记录使用量
     */
    @PostMapping("/usage")
    public ApiResponse<Void> recordUsage(@Valid @RequestBody RecordUsageRequest request) {
        billingService.recordUsage(
                request.getTenantId(),
                request.getMetricType(),
                request.getQuantity(),
                request.getResourceId(),
                request.getMetadata()
        );
        return ApiResponse.success();
    }

    /**
     * 获取使用量统计
     */
    @GetMapping("/usage/tenant/{tenantId}")
    public ApiResponse<List<Map<String, Object>>> getUsageStats(
            @PathVariable String tenantId,
            @RequestParam String metricType,
            @RequestParam String period) {
        List<Map<String, Object>> usage = billingService.getUsageStats(tenantId, period);
        return ApiResponse.success(usage);
    }

    /**
     * 检查使用量限制
     */
    @GetMapping("/usage/tenant/{tenantId}/limit-check")
    public ApiResponse<Boolean> checkUsageLimit(
            @PathVariable String tenantId,
            @RequestParam String metricType) {
        boolean withinLimit = billingService.checkUsageLimit(tenantId, metricType);
        return ApiResponse.success(withinLimit);
    }

    /**
     * 生成账单
     */
    @PostMapping("/invoices/generate")
    public ApiResponse<Invoice> generateInvoice(@Valid @RequestBody GenerateInvoiceRequest request) {
        Invoice invoice = billingService.generateInvoice(request.getSubscriptionId());
        return ApiResponse.success(invoice);
    }

    /**
     * 获取租户账单列表
     */
    @GetMapping("/invoices/tenant/{tenantId}")
    public ApiResponse<List<Invoice>> getTenantInvoices(@PathVariable String tenantId) {
        List<Invoice> invoices = billingService.getTenantInvoices(tenantId);
        return ApiResponse.success(invoices);
    }

    /**
     * 处理支付
     */
    @PostMapping("/invoices/{invoiceId}/payment")
    public ApiResponse<Void> processPayment(
            @PathVariable String invoiceId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        billingService.processPayment(
                invoiceId,
                request.getPaymentMethod(),
                request.getTransactionId()
        );
        return ApiResponse.success();
    }

    /**
     * 获取订阅计划列表
     */
    @GetMapping("/plans")
    public ApiResponse<List<SubscriptionPlan>> getSubscriptionPlans() {
        List<SubscriptionPlan> plans = billingService.getAvailablePlans();
        return ApiResponse.success(plans);
    }
}

// DTO 类
@Data
class CreateSubscriptionRequest {
    private String tenantId;
    private String planId;
    private Boolean startTrial = true;
}

@Data
class ChangePlanRequest {
    private String newPlanId;
}

@Data
class CancelSubscriptionRequest {
    private String reason;
}

@Data
class RecordUsageRequest {
    private String tenantId;
    private String metricType;
    private BigDecimal quantity;
    private String resourceId;
    private String metadata;
}

@Data
class GenerateInvoiceRequest {
    private String subscriptionId;
}

@Data
class ProcessPaymentRequest {
    private String paymentMethod;
    private String transactionId;
}
