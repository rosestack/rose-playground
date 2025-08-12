package io.github.rosestack.billing.payment.processor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessorRefundHooksTest {

    @Test
    void alipayHooks() {
        AlipayPaymentProcessor p = new AlipayPaymentProcessor();
        Map<String, Object> ok = new HashMap<>();
        ok.put("refund_status", "REFUND_SUCCESS");
        ok.put("refund_amount", "12.34");
        assertTrue(p.isRefundSuccess(ok));
        assertEquals(new BigDecimal("12.34"), p.parseRefundAmount(ok));
    }

    @Test
    void wechatHooks() {
        WechatPaymentProcessor p = new WechatPaymentProcessor();
        Map<String, Object> ok = new HashMap<>();
        ok.put("status", "SUCCESS");
        ok.put("refund_fee", "1234");
        assertTrue(p.isRefundSuccess(ok));
        assertEquals(new BigDecimal("12.34"), p.parseRefundAmount(ok));
    }

    @Test
    void stripeHooks() {
        StripePaymentProcessor p = new StripePaymentProcessor();
        Map<String, Object> ok = new HashMap<>();
        ok.put("status", "succeeded");
        ok.put("amount", "9.99");
        assertTrue(p.isRefundSuccess(ok));
        assertEquals(new BigDecimal("9.99"), p.parseRefundAmount(ok));
    }
}
