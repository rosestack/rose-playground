package com.company.todo.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 将当前 traceId 写入响应头，便于客户端与日志关联，同时也便于手工校验 MDC 透传效果。
 */
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingConfig {

    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Bean
    TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> traceIdResponseHeaderFilter(Tracer tracer) {
        OncePerRequestFilter delegate = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // 在链路追踪过滤器之后执行：此时已存在当前 Span，可直接读取 traceId
                String traceId = null;
                if (tracer != null
                        && tracer.currentSpan() != null
                        && tracer.currentSpan().context() != null) {
                    traceId = tracer.currentSpan().context().traceId();
                }
                if (traceId == null || traceId.isEmpty()) {
                    traceId = MDC.get("traceId");
                }
                if (traceId != null && !traceId.isEmpty()) {
                    response.setHeader(HEADER_TRACE_ID, traceId);
                }
                filterChain.doFilter(request, response);
            }
        };
        FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>(delegate);
        bean.setName("traceIdHeaderFilter");
        bean.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return bean;
    }
}
