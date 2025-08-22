package io.github.rosestack.billing.domain.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.PaymentMethod;
import io.github.rosestack.billing.domain.enums.PaymentStatus;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillPaymentMapper extends BaseMapper<BillPayment> {

    /**
     * 根据支付编号查找支付记录
     */
    default BillPayment findByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getPaymentNo, paymentNo)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }

    /**
     * 根据账单ID查找所有支付记录
     */
    default List<BillPayment> findByInvoiceId(Long invoiceId) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getInvoiceId, invoiceId)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据租户ID查找支付记录
     */
    default List<BillPayment> findByTenantId(String tenantId) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getTenantId, tenantId)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据支付状态查找支付记录
     */
    default List<BillPayment> findByStatus(PaymentStatus status) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, status)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据支付方式查找支付记录
     */
    default List<BillPayment> findByPaymentMethod(PaymentMethod paymentMethod) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getPaymentMethod, paymentMethod)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找成功的支付记录
     */
    default List<BillPayment> findSuccessfulPayments() {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, PaymentStatus.SUCCESS)
                .orderByDesc(BillPayment::getPaidTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找失败的支付记录
     */
    default List<BillPayment> findFailedPayments() {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .in(BillPayment::getStatus, PaymentStatus.FAILED, PaymentStatus.CANCELLED)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找处理中的支付记录
     */
    default List<BillPayment> findPendingPayments() {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, PaymentStatus.PENDING)
                .orderByAsc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找可退款的支付记录
     */
    default List<BillPayment> findRefundablePayments() {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, PaymentStatus.SUCCESS)
                .orderByDesc(BillPayment::getPaidTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据网关交易ID查找支付记录
     */
    default BillPayment findByGatewayTransactionId(String gatewayTransactionId) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getGatewayTransactionId, gatewayTransactionId)
                .last("LIMIT 1");
        return selectOne(queryWrapper);
    }

    /**
     * 根据支付网关查找支付记录
     */
    default List<BillPayment> findByPaymentGateway(String paymentGateway) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getPaymentGateway, paymentGateway)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据时间范围查找支付记录
     */
    default List<BillPayment> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .ge(BillPayment::getCreatedTime, startTime)
                .le(BillPayment::getCreatedTime, endTime)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据支付时间范围查找成功支付记录
     */
    default List<BillPayment> findSuccessfulPaymentsByPaidTime(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, PaymentStatus.SUCCESS)
                .ge(BillPayment::getPaidTime, startTime)
                .le(BillPayment::getPaidTime, endTime)
                .orderByDesc(BillPayment::getPaidTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据金额范围查找支付记录
     */
    default List<BillPayment> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .ge(BillPayment::getAmount, minAmount)
                .le(BillPayment::getAmount, maxAmount)
                .orderByDesc(BillPayment::getAmount);
        return selectList(queryWrapper);
    }

    /**
     * 计算账单的已支付金额总计
     */
    default BigDecimal sumPaidAmountByInvoice(Long invoiceId) {
        List<BillPayment> successfulPayments = findSuccessfulPaymentsByInvoice(invoiceId);
        return successfulPayments.stream()
                .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据账单ID查找成功的支付记录
     */
    default List<BillPayment> findSuccessfulPaymentsByInvoice(Long invoiceId) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getInvoiceId, invoiceId)
                .eq(BillPayment::getStatus, PaymentStatus.SUCCESS)
                .orderByDesc(BillPayment::getPaidTime);
        return selectList(queryWrapper);
    }

    /**
     * 计算租户的总支付金额
     */
    default BigDecimal sumPaidAmountByTenant(String tenantId) {
        List<BillPayment> successfulPayments = findSuccessfulPaymentsByTenant(tenantId);
        return successfulPayments.stream()
                .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据租户ID查找成功的支付记录
     */
    default List<BillPayment> findSuccessfulPaymentsByTenant(String tenantId) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getTenantId, tenantId)
                .eq(BillPayment::getStatus, PaymentStatus.SUCCESS)
                .orderByDesc(BillPayment::getPaidTime);
        return selectList(queryWrapper);
    }

    /**
     * 统计指定状态的支付记录数量
     */
    default long countByStatus(PaymentStatus status) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, status);
        return selectCount(queryWrapper);
    }

    /**
     * 统计指定支付方式的支付记录数量
     */
    default long countByPaymentMethod(PaymentMethod paymentMethod) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getPaymentMethod, paymentMethod);
        return selectCount(queryWrapper);
    }

    /**
     * 检查支付编号是否存在
     */
    default boolean existsByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getPaymentNo, paymentNo);
        return selectCount(queryWrapper) > 0;
    }

    /**
     * 检查网关交易ID是否存在
     */
    default boolean existsByGatewayTransactionId(String gatewayTransactionId) {
        if (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getGatewayTransactionId, gatewayTransactionId);
        return selectCount(queryWrapper) > 0;
    }

    /**
     * 查找超时的待处理支付记录
     */
    default List<BillPayment> findTimeoutPendingPayments(LocalDateTime timeoutThreshold) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getStatus, PaymentStatus.PENDING)
                .lt(BillPayment::getCreatedTime, timeoutThreshold)
                .orderByAsc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据货币单位查找支付记录
     */
    default List<BillPayment> findByCurrency(String currency) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getCurrency, currency)
                .orderByDesc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找需要手动处理的支付记录
     */
    default List<BillPayment> findPaymentsRequiringManualProcessing() {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .in(BillPayment::getPaymentMethod, PaymentMethod.BANK_TRANSFER, PaymentMethod.OTHER)
                .in(BillPayment::getStatus, PaymentStatus.PENDING, PaymentStatus.FAILED)
                .orderByAsc(BillPayment::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 批量更新支付状态
     */
    default int updatePaymentStatus(List<Long> paymentIds, PaymentStatus status) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return 0;
        }

        BillPayment updateEntity = new BillPayment();
        updateEntity.setStatus(status);

        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .in(BillPayment::getId, paymentIds);

        return update(updateEntity, queryWrapper);
    }

    /**
     * 根据账单ID删除支付记录
     */
    default int deleteByInvoiceId(Long invoiceId) {
        LambdaQueryWrapper<BillPayment> queryWrapper = new LambdaQueryWrapper<BillPayment>()
                .eq(BillPayment::getInvoiceId, invoiceId);
        return delete(queryWrapper);
    }
}