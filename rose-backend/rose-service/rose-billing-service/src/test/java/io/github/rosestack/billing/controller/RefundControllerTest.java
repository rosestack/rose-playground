package io.github.rosestack.billing.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.service.RefundService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RefundControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = Mockito.mock(RefundService.class);
        io.github.rosestack.billing.payment.PaymentGatewayService gatewayService =
                Mockito.mock(io.github.rosestack.billing.payment.PaymentGatewayService.class);
        RefundController controller = new RefundController(refundService, gatewayService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void requestRefund_success() throws Exception {
        Mockito.when(refundService.requestRefund(eq("inv-1"), eq(new BigDecimal("10.00")), anyString(), any()))
                .thenReturn(RefundResult.success("rf-1"));

        String body = objectMapper.writeValueAsString(new RefundController.RefundRequest() {
            {
                setInvoiceId("inv-1");
                setAmount(new BigDecimal("10.00"));
                setReason("test");
            }
        });

        mockMvc.perform(post("/api/billing/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void requestRefund_invalidAmount() throws Exception {
        String body = objectMapper.writeValueAsString(new RefundController.RefundRequest() {
            {
                setInvoiceId("inv-1");
                setAmount(new BigDecimal("0"));
                setReason("test");
            }
        });

        mockMvc.perform(post("/api/billing/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(io.github.rosestack.core.model.ErrorCode.BAD_REQUEST.getCode()));
    }
}
