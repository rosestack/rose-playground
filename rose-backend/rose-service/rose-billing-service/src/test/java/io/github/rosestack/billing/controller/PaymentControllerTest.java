package io.github.rosestack.billing.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.rosestack.billing.entity.Invoice;
import io.github.rosestack.billing.payment.PaymentGatewayService;
import io.github.rosestack.billing.payment.PaymentStatus;
import io.github.rosestack.billing.payment.PaymentMethod;
import io.github.rosestack.billing.service.BillingService;
import io.github.rosestack.billing.service.InvoiceService;
import io.github.rosestack.core.model.ApiResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private BillingService billingService;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private PaymentController controller;

    @Test
    void testCreatePaymentLink() {
        String invoiceId = "inv-1";
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setTenantId("t-1");
        invoice.setTotalAmount(new BigDecimal("123.45"));

        when(invoiceService.getInvoiceDetails(invoiceId)).thenReturn(invoice);
        when(paymentGatewayService.createPaymentLink(eq(invoiceId), any(), eq(PaymentMethod.WECHAT), eq("t-1")))
                .thenReturn("https://pay.link");

        PaymentController.CreatePaymentLinkRequest req = new PaymentController.CreatePaymentLinkRequest();
        req.setPaymentMethod("WECHAT");
        ApiResponse<String> resp = controller.createPaymentLink(invoiceId, req);

        assertTrue(resp.isSuccess());
        assertEquals("https://pay.link", resp.getData());
        verify(paymentGatewayService)
                .createPaymentLink(eq(invoiceId), eq(invoice.getTotalAmount()), eq(PaymentMethod.WECHAT), eq("t-1"));
    }

    @Test
    void testHandleCallback_Success() {
        Map<String, Object> data = new HashMap<>();
        data.put("invoiceId", "inv-1");
        data.put("transactionId", "tx-1");
        when(paymentGatewayService.verifyPaymentCallback(eq(PaymentMethod.ALIPAY), anyMap()))
                .thenReturn(true);

        ApiResponse<Void> resp = controller.handleCallback("ALIPAY", data);
        assertTrue(resp.isSuccess());
        verify(billingService).processPayment("inv-1", PaymentMethod.ALIPAY, "tx-1");
    }

    @Test
    void testHandleCallback_Invalid() {
        Map<String, Object> data = new HashMap<>();
        when(paymentGatewayService.verifyPaymentCallback(eq(PaymentMethod.ALIPAY), anyMap()))
                .thenReturn(false);
        ApiResponse<Void> resp = controller.handleCallback("ALIPAY", data);
        assertFalse(resp.isSuccess());
    }

    @Test
    void testQueryStatus() {
        when(paymentGatewayService.queryPaymentStatus("tx-1")).thenReturn(PaymentStatus.SUCCESS);
        ApiResponse<PaymentStatus> resp = controller.queryStatus("tx-1");
        assertTrue(resp.isSuccess());
        assertEquals(PaymentStatus.SUCCESS, resp.getData());
    }
}
