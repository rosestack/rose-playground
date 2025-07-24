package io.github.rose.billing.infrastructure.repository;

import io.github.rose.billing.domain.entity.Invoice;
import io.github.rose.billing.domain.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单数据访问接口
 *
 * @author rose
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    /**
     * 根据租户ID查找账单列表
     */
    List<Invoice> findByTenantIdOrderByCreateTimeDesc(String tenantId);

    /**
     * 根据状态和到期日期查找账单
     */
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate dueDate);

    /**
     * 查找租户的待支付账单
     */
    List<Invoice> findByTenantIdAndStatusIn(String tenantId, List<InvoiceStatus> statuses);

    /**
     * 统计租户总收入
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.tenantId = :tenantId AND i.status = 'PAID'")
    BigDecimal sumPaidAmountByTenantId(String tenantId);

    /**
     * 统计时间段内的收入
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.status = 'PAID' AND i.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal sumPaidAmountByPeriod(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查找逾期账单
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'OVERDUE' " +
           "AND i.dueDate < :currentDate")
    List<Invoice> findOverdueInvoices(LocalDate currentDate);
}
