package io.github.rosestack.billing.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import io.github.rosestack.billing.exception.InvoiceNotFoundException;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 账单管理服务
 *
 * @author rose
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService extends ServiceImpl<InvoiceRepository, Invoice> {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    // 可选注入，未配置 Outbox 时不影响主流程
    @Autowired(required = false)
    private OutboxService outboxService;

    /**
     * 获取租户的账单列表
     */
    public List<Invoice> getTenantInvoices(String tenantId) {
        return invoiceRepository.findByTenantIdOrderByCreateTimeDesc(tenantId);
    }

    /**
     * 获取待支付的账单
     */
    public List<Invoice> getPendingInvoices(String tenantId) {
        List<InvoiceStatus> pendingStatuses = List.of(InvoiceStatus.PENDING, InvoiceStatus.OVERDUE);
        return invoiceRepository.findByTenantIdAndStatusIn(tenantId, pendingStatuses);
    }

    /**
     * 获取逾期账单
     */
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    /**
     * 标记账单为已支付
     *
     * @param invoiceId 账单ID
     * @param paymentMethod 支付方式
     * @param transactionId 交易ID
     * @throws InvoiceNotFoundException 当账单不存在时抛出
     * @throws IllegalStateException 当账单状态不允许支付时抛出
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void markInvoiceAsPaid(@NotBlank String invoiceId,
                                  @NotBlank String paymentMethod,
                                  @NotBlank String transactionId) {

        Invoice invoice = invoiceRepository.selectById(invoiceId);
        // 幂等性检查：若同一 transactionId 已处理则忽略
        if (paymentRecordRepository.findByTransactionId(transactionId).isPresent()) {
            log.warn("重复回调已忽略: invoiceId={}, transactionId={}", invoiceId, transactionId);
            return;
        }

        if (invoice == null) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        // 已支付直接忽略，增强回调幂等性
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            if (transactionId.equals(invoice.getPaymentTransactionId())) {
                log.warn("账单已支付且交易ID一致，忽略: invoiceId={}, transactionId={}", invoiceId, transactionId);
            } else {
                log.warn("账单已支付但交易ID不同，忽略: invoiceId={}, existedTxId={}, incomingTxId={}",
                        invoiceId, invoice.getPaymentTransactionId(), transactionId);
            }
            return;
        }

        // 状态验证（仅允许从 PENDING/OVERDUE 变为 PAID）
        if (invoice.getStatus() != InvoiceStatus.PENDING && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException(
                String.format("账单状态不允许支付: 当前状态=%s, 账单ID=%s", invoice.getStatus(), invoiceId));
        }

        // 幂等性检查：transactionId是否已存在
        var existing = paymentRecordRepository.findByTransactionId(transactionId);
        if (existing.isPresent()) {
            log.warn("重复回调已忽略: invoiceId={}, transactionId={}", invoiceId, transactionId);
            return;
        }

        try {
            // 先落支付记录再更新账单，防止并发
            PaymentRecord record = new PaymentRecord();
            record.setId(null);
            record.setInvoiceId(invoiceId);
            record.setTenantId(invoice.getTenantId());
            record.setAmount(invoice.getTotalAmount());
            record.setPaymentMethod(paymentMethod);
            record.setTransactionId(transactionId);
            record.setStatus(PaymentRecordStatus.PENDING);
            paymentRecordRepository.insert(record);

            // 业务确认成功后再更新为 SUCCESS
            record.setStatus(PaymentRecordStatus.SUCCESS);
            record.setPaidTime(LocalDateTime.now());
            paymentRecordRepository.updateById(record);

            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidTime(LocalDateTime.now());
            invoice.setPaymentMethod(paymentMethod);
            invoice.setPaymentTransactionId(transactionId);
            invoiceRepository.updateById(invoice);

            // Outbox: 支付成功事件（可选）
            if (outboxService != null) {
                try {
                    String payload = new ObjectMapper().writeValueAsString(
                            java.util.Map.of(
                                    "invoiceId", invoiceId,
                                    "transactionId", transactionId,
                                    "paymentMethod", paymentMethod,
                                    "amount", invoice.getTotalAmount(),
                                    "currency", invoice.getCurrency(),
                                    "occurredTime", java.time.LocalDateTime.now().toString()
                            )
                    );
                    outboxService.saveEvent(invoice.getTenantId(), "PaymentSucceeded", invoiceId, payload);
                } catch (Exception ignore) {}
            }

            log.info("账单已标记为已支付: invoiceId={}, paymentMethod={}, transactionId={}, amount={}",
                    invoiceId, paymentMethod, transactionId, invoice.getTotalAmount());
        } catch (Exception e) {
            log.error("标记账单为已支付失败: invoiceId={}, transactionId={}", invoiceId, transactionId, e);
            throw new RuntimeException("标记账单为已支付失败", e);
        }
    }

    /**
     * 标记账单为逾期
     */
    @Transactional
    public void markInvoiceAsOverdue(String invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        if (invoice.getStatus() == InvoiceStatus.PENDING) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.updateById(invoice);
            log.info("账单已标记为逾期: {}", invoiceId);
        }
    }

    /**
     * 取消账单
     */
    @Transactional
    public void cancelInvoice(String invoiceId, String reason) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("已支付的账单不能取消");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.updateById(invoice);
        log.info("账单已取消: {}, 原因: {}", invoiceId, reason);
    }

    /**
     * 生成账单号
     */
    public String generateInvoiceNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        return "INV-" + datePrefix + "-" + timestamp;
    }

    /**
     * 计算租户总收入
     */
    public BigDecimal getTenantTotalRevenue(String tenantId) {
        return invoiceRepository.sumPaidAmountByTenantId(tenantId);
    }

    /**
     * 计算时间段内的收入
     */
    public BigDecimal getRevenueByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.sumPaidAmountByPeriod(startDate, endDate);
    }

    /**
     * 获取账单统计信息
     */
    public Map<String, Object> getInvoiceStats(String tenantId) {
        return invoiceRepository.getInvoiceStatsByTenant(tenantId);
    }

    /**
     * 获取收入趋势统计
     */
    public List<Map<String, Object>> getRevenueTrend(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.getRevenueStatsByPeriod(startDate, endDate);
    }

    /**
     * 处理逾期账单
     */
    @Transactional
    public int processOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findByStatusAndDueDateBefore(
                InvoiceStatus.PENDING, LocalDate.now());

        int count = 0;
        for (Invoice invoice : overdueInvoices) {
            markInvoiceAsOverdue(invoice.getId());
            count++;
        }

        log.info("处理逾期账单: {} 张", count);
        return count;
    }

    /**
     * 获取即将到期的账单
     */
    public List<Invoice> getInvoicesDueSoon(int days) {
        LocalDate dueDate = LocalDate.now().plusDays(days);
        return invoiceRepository.findByStatusAndDueDateBefore(InvoiceStatus.PENDING, dueDate);
    }

    /**
     * 重新发送账单
     */
    public void resendInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        if (invoice.getStatus() != InvoiceStatus.PENDING && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("只能重新发送待支付或逾期的账单");
        }

        // TODO: 集成通知服务发送账单
        log.info("重新发送账单: {}", invoiceId);
    }

    /**
     * 验证账单金额
     */
    public boolean validateInvoiceAmount(Invoice invoice) {
        BigDecimal calculatedTotal = invoice.getBaseAmount()
                .add(invoice.getUsageAmount() != null ? invoice.getUsageAmount() : BigDecimal.ZERO)
                .subtract(invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO)
                .add(invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO);

        return calculatedTotal.compareTo(invoice.getTotalAmount()) == 0;
    }

    /**
     * 获取账单详情
     */
    public Invoice getInvoiceDetails(String invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new InvoiceNotFoundException(invoiceId);
        }
        return invoice;
    }
}
