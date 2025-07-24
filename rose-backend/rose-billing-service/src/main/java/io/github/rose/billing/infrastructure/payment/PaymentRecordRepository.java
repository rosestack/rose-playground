package io.github.rose.billing.infrastructure.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付记录数据访问接口
 *
 * @author rose
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {

    /**
     * 根据账单ID查找支付记录
     */
    List<PaymentRecord> findByInvoiceId(String invoiceId);

    /**
     * 根据租户ID查找支付记录
     */
    List<PaymentRecord> findByTenantIdOrderByCreateTimeDesc(String tenantId);

    /**
     * 根据交易ID查找支付记录
     */
    Optional<PaymentRecord> findByTransactionId(String transactionId);

    /**
     * 根据支付状态查找记录
     */
    List<PaymentRecord> findByStatus(PaymentRecordStatus status);
}
