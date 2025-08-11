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
     * 统计时间段内已支付账单数量
     */
    @Select("SELECT COUNT(*) FROM invoice WHERE status = 'PAID' AND paid_at BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    long countPaidInvoicesByPeriod(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 统计时间段内基础订阅收入（base_amount）
     */
    @Select("SELECT COALESCE(SUM(base_amount), 0) FROM invoice WHERE status = 'PAID' AND paid_at BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    BigDecimal sumBaseAmountByPeriod(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定时间段内 Top N 租户收入
     */
    @Select("SELECT tenant_id as tenantId, COUNT(*) as invoiceCount, COALESCE(SUM(total_amount),0) as revenue, COALESCE(AVG(total_amount),0) as averageInvoiceValue " +
            "FROM invoice WHERE status='PAID' AND paid_at BETWEEN #{startDate} AND #{endDate} AND deleted=0 " +
            "GROUP BY tenant_id ORDER BY revenue DESC LIMIT #{limit}")
    List<java.util.Map<String, Object>> getTopTenantsByRevenue(@Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate,
                                                               @Param("limit") int limit);

    /**
     * 统计时间段内按订阅计划的收入
     */
    @Select("SELECT ts.plan_id AS planId, COALESCE(SUM(i.total_amount),0) AS revenue, COUNT(*) AS invoiceCount " +
            "FROM invoice i JOIN tenant_subscription ts ON i.subscription_id = ts.id " +
            "WHERE i.status='PAID' AND i.paid_at BETWEEN #{startDate} AND #{endDate} AND i.deleted=0 AND ts.deleted=0 " +
            "GROUP BY ts.plan_id")
    List<java.util.Map<String, Object>> getRevenueByPlan(@Param("startDate") LocalDateTime startDate,
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
