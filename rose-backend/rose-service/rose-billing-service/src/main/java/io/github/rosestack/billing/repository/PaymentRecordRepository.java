package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.PaymentRecord;
import io.github.rosestack.billing.enums.PaymentRecordStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 支付记录数据访问接口
 *
 * @author rose
 */
@Mapper
public interface PaymentRecordRepository extends BaseMapper<PaymentRecord> {

    /**
     * 根据账单ID查找支付记录
     */
    default List<PaymentRecord> findByInvoiceId(String invoiceId) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getInvoiceId, invoiceId)
                .eq(PaymentRecord::getDeleted, false);
        return selectList(wrapper);
    }

    /**
     * 根据租户ID查找支付记录
     */
    default List<PaymentRecord> findByTenantIdOrderByCreateTimeDesc(String tenantId) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getTenantId, tenantId)
                .eq(PaymentRecord::getDeleted, false)
                .orderByDesc(PaymentRecord::getCreatedTime);
        return selectList(wrapper);
    }

    /**
     * 根据交易ID查找支付记录
     */
    default Optional<PaymentRecord> findByTransactionId(String transactionId) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getTransactionId, transactionId)
                .eq(PaymentRecord::getDeleted, false);
        PaymentRecord record = selectOne(wrapper);
        return Optional.ofNullable(record);
    }

    /**
     * 根据支付状态查找记录
     */
    default List<PaymentRecord> findByStatus(PaymentRecordStatus status) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getStatus, status)
                .eq(PaymentRecord::getDeleted, false);
        return selectList(wrapper);
    }
}
