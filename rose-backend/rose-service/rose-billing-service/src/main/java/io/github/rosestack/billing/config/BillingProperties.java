package io.github.rosestack.billing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * 计费配置属性
 */
@ConfigurationProperties(prefix = "rose.billing")
@Data
public class BillingProperties {

    /**
     * 是否启用计费功能
     */
    private boolean enabled = true;

    /**
     * 默认货币单位
     */
    private String defaultCurrency = "USD";

    /**
     * 默认税率
     */
    private BigDecimal defaultTaxRate = new BigDecimal("0.1");

    /**
     * 账单到期天数
     */
    private int invoiceDueDays = 30;

    /**
     * 逾期宽限期（天）
     */
    private int overdueGraceDays = 3;

    /**
     * 使用量数据保留天数
     */
    private int usageDataRetentionDays = 180;

    /**
     * 支付配置
     */
    private PaymentConfig payment = new PaymentConfig();

    /**
     * 通知配置
     */
    private NotificationConfig notification = new NotificationConfig();

    @Data
    public static class PaymentConfig {
        /**
         * Stripe配置
         */
        private StripeConfig stripe = new StripeConfig();

        /**
         * 支付宝配置
         */
        private AlipayConfig alipay = new AlipayConfig();

        @Data
        public static class StripeConfig {
            private boolean enabled = false;
            private String publicKey;
            private String secretKey;
            private String webhookSecret;
        }

        @Data
        public static class AlipayConfig {
            private boolean enabled = false;
            private String appId;
            private String privateKey;
            private String publicKey;
        }
    }

    @Data
    public static class NotificationConfig {
        /**
         * 是否启用邮件通知
         */
        private boolean emailEnabled = true;

        /**
         * 是否启用短信通知
         */
        private boolean smsEnabled = false;

        /**
         * 账单生成通知模板
         */
        private String invoiceGeneratedTemplate = "INVOICE_GENERATED";

        /**
         * 支付确认通知模板
         */
        private String paymentConfirmationTemplate = "PAYMENT_CONFIRMATION";

        /**
         * 逾期通知模板
         */
        private String overdueNotificationTemplate = "OVERDUE_NOTIFICATION";
    }
}