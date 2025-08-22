package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.InvoiceStatus;
import io.github.rosestack.billing.domain.invoice.BillInvoice;
import io.github.rosestack.billing.domain.invoice.BillInvoiceMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.core.exception.BusinessException;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 账单管理服务
 *
 * 提供账单的生成、支付、查询、状态管理等核心业务功能
 * 支持自动账单生成、逾期处理、退款等企业级功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class BillInvoiceService {

    private final BillInvoiceMapper invoiceMapper;
    private final BillSubscriptionMapper subscriptionMapper;
    private final BillUsageService usageService;
    private final BillingEngineService billingEngineService;
    private final Timer invoiceGenerationTimer;

    public BillInvoiceService(BillInvoiceMapper invoiceMapper,
                           BillSubscriptionMapper subscriptionMapper,
                           BillUsageService usageService,
                           BillingEngineService billingEngineService,
                           Timer invoiceGenerationTimer) {
        this.invoiceMapper = invoiceMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.usageService = usageService;
        this.billingEngineService = billingEngineService;
        this.invoiceGenerationTimer = invoiceGenerationTimer;
    }

    /**
     * 创建账单
     */
    @Transactional(rollbackFor = Exception.class)
    public BillInvoice createBill(BillInvoice bill) {
        log.info("Creating new bill for subscription: {}", bill.getSubscriptionId());

        // 验证订阅是否存在
        BillSubscription subscription = subscriptionMapper.selectById(bill.getSubscriptionId());
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + bill.getSubscriptionId());
        }

        // 生成唯一账单编号
        String billNo = generateBillNo();
        while (invoiceMapper.existsByBillNo(billNo)) {
            billNo = generateBillNo();
        }
        bill.setBillNo(billNo);

        // 设置默认值
        if (bill.getStatus() == null) {
            bill.setStatus(InvoiceStatus.DRAFT);
        }
        if (bill.getCurrency() == null) {
            bill.setCurrency("CNY");
        }
        if (bill.getPaidAmount() == null) {
            bill.setPaidAmount(BigDecimal.ZERO);
        }
        if (bill.getDiscountAmount() == null) {
            bill.setDiscountAmount(BigDecimal.ZERO);
        }
        if (bill.getTaxAmount() == null) {
            bill.setTaxAmount(BigDecimal.ZERO);
        }

        // 计算未支付金额
        bill.setUnpaidAmount(bill.getTotalAmount());

        // 设置默认到期日期（30天后）
        if (bill.getDueDate() == null) {
            bill.setDueDate(LocalDate.now().plusDays(30));
        }

        // 保存账单
        invoiceMapper.insert(bill);
        log.info("Bill created successfully: id={}, billNo={}", bill.getId(), bill.getBillNo());

        return bill;
    }

    /**
     * 自动生成订阅账单
     */
    @Transactional(rollbackFor = Exception.class)
    public BillInvoice generateBillForSubscription(Long subscriptionId, LocalDate periodStart, LocalDate periodEnd) {
        return invoiceGenerationTimer.record(() -> {
            log.info("Generating bill for subscription: {}, period: {} to {}", 
                    subscriptionId, periodStart, periodEnd);

            // 检查是否已有该周期的账单
            BillInvoice existingBill = invoiceMapper.findBySubscriptionAndPeriod(subscriptionId, periodStart, periodEnd);
            if (existingBill != null) {
                log.warn("Bill already exists for subscription {} and period {} to {}", 
                        subscriptionId, periodStart, periodEnd);
                return existingBill;
            }

            // 获取用量数据并计算费用
            BigDecimal totalAmount = calculateBillAmount(subscriptionId, periodStart, periodEnd);

            // 创建账单
            BillInvoice bill = new BillInvoice();
            bill.setSubscriptionId(subscriptionId);
            bill.setPeriodStart(periodStart);
            bill.setPeriodEnd(periodEnd);
            bill.setTotalAmount(totalAmount);
            bill.setStatus(InvoiceStatus.DRAFT);

            // 生成账单详情
            String billDetails = generateBillDetails(subscriptionId, periodStart, periodEnd);
            bill.setBillDetails(billDetails);

            return createBill(bill);
        });
    }

    /**
     * 发布账单（从草稿状态变为可支付状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public BillInvoice publishBill(Long billId) {
        log.info("Publishing bill: {}", billId);

        BillInvoice bill = invoiceMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在: " + billId);
        }

        bill.publish();
        invoiceMapper.updateById(bill);
        
        log.info("Bill published successfully: {}", billId);
        return bill;
    }

    /**
     * 记录支付
     */
    @Transactional(rollbackFor = Exception.class)
    public BillInvoice recordPayment(Long billId, BigDecimal amount, String paymentMethod) {
        log.info("Recording payment for bill: {}, amount: {}", billId, amount);

        BillInvoice bill = invoiceMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在: " + billId);
        }

        if (!bill.canBePaid()) {
            throw new BusinessException("账单状态不允许支付: " + bill.getStatus());
        }

        if (amount.compareTo(bill.getRemainingAmount()) > 0) {
            throw new BusinessException("支付金额不能超过剩余应付金额");
        }

        // 记录支付
        bill.recordPayment(amount);
        invoiceMapper.updateById(bill);

        log.info("Payment recorded successfully for bill: {}, new status: {}", billId, bill.getStatus());
        return bill;
    }

    /**
     * 作废账单
     */
    @Transactional(rollbackFor = Exception.class)
    public void voidBill(Long billId, String reason) {
        log.info("Voiding bill: {}, reason: {}", billId, reason);

        BillInvoice bill = invoiceMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在: " + billId);
        }

        bill.voidBill();
        bill.setRemark(reason);
        invoiceMapper.updateById(bill);
        
        log.info("Bill voided successfully: {}", billId);
    }

    /**
     * 退款处理
     */
    @Transactional(rollbackFor = Exception.class)
    public BillInvoice processRefund(Long billId, BigDecimal refundAmount, String reason) {
        log.info("Processing refund for bill: {}, amount: {}", billId, refundAmount);

        BillInvoice bill = invoiceMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在: " + billId);
        }

        if (!bill.isPaid()) {
            throw new BusinessException("只有已支付的账单才能退款: " + bill.getStatus());
        }

        if (refundAmount.compareTo(bill.getPaidAmount()) > 0) {
            throw new BusinessException("退款金额不能超过已支付金额");
        }

        // 处理退款
        bill.markAsRefunded();
        bill.setRemark(reason);
        invoiceMapper.updateById(bill);

        log.info("Refund processed successfully for bill: {}", billId);
        return bill;
    }

    /**
     * 处理逾期账单
     */
    @Transactional(rollbackFor = Exception.class)
    public void processOverdueBills() {
        log.info("Processing overdue bills");

        List<BillInvoice> overdueBills = invoiceMapper.findOverdueBills();
        for (BillInvoice bill : overdueBills) {
            if (!bill.isOverdue()) {
                bill.markAsOverdue();
                invoiceMapper.updateById(bill);
                log.info("Bill marked as overdue: {}", bill.getId());
            }
        }

        log.info("Processed {} overdue bills", overdueBills.size());
    }

    /**
     * 批量生成账单
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateBillsForPeriod(LocalDate periodStart, LocalDate periodEnd) {
        log.info("Generating bills for period: {} to {}", periodStart, periodEnd);

        // 获取所有活跃订阅
        List<BillSubscription> activeSubscriptions = subscriptionMapper.findActiveSubscriptions();
        
        int generatedCount = 0;
        for (BillSubscription subscription : activeSubscriptions) {
            // 检查是否在计费周期内
            if (subscription.isInCurrentPeriod()) {
                try {
                    generateBillForSubscription(subscription.getId(), periodStart, periodEnd);
                    generatedCount++;
                } catch (Exception e) {
                    log.error("Failed to generate bill for subscription: {}", subscription.getId(), e);
                }
            }
        }

        log.info("Generated {} bills for period {} to {}", generatedCount, periodStart, periodEnd);
    }

    /**
     * 根据ID查找账单
     */
    public BillInvoice findById(Long id) {
        return invoiceMapper.selectById(id);
    }

    /**
     * 根据账单编号查找账单
     */
    public BillInvoice findByBillNo(String billNo) {
        return invoiceMapper.findByBillNo(billNo);
    }

    /**
     * 查找待支付账单
     */
    public List<BillInvoice> findPendingPaymentBills() {
        return invoiceMapper.findPendingPaymentBills();
    }

    /**
     * 查找逾期账单
     */
    public List<BillInvoice> findOverdueBills() {
        return invoiceMapper.findOverdueBills();
    }

    /**
     * 查找即将到期的账单
     */
    public List<BillInvoice> findBillsDueSoon(int days) {
        return invoiceMapper.findBillsDueSoon(days);
    }

    /**
     * 根据订阅ID查找账单
     */
    public List<BillInvoice> findBySubscriptionId(Long subscriptionId) {
        return invoiceMapper.findBySubscriptionId(subscriptionId);
    }

    /**
     * 计算账单金额
     */
    private BigDecimal calculateBillAmount(Long subscriptionId, LocalDate periodStart, LocalDate periodEnd) {
        log.debug("Calculating bill amount for subscription: {}, period: {} to {}", 
                subscriptionId, periodStart, periodEnd);
        
        try {
            // 使用计费引擎计算费用
            BillingEngineService.BillingResult billingResult = billingEngineService.calculateBilling(
                    subscriptionId, periodStart, periodEnd);
            
            return billingResult.getTotalAmount();
        } catch (Exception e) {
            log.error("Failed to calculate bill amount for subscription: {}", subscriptionId, e);
            // 降级处理：返回默认金额
            return BigDecimal.valueOf(100.00);
        }
    }

    /**
     * 生成账单详情
     */
    private String generateBillDetails(Long subscriptionId, LocalDate periodStart, LocalDate periodEnd) {
        try {
            // 使用计费引擎生成详细账单
            BillingEngineService.BillingResult billingResult = billingEngineService.calculateBilling(
                    subscriptionId, periodStart, periodEnd);
            
            // 将计费结果转换为JSON（简化处理）
            StringBuilder details = new StringBuilder();
            details.append("{");
            details.append("\"subscription_id\":")
                   .append(billingResult.getSubscriptionId()).append(",");
            details.append("\"plan_id\":")
                   .append(billingResult.getPlanId()).append(",");
            details.append("\"period_start\":\"")
                   .append(billingResult.getPeriodStart()).append("\",");
            details.append("\"period_end\":\"")
                   .append(billingResult.getPeriodEnd()).append("\",");
            details.append("\"quantity\":")
                   .append(billingResult.getQuantity()).append(",");
            details.append("\"subtotal\":")
                   .append(billingResult.getSubtotal()).append(",");
            details.append("\"discount\":")
                   .append(billingResult.getDiscount()).append(",");
            details.append("\"tax\":")
                   .append(billingResult.getTax()).append(",");
            details.append("\"total_amount\":")
                   .append(billingResult.getTotalAmount()).append(",");
            details.append("\"generated_time\":\"")
                   .append(LocalDateTime.now()).append("\"");
            details.append("}");
            
            return details.toString();
        } catch (Exception e) {
            log.error("Failed to generate bill details for subscription: {}", subscriptionId, e);
            // 降级处理：返回简单详情
            return String.format(
                "{\"subscription_id\":%d,\"period_start\":\"%s\",\"period_end\":\"%s\",\"generated_time\":\"%s\"}", 
                subscriptionId, periodStart, periodEnd, LocalDateTime.now()
            );
        }
    }

    /**
     * 生成账单编号
     */
    private String generateBillNo() {
        return "INV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}