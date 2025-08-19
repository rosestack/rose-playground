package io.github.rosestack.notification.autoconfigure;

import io.github.rosestack.notice.NoticeMetrics;
import io.github.rosestack.notice.NoticeService;
import io.github.rosestack.notice.support.CaffeineIdempotencyStore;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoConfiguration
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public NoticeService noticeService(NotificationProperties props) {
		NoticeService service = new NoticeService();
		// 默认提供一个内存幂等（可被覆盖）
		service.setIdempotencyStore(new CaffeineIdempotencyStore());
		service.setRetryable(props.isRetryable());
		// 配置工厂缓存系统属性（与核心工厂读取规则一致）
		System.setProperty("rose.notification.sender.cache.maxSize", String.valueOf(props.getSenderCacheMaxSize()));
		System.setProperty(
			"rose.notification.sender.cache.expireAfterAccessSeconds",
			String.valueOf(props.getSenderCacheExpireAfterAccessSeconds()));
		System.setProperty(
			"rose.notification.smsProvider.cache.maxSize", String.valueOf(props.getSmsProviderCacheMaxSize()));
		System.setProperty(
			"rose.notification.smsProvider.cache.expireAfterAccessSeconds",
			String.valueOf(props.getSmsProviderCacheExpireAfterAccessSeconds()));
		if (props.getExecutorCoreSize() > 0) {
			ExecutorService executor = Executors.newFixedThreadPool(props.getExecutorCoreSize());
			service.setExecutor(executor);
		}
		return service;
	}

	@Bean
	@ConditionalOnClass(MeterRegistry.class)
	@ConditionalOnBean(MeterRegistry.class)
	@ConditionalOnMissingBean
	public NoticeMetrics noticeMetrics(MeterRegistry meterRegistry, NoticeService service) {
		NoticeMetrics metrics = new NoticeMetrics(meterRegistry);
		service.setMetrics(metrics);
		return metrics;
	}
}
