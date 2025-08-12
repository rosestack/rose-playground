package io.github.rosestack.billing.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.entity.RefundRecord;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.repository.RefundRecordRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefundCallbackServiceTest {

    private InvoiceService invoiceService;
    private RefundRecordRepository refundRecordRepository;
    private RefundService refundService;
    private io.github.rosestack.billing.payment.PaymentGatewayService gatewayService;

    @BeforeEach
    void setUp() {
        invoiceService = mock(InvoiceService.class);
        refundRecordRepository = mock(RefundRecordRepository.class);
        gatewayService = mock(io.github.rosestack.billing.payment.PaymentGatewayService.class);
        io.github.rosestack.billing.repository.PaymentRecordRepository pr =
                mock(io.github.rosestack.billing.repository.PaymentRecordRepository.class);
        refundService = new RefundService(invoiceService, gatewayService, refundRecordRepository, pr);
    }

    @Test
    void processRefundCallback_marksSuccess_andFullRefund() {
        Map<String, Object> cb = new HashMap<>();
        cb.put("invoiceId", "inv-9");
        cb.put("refund_id", "rf-9");
        cb.put("refund_amount", "30.00");
        cb.put("refund_status", "SUCCESS");

        when(refundRecordRepository.selectOne(any())).thenReturn(null);
        when(refundRecordRepository.sumSucceededAmountByInvoiceId("inv-9")).thenReturn(new BigDecimal("20.00"));
        Invoice invoice = new Invoice();
        invoice.setId("inv-9");
        invoice.setTotalAmount(new BigDecimal("50.00"));
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceService.getInvoiceDetails("inv-9")).thenReturn(invoice);
        when(gatewayService.parseRefundAmount(eq("ALIPAY"), anyMap())).thenReturn(new BigDecimal("30.00"));
        when(gatewayService.isRefundSuccess(eq("ALIPAY"), anyMap())).thenReturn(true);

        boolean ok = refundService.processRefundCallback("ALIPAY", cb);
        assertTrue(ok);
        verify(refundRecordRepository, times(1)).insert(any(RefundRecord.class));
        verify(invoiceService, times(1)).updateById(invoice);
    }

    @Test
    void processRefundCallback_failedDoesNotMarkInvoice() {
        Map<String, Object> cb = new HashMap<>();
        cb.put("invoice_id", "inv-8");
        cb.put("id", "rf-8");
        cb.put("refund_status", "FAILED");

        when(refundRecordRepository.selectOne(any())).thenReturn(new RefundRecord());
        Invoice invoice = new Invoice();
        invoice.setId("inv-8");
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceService.getInvoiceDetails("inv-8")).thenReturn(invoice);
        when(gatewayService.parseRefundAmount(eq("WECHAT"), anyMap())).thenReturn(null);
        when(gatewayService.isRefundSuccess(eq("WECHAT"), anyMap())).thenReturn(false);

        boolean ok = refundService.processRefundCallback("WECHAT", cb);
        assertTrue(ok);
        verify(invoiceService, never()).updateById(any());
    }
}
