package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.RefundRecord;
import io.github.rosestack.billing.enums.RefundStatus;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface RefundRecordRepository extends BaseMapper<RefundRecord> {

    default BigDecimal sumSucceededAmountByInvoiceId(String invoiceId) {
        QueryWrapper<RefundRecord> qw = new QueryWrapper<>();
        qw.select("COALESCE(SUM(refund_amount),0) AS total")
          .eq("invoice_id", invoiceId)
          .eq("status", RefundStatus.SUCCESS.name());
        var list = selectMaps(qw);
        if (list.isEmpty() || list.get(0) == null) return BigDecimal.ZERO;
        Object v = list.get(0).get("total");
        return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
    }
}

