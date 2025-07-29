package io.github.rosestack.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 跨域配置
 * <p>
 * 提供跨域资源共享（CORS）配置
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public class CorsConfig {
    private final RoseWebProperties roseWebProperties;

    public CorsConfig(RoseWebProperties roseWebProperties) {
        this.roseWebProperties = roseWebProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = roseWebProperties.getCors();

        CorsConfiguration configuration = new CorsConfiguration();

        // 设置允许的源
        for (String origin : cors.getAllowedOrigins()) {
            configuration.addAllowedOrigin(origin);
        }

        // 设置允许的方法
        for (String method : cors.getAllowedMethods()) {
            configuration.addAllowedMethod(method);
        }

        // 设置允许的请求头
        for (String header : cors.getAllowedHeaders()) {
            configuration.addAllowedHeader(header);
        }

        // 设置是否允许携带凭证
        configuration.setAllowCredentials(cors.getAllowCredentials());

        // 设置预检请求的有效期
        configuration.setMaxAge(cors.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
} 