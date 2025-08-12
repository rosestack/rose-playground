package io.github.rosestack.billing.service;

import static org.mockito.Mockito.*;

import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.enums.InvoiceStatus;
import io.github.rosestack.billing.repository.InvoiceRepository;
import io.github.rosestack.billing.repository.PaymentRecordRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceIdempotencyTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId("inv-1");
        invoice.setTenantId("t-1");
        invoice.setStatus(InvoiceStatus.PENDING);
    }

    @Test
    void testMarkInvoiceAsPaid_Idempotent() {
        when(invoiceRepository.selectById("inv-1")).thenReturn(invoice);
        when(paymentRecordRepository.findByTransactionId("tx-1")).thenReturn(Optional.empty());

        invoiceService.markInvoiceAsPaid("inv-1", "WECHAT", "tx-1");

        // 第二次相同回调应被忽略（已存在交易ID）
        when(paymentRecordRepository.findByTransactionId("tx-1"))
                .thenReturn(Optional.of(mock(io.github.rosestack.billing.entity.PaymentRecord.class)));
        invoiceService.markInvoiceAsPaid("inv-1", "WECHAT", "tx-1");

        verify(invoiceRepository, times(1)).updateById(any(Invoice.class));
        verify(paymentRecordRepository, times(1)).insert(any(io.github.rosestack.billing.entity.PaymentRecord.class));
    }
}
