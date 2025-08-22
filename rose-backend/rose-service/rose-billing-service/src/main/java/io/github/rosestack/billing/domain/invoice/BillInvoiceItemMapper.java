package io.github.rosestack.billing.domain.invoice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.InvoiceItemType;
import io.github.rosestack.billing.domain.enums.TargetType;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 账单明细Mapper接口
 * 
 * 定义自定义查询方法，使用LambdaQueryWrapper实现，提高代码复用性
 * Service层调用这些方法，避免在Service中直接使用LambdaQueryWrapper
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface BillInvoiceItemMapper extends BaseMapper<BillInvoiceItem> {

    /**
     * 根据账单ID查找所有明细
     */
    default List<BillInvoiceItem> findByInvoiceId(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据账单ID和明细类型查找明细
     */
    default List<BillInvoiceItem> findByInvoiceAndType(Long invoiceId, InvoiceItemType itemType) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .eq(BillInvoiceItem::getItemType, itemType)
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据目标对象查找明细
     */
    default List<BillInvoiceItem> findByTarget(TargetType targetType, Long targetId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getTargetType, targetType)
                .eq(BillInvoiceItem::getTargetId, targetId)
                .orderByDesc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找账单的费用项明细（正向费用）
     */
    default List<BillInvoiceItem> findChargeItemsByInvoice(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .in(BillInvoiceItem::getItemType, 
                    InvoiceItemType.PLAN, 
                    InvoiceItemType.FEATURE, 
                    InvoiceItemType.TAX, 
                    InvoiceItemType.ADJUSTMENT)
                .gt(BillInvoiceItem::getAmount, BigDecimal.ZERO)
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找账单的折扣项明细（负向费用）
     */
    default List<BillInvoiceItem> findDiscountItemsByInvoice(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .and(wrapper -> wrapper
                        .eq(BillInvoiceItem::getItemType, InvoiceItemType.DISCOUNT)
                        .or()
                        .lt(BillInvoiceItem::getAmount, BigDecimal.ZERO)
                )
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找账单的套餐相关明细
     */
    default List<BillInvoiceItem> findPlanItemsByInvoice(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .eq(BillInvoiceItem::getItemType, InvoiceItemType.PLAN)
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找账单的功能相关明细
     */
    default List<BillInvoiceItem> findFeatureItemsByInvoice(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .eq(BillInvoiceItem::getItemType, InvoiceItemType.FEATURE)
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 查找账单的税费明细
     */
    default List<BillInvoiceItem> findTaxItemsByInvoice(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .eq(BillInvoiceItem::getItemType, InvoiceItemType.TAX)
                .orderByAsc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 根据计费周期查找明细
     */
    default List<BillInvoiceItem> findByBillingPeriod(LocalDate periodStart, LocalDate periodEnd) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .ge(BillInvoiceItem::getBillingPeriodStart, periodStart)
                .le(BillInvoiceItem::getBillingPeriodEnd, periodEnd)
                .orderByDesc(BillInvoiceItem::getBillingPeriodStart);
        return selectList(queryWrapper);
    }

    /**
     * 计算账单的明细总金额
     */
    default BigDecimal sumAmountByInvoice(Long invoiceId) {
        List<BillInvoiceItem> items = findByInvoiceId(invoiceId);
        return items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算账单的费用项总金额
     */
    default BigDecimal sumChargeAmountByInvoice(Long invoiceId) {
        List<BillInvoiceItem> items = findChargeItemsByInvoice(invoiceId);
        return items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算账单的折扣总金额
     */
    default BigDecimal sumDiscountAmountByInvoice(Long invoiceId) {
        List<BillInvoiceItem> items = findDiscountItemsByInvoice(invoiceId);
        return items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs(); // 取绝对值，因为折扣是负数
    }

    /**
     * 计算账单的税费总金额
     */
    default BigDecimal sumTaxAmountByInvoice(Long invoiceId) {
        List<BillInvoiceItem> items = findTaxItemsByInvoice(invoiceId);
        return items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据账单ID批量删除明细
     */
    default int deleteByInvoiceId(Long invoiceId) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId);
        return delete(queryWrapper);
    }

    /**
     * 根据明细类型统计数量
     */
    default long countByItemType(InvoiceItemType itemType) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getItemType, itemType);
        return selectCount(queryWrapper);
    }

    /**
     * 查找指定数量范围的明细
     */
    default List<BillInvoiceItem> findByQuantityRange(BigDecimal minQuantity, BigDecimal maxQuantity) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .ge(BillInvoiceItem::getQuantity, minQuantity)
                .le(BillInvoiceItem::getQuantity, maxQuantity)
                .orderByDesc(BillInvoiceItem::getQuantity);
        return selectList(queryWrapper);
    }

    /**
     * 查找指定金额范围的明细
     */
    default List<BillInvoiceItem> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .ge(BillInvoiceItem::getAmount, minAmount)
                .le(BillInvoiceItem::getAmount, maxAmount)
                .orderByDesc(BillInvoiceItem::getAmount);
        return selectList(queryWrapper);
    }

    /**
     * 根据货币单位查找明细
     */
    default List<BillInvoiceItem> findByCurrency(String currency) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getCurrency, currency)
                .orderByDesc(BillInvoiceItem::getCreatedTime);
        return selectList(queryWrapper);
    }

    /**
     * 检查账单是否有特定类型的明细
     */
    default boolean hasItemType(Long invoiceId, InvoiceItemType itemType) {
        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId)
                .eq(BillInvoiceItem::getItemType, itemType);
        return selectCount(queryWrapper) > 0;
    }

    /**
     * 批量更新明细的计费周期
     */
    default int updateBillingPeriodByInvoice(Long invoiceId, LocalDate periodStart, LocalDate periodEnd) {
        BillInvoiceItem updateEntity = new BillInvoiceItem();
        updateEntity.setBillingPeriodStart(periodStart);
        updateEntity.setBillingPeriodEnd(periodEnd);

        LambdaQueryWrapper<BillInvoiceItem> queryWrapper = new LambdaQueryWrapper<BillInvoiceItem>()
                .eq(BillInvoiceItem::getInvoiceId, invoiceId);

        return update(updateEntity, queryWrapper);
    }
}