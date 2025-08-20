package io.github.rosestack.spring.boot.notify.config;

import io.github.rosestack.notify.NotifyMetrics;
import io.github.rosestack.notify.NotifyService;
import io.github.rosestack.notify.support.CaffeineIdempotencyStore;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public NotifyService noticeService(NotifyProperties props) {
        NotifyService service = new NotifyService();
        // 默认提供一个内存幂等（可被覆盖）
        service.setIdempotencyStore(new CaffeineIdempotencyStore());
        service.setRetryable(props.isRetryable());
        // 配置工厂缓存系统属性（与核心工厂读取规则一致）
        System.setProperty("rose.notify.sender.cache.maxSize", String.valueOf(props.getSenderCacheMaxSize()));
        System.setProperty(
                "rose.notify.sender.cache.expireAfterAccessSeconds",
                String.valueOf(props.getSenderCacheExpireAfterAccessSeconds()));
        System.setProperty(
                "rose.notify.smsProvider.cache.maxSize", String.valueOf(props.getSmsProviderCacheMaxSize()));
        System.setProperty(
                "rose.notify.smsProvider.cache.expireAfterAccessSeconds",
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
    public NotifyMetrics noticeMetrics(MeterRegistry meterRegistry, NotifyService service) {
        NotifyMetrics metrics = new NotifyMetrics(meterRegistry);
        service.setMetrics(metrics);
        return metrics;
    }
}
