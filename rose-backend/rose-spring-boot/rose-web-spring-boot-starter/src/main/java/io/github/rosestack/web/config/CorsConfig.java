package io.github.rosestack.web.config;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CorsConfig {
    private final RoseWebProperties roseWebProperties;

    public CorsConfig(RoseWebProperties roseWebProperties) {
        this.roseWebProperties = roseWebProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("启用跨域资源共享（CORS）配置");
        CorsConfiguration cors = roseWebProperties.getCors();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
} 