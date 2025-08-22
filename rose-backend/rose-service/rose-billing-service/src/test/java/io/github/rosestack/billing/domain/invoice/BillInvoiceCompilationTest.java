package io.github.rosestack.billing.domain.invoice;

import io.github.rosestack.billing.domain.enums.BillStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BillInvoice编译验证测试
 * 
 * 验证修复后的枚举引用是否正确
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class BillInvoiceCompilationTest {

    @Test
    public void testBillStatusEnumUsage() {
        BillInvoice invoice = new BillInvoice();
        invoice.setStatus(BillStatus.DRAFT);
        invoice.setTotalAmount(BigDecimal.valueOf(100));
        invoice.setDueDate(LocalDate.now().plusDays(30));

        // 测试草稿状态
        assertEquals(BillStatus.DRAFT, invoice.getStatus());
        assertFalse(invoice.isPaid());
        assertFalse(invoice.isPartialPaid());
        assertFalse(invoice.isOverdue());
        assertTrue(invoice.canBeVoided());

        // 测试发布账单
        invoice.publish();
        assertEquals(BillStatus.PENDING, invoice.getStatus());
        assertTrue(invoice.canBePaid());

        // 测试部分支付
        invoice.recordPayment(BigDecimal.valueOf(50));
        assertTrue(invoice.isPartialPaid());
        assertFalse(invoice.isPaid());

        // 测试完全支付
        invoice.recordPayment(BigDecimal.valueOf(50));
        assertTrue(invoice.isPaid());
        assertFalse(invoice.isPartialPaid());
        assertEquals(BillStatus.PAID, invoice.getStatus());

        // 测试退款
        invoice.markAsRefunded();
        assertEquals(BillStatus.REFUNDED, invoice.getStatus());
    }

    @Test
    public void testBillInvoiceMapperCompilation() {
        // 这个测试只是为了确保Mapper接口能够编译
        // 实际的功能测试需要Spring上下文
        assertNotNull(BillInvoiceMapper.class);
    }

    @Test
    public void testAllBillStatusValues() {
        // 确保所有枚举值都可以正常使用
        BillStatus[] statuses = {
            BillStatus.DRAFT,
            BillStatus.PENDING,
            BillStatus.PAID,
            BillStatus.OVERDUE,
            BillStatus.CANCELLED,
            BillStatus.REFUNDED
        };

        for (BillStatus status : statuses) {
            assertNotNull(status);
            assertNotNull(status.getDescription());
        }
    }
}