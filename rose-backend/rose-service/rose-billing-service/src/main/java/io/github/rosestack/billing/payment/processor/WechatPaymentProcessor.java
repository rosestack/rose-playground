package io.github.rosestack.billing.payment.processor;

import io.github.rosestack.billing.dto.PaymentRequest;
import io.github.rosestack.billing.dto.PaymentResult;
import io.github.rosestack.billing.dto.RefundResult;
import io.github.rosestack.billing.payment.PaymentProcessor;
import io.github.rosestack.billing.payment.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付处理器具体实现
 *
 * @author rose
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rose.billing.payment.wechat.enabled", havingValue = "true")
public class WechatPaymentProcessor implements PaymentProcessor {

    @Value("${rose.billing.payment.wechat.app-id:}")
    private String appId;

    @Value("${rose.billing.payment.wechat.mch-id:}")
    private String mchId;

    @Value("${rose.billing.payment.wechat.api-key:}")
    private String apiKey;

    // 轻量回调校验配置
    @Value("${rose.billing.payment.wechat.hmac-secret:}")
    private String hmacSecret;

    @Value("${rose.billing.payment.wechat.allowed-skew-seconds:300}")
    private long allowedSkewSeconds;

    private static String hmacSha256Hex(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPaymentMethod() {
        return "WECHAT";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            log.info("处理微信支付：账单 {}, 金额 {}", request.getInvoiceId(), request.getAmount());

            // TODO: 集成微信支付SDK
            // WXPayConfig config = new WXPayConfig() {
            //     // 实现配置方法
            // };
            // WXPay wxpay = new WXPay(config);
            // Map<String, String> data = new HashMap<String, String>();
            // data.put("body", "Invoice Payment");
            // data.put("out_trade_no", request.getInvoiceId());
            // data.put("device_info", "WEB");
            // data.put("fee_type", "CNY");
            // data.put("total_fee", String.valueOf(request.getAmount().multiply(new
            // BigDecimal("100")).intValue()));
            // data.put("spbill_create_ip", "127.0.0.1");
            // data.put("notify_url", "http://www.example.com/wxpay/notify");
            // data.put("trade_type", "NATIVE");
            // Map<String, String> resp = wxpay.unifiedOrder(data);

            // 模拟支付成功
            String transactionId = "wechat_" + System.currentTimeMillis();
            Map<String, Object> response = new HashMap<>();
            response.put("transaction_id", transactionId);
            response.put("trade_state", "SUCCESS");

            PaymentResult result = PaymentResult.success(transactionId);
            result.setGatewayResponse(response);

            log.info("微信支付成功：{}", transactionId);
            return result;

        } catch (Exception e) {
            log.error("微信支付失败：{}", request.getInvoiceId(), e);
            return PaymentResult.failed("微信支付失败：" + e.getMessage());
        }
    }

    @Override
    public String createPaymentLink(String invoiceId, BigDecimal amount) {
        try {
            log.info("创建微信支付链接：账单 {}, 金额 {}", invoiceId, amount);

            // TODO: 创建微信支付二维码
            // 通过统一下单接口获取code_url，然后生成二维码

            // 模拟返回支付链接
            return "weixin://wxpay/bizpayurl?pr=" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("创建微信支付链接失败：{}", invoiceId, e);
            throw new RuntimeException("创建微信支付链接失败", e);
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
                    log.warn("微信回调超出时间窗: now={}, ts={}, skew={}", now, ts, allowedSkewSeconds);
                    return false;
                }
            }

            Object signObj = callbackData.get("sign");
            if (signObj != null && hmacSecret != null && !hmacSecret.isEmpty()) {
                String payload = String.valueOf(callbackData.getOrDefault("invoiceId", ""))
                        + "|"
                        + String.valueOf(callbackData.getOrDefault("transaction_id", ""))
                        + "|"
                        + String.valueOf(callbackData.getOrDefault("timestamp", ""));
                String expected = hmacSha256Hex(payload, hmacSecret);
                if (!expected.equals(signObj.toString())) {
                    log.warn("微信回调HMAC校验失败");
                    return false;
                }
            }

            return callbackData.containsKey("transaction_id") && callbackData.containsKey("result_code");

        } catch (Exception e) {
            log.error("验证微信支付回调失败", e);
            return false;
        }
    }

    @Override
    public RefundResult processRefund(String transactionId, BigDecimal amount, String reason) {
        try {
            log.info("处理微信退款：交易 {}, 金额 {}", transactionId, amount);

            // TODO: 调用微信退款API
            // Map<String, String> data = new HashMap<String, String>();
            // data.put("transaction_id", transactionId);
            // data.put("out_refund_no", "refund_" + System.currentTimeMillis());
            // data.put("total_fee", String.valueOf(amount.multiply(new BigDecimal("100")).intValue()));
            // data.put("refund_fee", String.valueOf(amount.multiply(new BigDecimal("100")).intValue()));
            // Map<String, String> resp = wxpay.refund(data);

            // 模拟退款成功
            String refundId = "wechat_refund_" + System.currentTimeMillis();

            log.info("微信退款成功：{}", refundId);
            return RefundResult.success(refundId);

        } catch (Exception e) {
            log.error("微信退款失败：{}", transactionId, e);
            return RefundResult.failed("微信退款失败：" + e.getMessage());
        }
    }

    @Override
    public boolean verifyRefundCallback(Map<String, Object> callbackData) {
        try {
            Object tsObj = callbackData.get("timestamp");
            if (tsObj != null) {
                long ts = Long.parseLong(tsObj.toString());
                long now = System.currentTimeMillis() / 1000;
                if (Math.abs(now - ts) > allowedSkewSeconds) return false;
            }
            Object signObj = callbackData.get("sign");
            if (signObj != null && hmacSecret != null && !hmacSecret.isEmpty()) {
                String payload = String.valueOf(callbackData.getOrDefault("invoiceId", ""))
                        + "|"
                        + String.valueOf(callbackData.getOrDefault("refund_id", ""))
                        + "|"
                        + String.valueOf(callbackData.getOrDefault("timestamp", ""));
                String expected = hmacSha256Hex(payload, hmacSecret);
                return expected.equals(signObj.toString());
            }
            return true;
        } catch (Exception e) {
            log.error("验证微信退款回调失败", e);
            return false;
        }
    }

    @Override
    public boolean isRefundSuccess(java.util.Map<String, Object> data) {
        String s = String.valueOf(data.getOrDefault("refund_status", data.getOrDefault("status", "")))
                .toUpperCase();
        return "SUCCESS".equals(s);
    }

    @Override
    public java.math.BigDecimal parseRefundAmount(java.util.Map<String, Object> data) {
        Object rf = data.get("refund_fee");
        if (rf != null) {
            try {
                return new java.math.BigDecimal(rf.toString()).movePointLeft(2);
            } catch (Exception ignored) {
            }
        }
        return PaymentProcessor.super.parseRefundAmount(data);
    }

    @Override
    public PaymentStatus queryPaymentStatus(String transactionId) {
        try {
            // TODO: 查询微信支付状态
            // Map<String, String> data = new HashMap<String, String>();
            // data.put("transaction_id", transactionId);
            // Map<String, String> resp = wxpay.orderQuery(data);

            // 模拟返回成功状态
            return PaymentStatus.SUCCESS;

        } catch (Exception e) {
            log.error("查询微信支付状态失败：{}", transactionId, e);
            return PaymentStatus.UNKNOWN;
        }
    }
}
