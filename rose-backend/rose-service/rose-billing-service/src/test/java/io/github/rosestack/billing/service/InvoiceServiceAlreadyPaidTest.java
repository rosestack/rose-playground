package io.github.rosestack.billing.service;

import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceAlreadyPaidTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void testAlreadyPaidIgnoreDifferentTxId() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-1");
        invoice.setTenantId("t-1");
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentTransactionId("tx-old");

        when(invoiceRepository.selectById("inv-1")).thenReturn(invoice);
        when(paymentRecordRepository.findByTransactionId("tx-new")).thenReturn(Optional.empty());

        invoiceService.markInvoiceAsPaid("inv-1", "ALIPAY", "tx-new");

        // 已支付忽略：不应再次更新或插入
        verify(invoiceRepository, never()).updateById(any(Invoice.class));
        verify(paymentRecordRepository, never()).insert(any(io.github.rosestack.billing.entity.PaymentRecord.class));
    }
}

