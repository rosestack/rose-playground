package io.github.rosestack.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.payment.PaymentStatus;
import io.github.rosestack.billing.payment.PaymentGatewayService;
import io.github.rosestack.billing.service.BillingService;
import io.github.rosestack.billing.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentGatewayService paymentGatewayService = Mockito.mock(PaymentGatewayService.class);
    private final BillingService billingService = Mockito.mock(BillingService.class);
    private final InvoiceService invoiceService = Mockito.mock(InvoiceService.class);
    private final PaymentController controller = new PaymentController(paymentGatewayService, billingService, invoiceService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    @Test
    void createPaymentLink_returnsLink() throws Exception {
        Mockito.when(paymentGatewayService.createPaymentLink(eq("inv-1"), any(), eq("WECHAT"), eq("t-1")))
                .thenReturn("https://pay.link");
        var req = new PaymentController.CreatePaymentLinkRequest();
        req.setPaymentMethod("WECHAT");

        // 为了简化，不从 DB 读取发票金额，模拟 InvoiceService 层逻辑在其他测试中覆盖
        // 这里仅校验控制器响应结构
        Mockito.when(invoiceService.getInvoiceDetails("inv-1"))
                .thenAnswer(invocation -> {
                    var invoice = new io.github.rosestack.billing.entity.Invoice();
                    invoice.setId("inv-1");
                    invoice.setTenantId("t-1");
                    invoice.setTotalAmount(new java.math.BigDecimal("10.00"));
                    return invoice;
                });

        mockMvc.perform(post("/api/billing/payment/invoices/{invoiceId}/link", "inv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("https://pay.link"));
    }

    @Test
    void handleCallback_invalidSignature_returnsError() throws Exception {
        Mockito.when(paymentGatewayService.verifyPaymentCallback(eq("ALIPAY"), anyMap()))
                .thenReturn(false);

        mockMvc.perform(post("/api/billing/payment/callback/{method}", "ALIPAY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("any", "x"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void handleCallback_success_callsBillingService() throws Exception {
        Mockito.when(paymentGatewayService.verifyPaymentCallback(eq("WECHAT"), anyMap()))
                .thenReturn(true);

        mockMvc.perform(post("/api/billing/payment/callback/{method}", "WECHAT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "invoiceId", "inv-1",
                                "transactionId", "tx-1"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Mockito.verify(billingService).processPayment("inv-1", "WECHAT", "tx-1");
    }

    @Test
    void queryStatus_success() throws Exception {
        Mockito.when(paymentGatewayService.queryPaymentStatus("tx-1"))
                .thenReturn(PaymentStatus.SUCCESS);

        mockMvc.perform(get("/api/billing/payment/status/{tx}", "tx-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("SUCCESS"));
    }
}

