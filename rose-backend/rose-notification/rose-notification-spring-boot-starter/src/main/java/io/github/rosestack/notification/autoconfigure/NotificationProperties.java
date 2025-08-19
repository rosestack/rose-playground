package io.github.rosestack.notification.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rose.notification")
public class NotificationProperties {
	/**
	 * 是否开启 Sender 层重试包装
	 */
	private boolean retryable = false;

	/**
	 * 线程池大小（用于 sendAsync / 批量）
	 */
	private int executorCoreSize = Runtime.getRuntime().availableProcessors();

	/**
	 * 发送器缓存最大容量
	 */
	private long senderCacheMaxSize = 1000;

	/**
	 * 发送器缓存访问过期（秒）
	 */
	private long senderCacheExpireAfterAccessSeconds = 1800;

	/**
	 * 短信服务商缓存最大容量
	 */
	private long smsProviderCacheMaxSize = 1000;

	/**
	 * 短信服务商缓存访问过期（秒）
	 */
	private long smsProviderCacheExpireAfterAccessSeconds = 1800;

	public boolean isRetryable() {
		return retryable;
	}

	public void setRetryable(boolean retryable) {
		this.retryable = retryable;
	}

	public int getExecutorCoreSize() {
		return executorCoreSize;
	}

	public void setExecutorCoreSize(int executorCoreSize) {
		this.executorCoreSize = executorCoreSize;
	}

	public long getSenderCacheMaxSize() {
		return senderCacheMaxSize;
	}

	public void setSenderCacheMaxSize(long senderCacheMaxSize) {
		this.senderCacheMaxSize = senderCacheMaxSize;
	}

	public long getSenderCacheExpireAfterAccessSeconds() {
		return senderCacheExpireAfterAccessSeconds;
	}

	public void setSenderCacheExpireAfterAccessSeconds(long senderCacheExpireAfterAccessSeconds) {
		this.senderCacheExpireAfterAccessSeconds = senderCacheExpireAfterAccessSeconds;
	}

	public long getSmsProviderCacheMaxSize() {
		return smsProviderCacheMaxSize;
	}

	public void setSmsProviderCacheMaxSize(long smsProviderCacheMaxSize) {
		this.smsProviderCacheMaxSize = smsProviderCacheMaxSize;
	}

	public long getSmsProviderCacheExpireAfterAccessSeconds() {
		return smsProviderCacheExpireAfterAccessSeconds;
	}

	public void setSmsProviderCacheExpireAfterAccessSeconds(long smsProviderCacheExpireAfterAccessSeconds) {
		this.smsProviderCacheExpireAfterAccessSeconds = smsProviderCacheExpireAfterAccessSeconds;
	}
}
