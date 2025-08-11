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

class RefundServiceTest {

    private InvoiceService invoiceService;
    private RefundRecordRepository refundRecordRepository;
    private io.github.rosestack.billing.payment.PaymentGatewayService paymentGatewayService;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        invoiceService = mock(InvoiceService.class);
        refundRecordRepository = mock(RefundRecordRepository.class);
        paymentGatewayService = mock(io.github.rosestack.billing.payment.PaymentGatewayService.class);
        refundService = new RefundService(invoiceService, paymentGatewayService, refundRecordRepository);
    }

    @Test
    void requestRefund_onlyPaidInvoiceAllowed_andNotExceedTotal() {
        String invoiceId = "inv-1";
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setTenantId("tenant-1");
        invoice.setPaymentMethod("WECHAT");
        invoice.setPaymentTransactionId("tx-1");
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceService.getInvoiceDetails(invoiceId)).thenReturn(invoice);
        when(refundRecordRepository.sumSucceededAmountByInvoiceId(invoiceId)).thenReturn(new BigDecimal("20.00"));
        when(paymentGatewayService.processRefund(eq("tx-1"), eq(new BigDecimal("30.00")), anyString(), eq("tenant-1")))
            .thenReturn(RefundResult.success("rf-1"));

        RefundResult result = refundService.requestRefund(invoiceId, new BigDecimal("30.00"), "reason");

        assertTrue(result.isSuccess());
        verify(refundRecordRepository, times(1)).insert(any(io.github.rosestack.billing.entity.RefundRecord.class));
        verify(invoiceService, never()).updateById(invoice); // 部分退款不改变发票为 REFUNDED
    }
}

