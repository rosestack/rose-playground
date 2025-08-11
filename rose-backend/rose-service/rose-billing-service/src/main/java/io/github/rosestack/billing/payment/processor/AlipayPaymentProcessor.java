package io.github.rosestack.billing.payment.processor;

import io.github.rosestack.billing.dto.PaymentRequest;
import io.github.rosestack.billing.dto.PaymentResult;
import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.enums.PaymentStatus;
import io.github.rosestack.billing.payment.PaymentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付处理器具体实现
 *
 * @author rose
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rose.billing.payment.alipay.enabled", havingValue = "true")
public class AlipayPaymentProcessor implements PaymentProcessor {

    @Value("${rose.billing.payment.alipay.app-id}")
    private String appId;

    @Value("${rose.billing.payment.alipay.private-key}")
    private String privateKey;

    @Value("${rose.billing.payment.alipay.public-key}")
    private String publicKey;

    // 轻量回调校验配置（非正式SDK方案）：可选 HMAC 密钥与时间窗（秒）
    @Value("${rose.billing.payment.alipay.hmac-secret:}")
    private String hmacSecret;

    @Value("${rose.billing.payment.alipay.allowed-skew-seconds:300}")
    private long allowedSkewSeconds;

    @Override
    public String getPaymentMethod() {
        return "ALIPAY";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            log.info("处理支付宝支付：账单 {}, 金额 {}", request.getInvoiceId(), request.getAmount());

            // TODO: 集成支付宝SDK
            // AlipayClient alipayClient = new DefaultAlipayClient(
            //     "https://openapi.alipay.com/gateway.do",
            //     appId, privateKey, "json", "UTF-8", publicKey, "RSA2");
            //
            // AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
            // request.setBizContent("{" +
            //     "\"out_trade_no\":\"" + request.getInvoiceId() + "\"," +
            //     "\"total_amount\":\"" + request.getAmount() + "\"," +
            //     "\"subject\":\"Invoice Payment\"," +
            //     "\"buyer_id\":\"" + request.getTenantId() + "\"" +
            //     "}");
            // AlipayTradeCreateResponse response = alipayClient.execute(request);

            // 模拟支付成功
            String transactionId = "alipay_" + System.currentTimeMillis();
            Map<String, Object> response = new HashMap<>();
            response.put("trade_no", transactionId);
            response.put("trade_status", "TRADE_SUCCESS");

            PaymentResult result = PaymentResult.success(transactionId);
            result.setGatewayResponse(response);

            log.info("支付宝支付成功：{}", transactionId);
            return result;

        } catch (Exception e) {
            log.error("支付宝支付失败：{}", request.getInvoiceId(), e);
            return PaymentResult.failed("支付宝支付失败：" + e.getMessage());
        }
    }

    @Override
    public String createPaymentLink(String invoiceId, BigDecimal amount) {
        try {
            log.info("创建支付宝支付链接：账单 {}, 金额 {}", invoiceId, amount);

            // TODO: 创建支付宝支付链接
            // AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // request.setReturnUrl("https://yourdomain.com/return");
            // request.setNotifyUrl("https://yourdomain.com/notify");
            // request.setBizContent("{" +
            //     "\"out_trade_no\":\"" + invoiceId + "\"," +
            //     "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
            //     "\"total_amount\":\"" + amount + "\"," +
            //     "\"subject\":\"Invoice Payment\"" +
            //     "}");
            // String form = alipayClient.pageExecute(request).getBody();

            // 模拟返回支付链接
            return "https://openapi.alipay.com/gateway.do?timestamp=" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("创建支付宝支付链接失败：{}", invoiceId, e);
            throw new RuntimeException("创建支付宝支付链接失败", e);
        }
    }

    @Override
    public boolean verifyCallback(Map<String, Object> callbackData) {
        try {
            // 轻量安全校验：timestamp 时间窗 + HMAC 校验（如未配置 hmac，则降级为字段存在性校验）
            Object tsObj = callbackData.get("timestamp");
            if (tsObj != null) {
                long ts = Long.parseLong(tsObj.toString());
                long now = System.currentTimeMillis() / 1000;
                if (Math.abs(now - ts) > allowedSkewSeconds) {
                    log.warn("支付宝回调超出时间窗: now={}, ts={}, skew={}", now, ts, allowedSkewSeconds);
                    return false;
                }
            }

            Object signObj = callbackData.get("sign");
            if (signObj != null && hmacSecret != null && !hmacSecret.isEmpty()) {
                String payload = String.valueOf(callbackData.getOrDefault("invoiceId", "")) + "|" +
                        String.valueOf(callbackData.getOrDefault("trade_no", "")) + "|" +
                        String.valueOf(callbackData.getOrDefault("timestamp", ""));
                String expected = hmacSha256Hex(payload, hmacSecret);
                if (!expected.equals(signObj.toString())) {
                    log.warn("支付宝回调HMAC校验失败");
                    return false;
                }
            }

            return callbackData.containsKey("trade_no") && callbackData.containsKey("trade_status");

        } catch (Exception e) {
            log.error("验证支付宝回调失败", e);
            return false;
        }
    }

    private static String hmacSha256Hex(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RefundResult processRefund(String transactionId, BigDecimal amount, String reason) {
        try {
            log.info("处理支付宝退款：交易 {}, 金额 {}", transactionId, amount);

            // TODO: 调用支付宝退款API
            // AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            // request.setBizContent("{" +
            //     "\"trade_no\":\"" + transactionId + "\"," +
            //     "\"refund_amount\":\"" + amount + "\"," +
            //     "\"refund_reason\":\"" + reason + "\"" +
            //     "}");
            // AlipayTradeRefundResponse response = alipayClient.execute(request);

            // 模拟退款成功
            String refundId = "alipay_refund_" + System.currentTimeMillis();

            log.info("支付宝退款成功：{}", refundId);
            return RefundResult.success(refundId);

        } catch (Exception e) {
            log.error("支付宝退款失败：{}", transactionId, e);
            return RefundResult.failed("支付宝退款失败：" + e.getMessage());
        }
    }

    @Override
    public boolean verifyRefundCallback(Map<String, Object> callbackData) {
        try {
            // 轻量校验：timestamp + HMAC（如配置）同支付回调一致；后续替换为SDK
            Object tsObj = callbackData.get("timestamp");
            if (tsObj != null) {
                long ts = Long.parseLong(tsObj.toString());
                long now = System.currentTimeMillis() / 1000;
                if (Math.abs(now - ts) > allowedSkewSeconds) return false;
            }
            Object signObj = callbackData.get("sign");
            if (signObj != null && hmacSecret != null && !hmacSecret.isEmpty()) {
                String payload = String.valueOf(callbackData.getOrDefault("invoiceId", "")) + "|" +
                        String.valueOf(callbackData.getOrDefault("refund_id", "")) + "|" +
                        String.valueOf(callbackData.getOrDefault("timestamp", ""));
                String expected = hmacSha256Hex(payload, hmacSecret);
                return expected.equals(signObj.toString());
            }
            return true;
        } catch (Exception e) {
            log.error("验证支付宝退款回调失败", e);
            return false;
        }
    }

    @Override
    public boolean isRefundSuccess(java.util.Map<String, Object> data) {
        String s = String.valueOf(data.getOrDefault("refund_status", data.getOrDefault("status", ""))).toUpperCase();
        return "REFUND_SUCCESS".equals(s) || "SUCCESS".equals(s);
    }

    @Override
    public java.math.BigDecimal parseRefundAmount(java.util.Map<String, Object> data) {
        Object ra = data.get("refund_amount");
        if (ra != null) return new java.math.BigDecimal(ra.toString());
        return PaymentProcessor.super.parseRefundAmount(data);
    }


    @Override
    public PaymentStatus queryPaymentStatus(String transactionId) {
        try {
            // TODO: 查询支付宝支付状态
            // AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            // request.setBizContent("{\"trade_no\":\"" + transactionId + "\"}");
            // AlipayTradeQueryResponse response = alipayClient.execute(request);

            // 模拟返回成功状态
            return PaymentStatus.SUCCESS;

        } catch (Exception e) {
            log.error("查询支付宝支付状态失败：{}", transactionId, e);
            return PaymentStatus.UNKNOWN;
        }
    }
}
