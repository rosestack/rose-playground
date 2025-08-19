package io.github.rosestack.notice.sender;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.SenderConfiguration;
import io.github.rosestack.notice.spi.AbstractConfigure;
import io.github.rosestack.notice.spi.Sender;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Sender 委托类，内置可配置重试策略。
 */
public class RetryableSender extends AbstractConfigure implements Sender {
	private final Sender delegate;
	private volatile RetryPolicy retryPolicy;

	public RetryableSender(Sender delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getChannelType() {
		return delegate.getChannelType();
	}

	@Override
	public String send(SendRequest request) {
		int attempt = 1;
		for (; ; ) {
			try {
				return delegate.send(request);
			} catch (RuntimeException ex) {
				RetryPolicy policy = this.retryPolicy;
				if (policy == null || !policy.shouldRetry(attempt, ex)) {
					throw ex;
				}
				long delayMillis = Math.max(0L, policy.nextDelayMillis(attempt));
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(delayMillis));
				attempt++;
			}
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doConfigure(SenderConfiguration config) throws Exception {
		// 允许通过配置覆盖默认策略
		int maxAttempts = 3;
		long initialDelay = 200L;
		long jitter = 100L;
		if (config != null && config.getConfig() != null) {
			Object attempts = config.getConfig().get("retry.maxAttempts");
			Object delay = config.getConfig().get("retry.initialDelayMillis");
			Object jitterCfg = config.getConfig().get("retry.jitterMillis");
			if (attempts != null) {
				try {
					maxAttempts = Math.max(1, Integer.parseInt(String.valueOf(attempts)));
				} catch (Exception ignored) {
				}
			}
			if (delay != null) {
				try {
					initialDelay = Math.max(0L, Long.parseLong(String.valueOf(delay)));
				} catch (Exception ignored) {
				}
			}
			if (jitterCfg != null) {
				try {
					jitter = Math.max(0L, Long.parseLong(String.valueOf(jitterCfg)));
				} catch (Exception ignored) {
				}
			}
		}
		this.retryPolicy = new ExponentialBackoffRetryPolicy(maxAttempts, initialDelay, jitter);
	}
}
