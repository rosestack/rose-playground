package io.github.rosestack.billing.service;

import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.repository.RefundRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefundServiceMoreTests {

    private InvoiceService invoiceService;
    private RefundRecordRepository refundRecordRepository;
    private io.github.rosestack.billing.payment.PaymentGatewayService paymentGatewayService;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        invoiceService = mock(InvoiceService.class);
        refundRecordRepository = mock(RefundRecordRepository.class);
        paymentGatewayService = mock(io.github.rosestack.billing.payment.PaymentGatewayService.class);
        io.github.rosestack.billing.repository.PaymentRecordRepository pr = mock(io.github.rosestack.billing.repository.PaymentRecordRepository.class);
        refundService = new RefundService(invoiceService, paymentGatewayService, refundRecordRepository, pr);
    }

    @Test
    void fullRefund_shouldMarkInvoiceRefunded() {
        String invoiceId = "inv-2";
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setTenantId("tenant-2");
        invoice.setPaymentMethod("ALIPAY");
        invoice.setPaymentTransactionId("tx-2");
        invoice.setTotalAmount(new BigDecimal("50.00"));
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceService.getInvoiceDetails(invoiceId)).thenReturn(invoice);
        when(refundRecordRepository.sumSucceededAmountByInvoiceId(invoiceId)).thenReturn(new BigDecimal("10.00"));
        when(paymentGatewayService.processRefund(eq("tx-2"), eq(new BigDecimal("40.00")), anyString(), eq("tenant-2")))
                .thenReturn(RefundResult.success("rf-2"));

        RefundResult result = refundService.requestRefund(invoiceId, new BigDecimal("40.00"), "full refund");
        assertTrue(result.isSuccess());
        verify(invoiceService, times(1)).updateById(invoice);
    }

    @Test
    void exceedRefund_shouldFail() {
        String invoiceId = "inv-3";
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setTenantId("tenant-3");
        invoice.setPaymentMethod("WECHAT");
        invoice.setPaymentTransactionId("tx-3");
        invoice.setTotalAmount(new BigDecimal("30.00"));
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceService.getInvoiceDetails(invoiceId)).thenReturn(invoice);
        when(refundRecordRepository.sumSucceededAmountByInvoiceId(invoiceId)).thenReturn(new BigDecimal("10.00"));

        RefundResult result = refundService.requestRefund(invoiceId, new BigDecimal("25.00"), "exceed");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("超出可退"));
        verify(paymentGatewayService, never()).processRefund(any(), any(), any(), any());
    }
}

