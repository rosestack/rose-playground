package io.github.rosestack.spring.boot.web.config;

import io.github.rosestack.core.Constants;
import io.github.rosestack.core.spring.SpringContextUtils;
import io.github.rosestack.spring.boot.web.filter.CachingRequestFilter;
import io.github.rosestack.spring.boot.web.filter.TraceIdFilter;
import io.github.rosestack.spring.boot.web.filter.XssFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.github.rosestack.core.Constants.FilterOrder.CACHING_REQUEST_FILTER_ORDER;
import static io.github.rosestack.core.Constants.FilterOrder.XSS_FILTER_ORDER;

/**
 * Web MVC 配置
 * <p>
 * 配置拦截器、处理器等 Web MVC 相关功能
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final RoseWebProperties roseWebProperties;

    @Bean
    @ConditionalOnProperty(prefix = "rose.web.filter.request-id", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdFilter> requestIdFilter() {
        return SpringContextUtils.createFilterBean(new TraceIdFilter(roseWebProperties), Constants.FilterOrder.REQUEST_FILTER_ORDER);
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.web.filter.caching-request", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CachingRequestFilter> cachingRequestFilter() {
        return SpringContextUtils.createFilterBean(new CachingRequestFilter(roseWebProperties), CACHING_REQUEST_FILTER_ORDER);
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.web.filter.xss", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<XssFilter> xxsFilter() {
        return SpringContextUtils.createFilterBean(new XssFilter(roseWebProperties), XSS_FILTER_ORDER);
    }
}