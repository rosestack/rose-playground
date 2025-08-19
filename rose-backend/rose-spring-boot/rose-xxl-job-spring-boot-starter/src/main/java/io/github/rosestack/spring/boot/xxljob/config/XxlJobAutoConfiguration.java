package io.github.rosestack.spring.boot.xxljob.config;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.rosestack.spring.boot.xxljob.aspect.XxlJobMetricAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * XXL-Job 自动配置
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnClass({XxlJobExecutor.class, XxlJobSpringExecutor.class})
@Import(XxlJobClientConfiguration.class)
@ConditionalOnProperty(prefix = "rose.xxl-job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties props, Environment env) {
		// 解析 appName：优先使用 rose.xxl-job.appname，其次 spring.application.name
		String appName = getAppName(props, env);
		if (!StringUtils.hasText(appName)) {
			throw new IllegalStateException("xxl-job appname 未配置，且 spring.application.name 也为空，请至少配置其中一个");
		}

		log.info("xxl-job.executor.config: appName={}, adminAddresses={}", appName, props.getAdminAddresses());
		XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
		executor.setAdminAddresses(props.getAdminAddresses());
		executor.setAppname(appName);
		executor.setAddress(props.getAddress());
		executor.setIp(props.getIp());
		executor.setPort(props.getPort());
		executor.setAccessToken(props.getAccessToken());
		executor.setLogPath(props.getLogPath());
		executor.setLogRetentionDays(props.getLogRetentionDays());
		return executor;
	}

	@Bean
	@ConditionalOnMissingBean(name = "xxlJobHealthIndicator")
	public HealthIndicator xxlJobHealthIndicator(XxlJobProperties props, Environment env) {
		return () -> {
			// 基础：无主动探测，仅报告 UP（装配成功）及静态信息
			Health.Builder builder = Health.up()
				.withDetail("appName", getAppName(props, env))
				.withDetail("port", props.getPort())
				.withDetail("adminAddresses", props.getAdminAddresses());
			return builder.build();
		};
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "rose.xxl-job.metrics", name = "enabled", havingValue = "true")
	@ConditionalOnClass({MeterRegistry.class, XxlJob.class})
	XxlJobMetricAspect xxlJobMetricsAspect(MeterRegistry registry) {
		return new XxlJobMetricAspect(registry);
	}

	private static String getAppName(XxlJobProperties props, Environment env) {
		String app = props.getAppname();
		if (!StringUtils.hasText(app)) {
			app = env.getProperty("spring.application.name");
		}
		return app;
	}
}

