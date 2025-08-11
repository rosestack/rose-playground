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
                .orderByDesc(Invoice::getCreatedTime);
        return selectList(wrapper);
    }

    /**
     * 根据状态和到期日期查找账单
     */
    default List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate dueDate) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getStatus, status)
                .lt(Invoice::getDueDate, dueDate);
        return selectList(wrapper);
    }

    /**
     * 查找租户的待支付账单
     */
    default List<Invoice> findByTenantIdAndStatusIn(String tenantId, List<InvoiceStatus> statuses) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getTenantId, tenantId)
                .in(Invoice::getStatus, statuses);
        return selectList(wrapper);
    }

    /**
     * 统计租户总收入
     */
    default BigDecimal sumPaidAmountByTenantId(String tenantId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("COALESCE(SUM(total_amount), 0) AS total")
          .eq("tenant_id", tenantId)
          .eq("status", "PAID");
        java.util.List<java.util.Map<String, Object>> list = selectMaps(qw);
        if (list.isEmpty() || list.get(0) == null) return java.math.BigDecimal.ZERO;
        Object v = list.get(0).get("total");
        return v == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(v.toString());
    }

    /**
     * 统计时间段内的收入
     */
    default BigDecimal sumPaidAmountByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("COALESCE(SUM(total_amount), 0) AS total")
          .eq("status", "PAID")
          .between("paid_at", startDate, endDate);
        java.util.List<java.util.Map<String, Object>> list = selectMaps(qw);
        if (list.isEmpty() || list.get(0) == null) return java.math.BigDecimal.ZERO;
        Object v = list.get(0).get("total");
        return v == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(v.toString());
    }

    /**
     * 获取租户的账单统计
     */
    default java.util.Map<String, Object> getInvoiceStatsByTenant(String tenantId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("COUNT(*) as totalCount",
                "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pendingCount",
                "COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paidCount",
                "COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END) as overdueCount",
                "COALESCE(SUM(CASE WHEN status = 'PAID' THEN total_amount ELSE 0 END), 0) as totalPaidAmount",
                "COALESCE(SUM(CASE WHEN status = 'PENDING' THEN total_amount ELSE 0 END), 0) as totalPendingAmount")
          .eq("tenant_id", tenantId);
        java.util.List<java.util.Map<String, Object>> list = selectMaps(qw);
        return list.isEmpty() ? java.util.Map.of() : list.get(0);
    }

    /**
     * 获取时间段内的每日收入统计
     */
    default List<java.util.Map<String, Object>> getRevenueStatsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("DATE(paid_at) as paymentDate",
                "COUNT(*) as paymentCount",
                "SUM(total_amount) as dailyRevenue")
          .eq("status", "PAID")
          .between("paid_at", startDate, endDate)
          .groupBy("DATE(paid_at)")
          .orderByAsc("paymentDate");
        return selectMaps(qw);
    }

    /**
     * 统计时间段内已支付账单数量
     */
    default long countPaidInvoicesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("COUNT(*) AS cnt")
          .eq("status", "PAID")
          .between("paid_at", startDate, endDate);
        java.util.List<java.util.Map<String, Object>> list = selectMaps(qw);
        Object v = list.isEmpty() ? null : list.get(0).get("cnt");
        return v == null ? 0L : Long.parseLong(v.toString());
    }

    /**
     * 统计时间段内基础订阅收入（base_amount）
     */
    default BigDecimal sumBaseAmountByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("COALESCE(SUM(base_amount), 0) AS total")
          .eq("status", "PAID")
          .between("paid_at", startDate, endDate);
        java.util.List<java.util.Map<String, Object>> list = selectMaps(qw);
        if (list.isEmpty() || list.get(0) == null) return java.math.BigDecimal.ZERO;
        Object v = list.get(0).get("total");
        return v == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(v.toString());
    }

    /**
     * 统计指定时间段内 Top N 租户收入
     */
    default List<java.util.Map<String, Object>> getTopTenantsByRevenue(LocalDateTime startDate,
                                                                        LocalDateTime endDate,
                                                                        int limit) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Invoice> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("tenant_id as tenantId",
                "COUNT(*) as invoiceCount",
                "COALESCE(SUM(total_amount),0) as revenue",
                "COALESCE(AVG(total_amount),0) as averageInvoiceValue")
          .eq("status", "PAID")
          .between("paid_at", startDate, endDate)
          .groupBy("tenant_id")
          .orderByDesc("revenue")
          .last("LIMIT " + limit);
        return selectMaps(qw);
    }

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
