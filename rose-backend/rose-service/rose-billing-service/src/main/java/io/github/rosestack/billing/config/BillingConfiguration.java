package io.github.rosestack.billing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 计费系统配置类
 * <p>
 * 配置计费系统的相关Bean和特性
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Configuration
public class BillingConfiguration {

	/**
	 * 计费系统属性配置
	 */
	@Bean
	@ConfigurationProperties(prefix = "billing")
	public BillingProperties billingProperties() {
		return new BillingProperties();
	}

	/**
	 * 计费系统属性类
	 */
	@Data
	public static class BillingProperties {

		/**
		 * 默认货币单位
		 */
		private String defaultCurrency = "USD";

		/**
		 * 默认税率
		 */
		private double defaultTaxRate = 0.06;

		/**
		 * 支付超时时间（分钟）
		 */
		private int paymentTimeoutMinutes = 30;

		/**
		 * 账单生成提前天数
		 */
		private int invoiceGenerationDays = 3;

		/**
		 * 配额检查缓存时间（秒）
		 */
		private int quotaCacheSeconds = 300;

		/**
		 * 是否启用自动计费
		 */
		private boolean autoInvoiceEnabled = true;

		/**
		 * 是否启用支付提醒
		 */
		private boolean paymentReminderEnabled = true;

		/**
		 * 计费引擎配置
		 */
		private EngineConfig engine = new EngineConfig();

		/**
		 * 折扣配置
		 */
		private DiscountConfig discount = new DiscountConfig();

		/**
		 * 计费引擎配置
		 */
		@Data
		public static class EngineConfig {
			private boolean enableUsageBilling = true;
			private boolean enableQuotaCheck = true;
			private boolean enablePriceCalculation = true;
			private int batchSize = 100;
			private int retryAttempts = 3;
		}

		/**
		 * 折扣配置
		 */
		@Data
		public static class DiscountConfig {
			private double annualDiscountRate = 0.10; // 年付折扣率
			private double quantity50DiscountRate = 0.05; // 50+ 席位折扣率
			private double quantity100DiscountRate = 0.10; // 100+ 席位折扣率
			private double quantity200DiscountRate = 0.15; // 200+ 席位折扣率
			private boolean enableVipDiscount = false; // 是否启用VIP折扣
		}
	}
}
