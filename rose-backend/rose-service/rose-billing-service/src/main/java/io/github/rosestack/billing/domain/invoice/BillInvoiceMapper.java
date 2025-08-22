package io.github.rosestack.billing.domain.invoice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.BillStatus;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 账单Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillInvoiceMapper extends BaseMapper<BillInvoice> {
    
    /**
     * 根据账单编号查找账单
     */
    default BillInvoice findByBillNo(String billNo) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getBillNo, billNo)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }
    
    /**
     * 根据订阅ID查找所有账单
     */
    default List<BillInvoice> findBySubscriptionId(Long subscriptionId) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getSubscriptionId, subscriptionId)
                .orderByDesc(BillInvoice::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据状态查找账单
     */
    default List<BillInvoice> findByStatus(BillStatus status) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getStatus, status)
                .orderByDesc(BillInvoice::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找待支付的账单
     */
    default List<BillInvoice> findPendingPaymentBills() {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .in(BillInvoice::getStatus, BillStatus.PENDING, BillStatus.OVERDUE)
                .orderByAsc(BillInvoice::getDueDate);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找逾期账单
     */
    default List<BillInvoice> findOverdueBills() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .and(wrapper -> wrapper
                        .eq(BillInvoice::getStatus, BillStatus.OVERDUE)
                        .or()
                        .and(subWrapper -> subWrapper
                                .eq(BillInvoice::getStatus, BillStatus.PENDING)
                                .lt(BillInvoice::getDueDate, today)
                        )
                )
                .orderByAsc(BillInvoice::getDueDate);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找即将到期的账单
     */
    default List<BillInvoice> findBillsDueSoon(int days) {
        LocalDate dueDate = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getStatus, BillStatus.PENDING)
                .le(BillInvoice::getDueDate, dueDate)
                .orderByAsc(BillInvoice::getDueDate);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找指定日期到期的账单（用于发送提醒）
     */
    default List<BillInvoice> findUpcomingDueInvoices(LocalDate dueDate) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getStatus, BillStatus.PENDING)
                .eq(BillInvoice::getDueDate, dueDate)
                .orderByAsc(BillInvoice::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据计费周期查找账单
     */
    default List<BillInvoice> findByPeriod(LocalDate periodStart, LocalDate periodEnd) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .ge(BillInvoice::getPeriodStart, periodStart)
                .le(BillInvoice::getPeriodEnd, periodEnd)
                .orderByDesc(BillInvoice::getPeriodStart);
        return selectList(queryWrapper);
    }
    
    /**
     * 根据订阅ID和计费周期查找账单
     */
    default BillInvoice findBySubscriptionAndPeriod(Long subscriptionId, LocalDate periodStart, LocalDate periodEnd) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getSubscriptionId, subscriptionId)
                .eq(BillInvoice::getPeriodStart, periodStart)
                .eq(BillInvoice::getPeriodEnd, periodEnd)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }
    
    /**
     * 查找指定订阅的最新账单
     */
    default BillInvoice findLatestBySubscription(Long subscriptionId) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getSubscriptionId, subscriptionId)
                .orderByDesc(BillInvoice::getCreatedTime)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }
    
    /**
     * 计算指定订阅的未支付金额总计
     */
    default BigDecimal sumUnpaidAmountBySubscription(Long subscriptionId) {
        List<BillInvoice> unpaidBills = findPendingPaymentBillsBySubscription(subscriptionId);
        return unpaidBills.stream()
                .map(bill -> bill.getUnpaidAmount() != null ? bill.getUnpaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 查找指定订阅的待支付账单
     */
    default List<BillInvoice> findPendingPaymentBillsBySubscription(Long subscriptionId) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getSubscriptionId, subscriptionId)
                .in(BillInvoice::getStatus, BillStatus.PENDING, BillStatus.OVERDUE)
                .orderByAsc(BillInvoice::getDueDate);
        return selectList(queryWrapper);
    }
    
    /**
     * 查找指定时间段创建的账单
     */
    default List<BillInvoice> findByCreatedTimeRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .ge(BillInvoice::getCreatedTime, startDate.atStartOfDay())
                .le(BillInvoice::getCreatedTime, endDate.atTime(23, 59, 59))
                .orderByDesc(BillInvoice::getCreatedTime);
        return selectList(queryWrapper);
    }
    
    /**
     * 检查账单编号是否存在
     */
    default boolean existsByBillNo(String billNo) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getBillNo, billNo);
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 统计指定状态的账单数量
     */
    default long countByStatus(BillStatus status) {
        LambdaQueryWrapper<BillInvoice> queryWrapper = new LambdaQueryWrapper<BillInvoice>()
                .eq(BillInvoice::getStatus, status);
        return selectCount(queryWrapper);
    }
    
    /**
     * 计算指定状态账单的总金额
     */
    default BigDecimal sumAmountByStatus(BillStatus status) {
        List<BillInvoice> bills = findByStatus(status);
        return bills.stream()
                .map(bill -> bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}