package io.github.rosestack.web.config;

import io.github.rosestack.core.annotation.RoseStarter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

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
        EnvironmentConfig.class,
        SwaggerConfig.class
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@RoseStarter(module = "web", properties = RoseWebProperties.class)
public class RoseWebAutoConfiguration {

} 