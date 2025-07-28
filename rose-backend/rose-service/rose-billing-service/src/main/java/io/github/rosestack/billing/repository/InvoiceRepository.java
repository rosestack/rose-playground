package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.enums.InvoiceStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单数据访问接口
 *
 * @author rose
 */
@Mapper
public interface InvoiceRepository extends BaseMapper<Invoice> {

    /**
     * 根据租户ID查找账单列表
     */
    default List<Invoice> findByTenantIdOrderByCreateTimeDesc(String tenantId) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getTenantId, tenantId)
                .eq(Invoice::getDeleted, false)
                .orderByDesc(Invoice::getCreatedTime);
        return selectList(wrapper);
    }

    /**
     * 根据状态和到期日期查找账单
     */
    default List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate dueDate) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getStatus, status)
                .lt(Invoice::getDueDate, dueDate)
                .eq(Invoice::getDeleted, false);
        return selectList(wrapper);
    }

    /**
     * 查找租户的待支付账单
     */
    default List<Invoice> findByTenantIdAndStatusIn(String tenantId, List<InvoiceStatus> statuses) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getTenantId, tenantId)
                .in(Invoice::getStatus, statuses)
                .eq(Invoice::getDeleted, false);
        return selectList(wrapper);
    }

    /**
     * 统计租户总收入
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM invoice " +
            "WHERE tenant_id = #{tenantId} AND status = 'PAID' AND deleted = 0")
    BigDecimal sumPaidAmountByTenantId(@Param("tenantId") String tenantId);

    /**
     * 统计时间段内的收入
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM invoice " +
            "WHERE status = 'PAID' AND paid_at BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    BigDecimal sumPaidAmountByPeriod(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 获取租户的账单统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pendingCount, " +
            "COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paidCount, " +
            "COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END) as overdueCount, " +
            "COALESCE(SUM(CASE WHEN status = 'PAID' THEN total_amount ELSE 0 END), 0) as totalPaidAmount, " +
            "COALESCE(SUM(CASE WHEN status = 'PENDING' THEN total_amount ELSE 0 END), 0) as totalPendingAmount " +
            "FROM invoice WHERE tenant_id = #{tenantId} AND deleted = 0")
    java.util.Map<String, Object> getInvoiceStatsByTenant(@Param("tenantId") String tenantId);

    /**
     * 获取时间段内的每日收入统计
     */
    @Select("SELECT " +
            "DATE(paid_at) as paymentDate, " +
            "COUNT(*) as paymentCount, " +
            "SUM(total_amount) as dailyRevenue " +
            "FROM invoice " +
            "WHERE status = 'PAID' AND paid_at BETWEEN #{startDate} AND #{endDate} AND deleted = 0 " +
            "GROUP BY DATE(paid_at) ORDER BY paymentDate")
    List<java.util.Map<String, Object>> getRevenueStatsByPeriod(@Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);

    /**
     * 查找逾期账单
     */
    default List<Invoice> findOverdueInvoices(LocalDate currentDate) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getStatus, InvoiceStatus.OVERDUE)
                .lt(Invoice::getDueDate, currentDate)
                .eq(Invoice::getDeleted, false);
        return selectList(wrapper);
    }
}
