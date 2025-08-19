package io.github.rosestack.spring.boot.web.config;

import io.github.rosestack.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.web.advice.ApiResponseBodyAdvice;
import io.github.rosestack.spring.boot.web.exception.ExceptionHandlerHelper;
import io.github.rosestack.spring.boot.web.exception.GlobalExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Web 自动配置
 *
 * <p>提供 Web 相关的自动配置功能
 *
 * @author rosestack
 * @since 1.0.0
 */
@Import({
	CorsConfig.class,
	AsyncConfig.class,
	MessageConfig.class,
	MetricConfig.class,
	WebMvcConfig.class,
	SwaggerConfig.class,
	// 精准引入组件（替代包扫描）
	ApiResponseBodyAdvice.class,
	GlobalExceptionHandler.class,
	ExceptionHandlerHelper.class
})
@Slf4j
@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(RoseWebProperties.class)
@ConditionalOnProperty(prefix = "rose.web", name = "enabled", havingValue = "true", matchIfMissing = true)
@PropertySource(value = "classpath:application-rose-web.yaml", factory = YmlPropertySourceFactory.class)
public class RoseWebAutoConfiguration {

	@PostConstruct
	public void init() {
		log.info("Rose Web 自动配置已启用");
	}
}
