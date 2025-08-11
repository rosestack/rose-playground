package io.github.rosestack.billing.service;

import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.entity.TenantSubscription;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.event.PaymentSucceededEvent;
import io.github.rosestack.billing.exception.PlanNotFoundException;
import io.github.rosestack.billing.exception.SubscriptionNotFoundException;
import io.github.rosestack.billing.exception.UsageLimitExceededException;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.SubscriptionPlanRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 计费核心服务
 *
 * @author rose
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final SubscriptionPlanRepository planRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final UsageRecordRepository usageRepository;
    private final PricingCalculator pricingCalculator;
    private final BillingNotificationService notificationService;

    // 新增的服务依赖
    private final ApplicationEventPublisher eventPublisher;

    private final SubscriptionService subscriptionService;
    private final UsageService usageService;
    private final InvoiceService invoiceService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private TenantBillingConfigService tenantBillingConfigService;

    /**
     * 创建租户订阅
     *
     * @param tenantId 租户ID，不能为空
     * @param planId 订阅计划ID，不能为空
     * @param startTrial 是否开启试用期，可以为null（默认false）
     * @return 创建的订阅信息
     * @throws PlanNotFoundException 当订阅计划不存在时抛出
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantSubscription createSubscription(String tenantId, String planId, Boolean startTrial) {
        // 参数校验移到 Controller；此处保留业务校验
        SubscriptionPlan plan = planRepository.selectById(planId);
        if (plan == null) {
            throw new PlanNotFoundException(planId);
        }

        TenantSubscription subscription = new TenantSubscription();
        subscription.setId(UUID.randomUUID().toString());
        subscription.setTenantId(tenantId);
        subscription.setPlanId(planId);
        subscription.setStartTime(LocalDateTime.now());
        subscription.setAutoRenew(true);

        if (startTrial && plan.getTrialDays() != null && plan.getTrialDays() > 0) {
            // 设置试用期
            subscription.setInTrial(true);
            subscription.setTrialEndTime(LocalDateTime.now().plusDays(plan.getTrialDays()));
            subscription.setStatus(SubscriptionStatus.TRIAL);
            subscription.setNextBillingTime(subscription.getTrialEndTime());
        } else {
            // 直接激活订阅
            subscription.setInTrial(false);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setNextBillingTime(calculateNextBillingTime(plan));
        }

        subscription.setEndTime(subscription.getNextBillingTime());
        subscription.setCurrentPeriodAmount(plan.getBasePrice());

        subscriptionRepository.insert(subscription);

        // 发送订阅确认通知
        notificationService.sendSubscriptionConfirmation(tenantId, subscription);

        log.info("创建订阅成功，租户: {}, 计划: {}, 试用: {}", tenantId, planId, startTrial);
        return subscription;
    }

    /**
     * 获取租户订阅信息
     */
    public TenantSubscription getTenantSubscription(String tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new SubscriptionNotFoundException("tenant:" + tenantId));
    }

    /**
     * 更改订阅计划
     */
    @Transactional
    public TenantSubscription changePlan(String subscriptionId, String newPlanId) {
        TenantSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }

        SubscriptionPlan newPlan = planRepository.selectById(newPlanId);
        if (newPlan == null) {
            throw new PlanNotFoundException(newPlanId);
        }

        subscription.setPlanId(newPlanId);
        subscription.setCurrentPeriodAmount(newPlan.getBasePrice());

        subscriptionRepository.updateById(subscription);
        return subscription;
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void cancelSubscription(String subscriptionId, String reason) {
        TenantSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledTime(LocalDateTime.now());
        subscription.setCancellationReason(reason);

        subscriptionRepository.updateById(subscription);

        log.info("取消订阅成功，订阅ID: {}, 原因: {}", subscriptionId, reason);
    }

    /**
     * 生成账单
     */
    @Transactional
    public Invoice generateInvoice(String subscriptionId) {
        TenantSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }

        SubscriptionPlan plan = planRepository.selectById(subscription.getPlanId());
        if (plan == null) {
            throw new PlanNotFoundException(subscription.getPlanId());
        }

        LocalDate periodStart = subscription.getNextBillingTime().minusDays(plan.getBillingCycle()).toLocalDate();
        LocalDate periodEnd = subscription.getNextBillingTime().toLocalDate().minusDays(1);

        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID().toString());
        invoice.setTenantId(subscription.getTenantId());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setSubscriptionId(subscriptionId);
        invoice.setPeriodStart(periodStart);
        invoice.setPeriodEnd(periodEnd);
        invoice.setDueDate(LocalDate.now().plusDays(30)); // 30天付款期限
        invoice.setStatus(InvoiceStatus.PENDING);

        // 计算基础费用
        BigDecimal baseAmount = calculateBaseAmount(subscription, plan);
        invoice.setBaseAmount(baseAmount);

        // 计算使用量费用
        BigDecimal usageAmount = calculateUsageAmount(subscription.getTenantId(),
            periodStart.atStartOfDay(), periodEnd.plusDays(1).atStartOfDay(), plan);
        invoice.setUsageAmount(usageAmount);

        // 计算折扣
        BigDecimal discountAmount = calculateDiscount(subscription, baseAmount.add(usageAmount));
        invoice.setDiscountAmount(discountAmount);

        // 计算税费
        BigDecimal taxAmount = calculateTax(baseAmount.add(usageAmount).subtract(discountAmount));
        invoice.setTaxAmount(taxAmount);

        // 计算总金额
        BigDecimal totalAmount = baseAmount.add(usageAmount).subtract(discountAmount).add(taxAmount);
        invoice.setTotalAmount(totalAmount);
        String currency = tenantBillingConfigService.getCurrency(subscription.getTenantId());
        invoice.setCurrency(currency);
        java.util.Map<String, Object> snapshot = new java.util.HashMap<>();
        snapshot.put("planId", plan.getId());
        snapshot.put("billingCycle", plan.getBillingCycle());
        snapshot.put("basePrice", plan.getBasePrice());
        snapshot.put("trialDays", plan.getTrialDays());
        snapshot.put("currency", currency);
        snapshot.put("taxRate", "0.1"); // 示例税率
        invoice.setPriceSnapshot(snapshot);

        invoiceRepository.insert(invoice);

        // 更新订阅下次计费时间
        subscription.setNextBillingTime(calculateNextBillingTime(plan));
        subscriptionRepository.updateById(subscription);

        // 发送账单通知
        notificationService.sendInvoiceGenerated(subscription.getTenantId(), invoice);

        log.info("生成账单成功，租户: {}, 金额: {}", subscription.getTenantId(), totalAmount);
        return invoice;
    }

    /**
     * 获取租户账单列表
     */
    public List<Invoice> getTenantInvoices(String tenantId) {
        return invoiceRepository.findByTenantIdOrderByCreateTimeDesc(tenantId);
    }

    /**
     * 记录使用量
     */
    public void recordUsage(String tenantId, String metricType, BigDecimal quantity,
                           String resourceId, String metadata) {
        // 先检查使用量限制
        if (!subscriptionService.validateUsageLimit(tenantId, metricType)) {
            throw new UsageLimitExceededException(metricType, quantity, BigDecimal.ZERO);
        }

        // 使用 UsageService 记录使用量
        usageService.recordUsage(tenantId, metricType, quantity, metadata);

        // 异步检查使用量限制
        checkUsageLimitsAsync(tenantId, metricType);

        log.debug("记录使用量，租户: {}, 类型: {}, 数量: {}", tenantId, metricType, quantity);
    }

    /**
     * 记录使用量（带订阅ID）
     */
    public void recordUsage(String tenantId, String subscriptionId, String metricType, BigDecimal quantity,
                            String resourceId, String metadata) {
        if (!subscriptionService.validateUsageLimit(tenantId, metricType)) {
            throw new UsageLimitExceededException(metricType, quantity, BigDecimal.ZERO);
        }
        usageService.recordUsage(tenantId, subscriptionId, metricType, quantity, metadata);
        checkUsageLimitsAsync(tenantId, metricType);
        log.debug("记录使用量(含订阅)，租户: {}, 订阅: {}, 类型: {}, 数量: {}", tenantId, subscriptionId, metricType, quantity);
    }


    /**
     * 获取使用量统计
     */
    public List<Map<String, Object>> getUsageStats(String tenantId, String period) {
        // 根据period参数实现不同时间范围的查询
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = switch (period.toUpperCase()) {
            case "DAY" -> endTime.minusDays(1);
            case "WEEK" -> endTime.minusWeeks(1);
            case "MONTH" -> endTime.minusMonths(1);
            case "YEAR" -> endTime.minusYears(1);
            default -> endTime.minusDays(30); // 默认30天
        };

        return usageService.getUsageTrend(tenantId, startTime, endTime);
    }

    /**
     * 处理支付
     */
    @Transactional
    public void processPayment(String invoiceId, String paymentMethod, String transactionId) {
        // 使用 InvoiceService 处理支付
        invoiceService.markInvoiceAsPaid(invoiceId, paymentMethod, transactionId);

        // 获取账单信息
        Invoice invoice = invoiceService.getInvoiceDetails(invoiceId);

        // 激活或续期订阅
        TenantSubscription subscription = subscriptionRepository.selectById(invoice.getSubscriptionId());
        if (subscription == null) {
            throw new SubscriptionNotFoundException(invoice.getSubscriptionId());
        }

        if (subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.updateById(subscription);
        }

        // 标记使用量为已计费
        usageService.markUsageAsBilled(invoice.getTenantId(), invoice.getPeriodStart().atStartOfDay(),
            invoice.getPeriodEnd().plusDays(1).atStartOfDay(), invoiceId);

        // 发送支付确认通知（保留同步通知）
        notificationService.sendPaymentConfirmation(subscription.getTenantId(), invoice);
        // 发布领域事件，供异步监听与扩展
        eventPublisher.publishEvent(new PaymentSucceededEvent(
                subscription.getTenantId(), invoiceId, transactionId, paymentMethod,
                invoice.getTotalAmount(), LocalDateTime.now()
        ));

        log.info("处理支付成功，账单: {}, 金额: {}", invoiceId, invoice.getTotalAmount());
    }

    /**
     * 检查使用量限制
     */
    public boolean checkUsageLimit(String tenantId, String metricType) {
        TenantSubscription subscription = subscriptionRepository.findActiveByTenantId(tenantId)
            .orElse(null);

        if (subscription == null) {
            return false; // 无有效订阅
        }

        SubscriptionPlan plan = planRepository.selectById(subscription.getPlanId());

        if (plan == null) {
            return false;
        }

        // 获取当前计费周期的使用量
        LocalDateTime periodStart = subscription.getNextBillingTime().minusDays(plan.getBillingCycle());
        BigDecimal currentUsage = usageRepository.sumUsageByTenantAndMetricAndPeriod(
            tenantId, metricType, periodStart, LocalDateTime.now());

        // 检查限制
        return pricingCalculator.checkLimit(plan, metricType, currentUsage);
    }

    /**
     * 获取可用订阅计划
     */
    public List<SubscriptionPlan> getAvailablePlans() {
        return planRepository.findValidPlans(LocalDateTime.now());
    }

    /**
     * 汇总日使用量
     */
    public void aggregateDailyUsage(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        // 实现使用量数据汇总逻辑
        log.info("汇总使用量数据：{} - {}", startOfDay, endOfDay);
    }

    /**
     * 检查使用量限制并发送通知
     */
    public void checkUsageLimitsAndNotify() {
        // 实现使用量限制检查和通知逻辑
        log.info("检查使用量限制并发送通知");
    }

    /**
     * 生成财务报告
     */
    public void generateFinancialReport(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        // 实现财务报告生成逻辑
        log.info("生成财务报告：{} - {}，类型：{}", startDate, endDate, reportType);
    }

    /**
     * 清理过期使用量记录
     */
    public int cleanupOldUsageRecords(LocalDateTime cutoffDate) {
        return usageRepository.deleteOldBilledRecords(cutoffDate);
    }

    /**
     * 清理已取消的订阅
     */
    public int cleanupCancelledSubscriptions(LocalDateTime cutoffDate) {
        // 实现清理已取消订阅的逻辑
        log.info("清理已取消的订阅，截止时间：{}", cutoffDate);
        return 0;
    }

    // 私有辅助方法
    private LocalDateTime calculateNextBillingTime(SubscriptionPlan plan) {
        return LocalDateTime.now().plusDays(plan.getBillingCycle());
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    private BigDecimal calculateBaseAmount(TenantSubscription subscription, SubscriptionPlan plan) {
        if (subscription.getInTrial()) {
            return BigDecimal.ZERO;
        }
        return plan.getBasePrice();
    }

    private BigDecimal calculateUsageAmount(String tenantId, LocalDateTime periodStart,
                                          LocalDateTime periodEnd, SubscriptionPlan plan) {
        return pricingCalculator.calculateUsageAmount(tenantId, periodStart, periodEnd, plan);
    }

    private BigDecimal calculateDiscount(TenantSubscription subscription, BigDecimal amount) {
        // 实现折扣逻辑
        // 1. 检查长期订阅折扣
        if (subscription.getAutoRenew()) {
            return amount.multiply(new BigDecimal("0.05")); // 5%自动续费折扣
        }

        // 2. 检查试用期转换折扣
        if (subscription.getInTrial()) {
            return amount.multiply(new BigDecimal("0.1")); // 10%试用转换折扣
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateTax(BigDecimal amount) {
        // 实现税费计算逻辑
        return amount.multiply(new BigDecimal("0.1")); // 10%税率示例
    }

    private void checkUsageLimitsAsync(String tenantId, String metricType) {
        // 异步检查使用量限制并发送警告
        log.debug("异步检查使用量限制：租户 {}, 类型 {}", tenantId, metricType);
    }

}
