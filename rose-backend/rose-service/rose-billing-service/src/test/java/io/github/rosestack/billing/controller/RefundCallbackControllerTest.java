package io.github.rosestack.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefundCallbackControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RefundService refundService;
    private io.github.rosestack.billing.payment.PaymentGatewayService gatewayService;

    @BeforeEach
    void setUp() {
        refundService = Mockito.mock(RefundService.class);
        gatewayService = Mockito.mock(io.github.rosestack.billing.payment.PaymentGatewayService.class);
        RefundController controller = new RefundController(refundService, gatewayService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void handleRefundCallback_ok() throws Exception {
        Mockito.when(gatewayService.verifyRefundCallback(eq("ALIPAY"), any(Map.class)))
                .thenReturn(true);
        Mockito.when(refundService.processRefundCallback(eq("ALIPAY"), any(Map.class)))
                .thenReturn(true);

        String body = objectMapper.writeValueAsString(Map.of(
                "invoiceId", "inv-1",
                "refund_id", "rf-1",
                "timestamp", System.currentTimeMillis() / 1000));

        mockMvc.perform(post("/api/billing/refund/callback/{paymentMethod}", "ALIPAY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void handleRefundCallback_invalid() throws Exception {
        Mockito.when(gatewayService.verifyRefundCallback(eq("WECHAT"), any(Map.class)))
                .thenReturn(false);

        String body = objectMapper.writeValueAsString(Map.of("any", "x"));
        mockMvc.perform(post("/api/billing/refund/callback/{paymentMethod}", "WECHAT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code")
                        .value(io.github.rosestack.billing.enums.BillingErrorCode.INVALID_REFUND_CALLBACK.getCode()));
    }
}
