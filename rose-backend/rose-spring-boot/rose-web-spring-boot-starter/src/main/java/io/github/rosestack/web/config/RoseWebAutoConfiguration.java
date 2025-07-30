package io.github.rosestack.web.config;

import io.github.rosestack.core.spring.YmlPropertySourceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Web 自动配置
 * <p>
 * 提供 Web 相关的自动配置功能
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */

@Import({
        CorsConfig.class,
        JacksonConfig.class,
        AsyncConfig.class,
        MessageConfig.class,
        MetricConfig.class,
        WebMvcConfig.class,
        SwaggerConfig.class
})
@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(RoseWebProperties.class)
@ComponentScan(basePackages = "io.github.rosestack.web")
@ConditionalOnProperty(prefix = "rose.web", name = "enabled", havingValue = "true", matchIfMissing = true)
@PropertySource(value = "classpath:application-rose-web.yml", factory = YmlPropertySourceFactory.class)
public class RoseWebAutoConfiguration {

}