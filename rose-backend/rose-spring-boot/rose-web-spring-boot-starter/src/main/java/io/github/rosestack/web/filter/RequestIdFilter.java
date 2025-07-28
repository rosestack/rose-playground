package io.github.rosestack.web.filter;

import io.github.rosestack.core.util.ServletUtils;
import io.github.rosestack.web.config.WebProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求 ID 过滤器
 * <p>
 * 为每个请求生成唯一标识，支持链路追踪
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(prefix = "rose.web.filter.request-id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestIdFilter extends OncePerRequestFilter {

    private final WebProperties webProperties;

    public RequestIdFilter(WebProperties webProperties) {
        this.webProperties = webProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        WebProperties.Filter.RequestId requestId = webProperties.getFilter().getRequestId();

        String requestIdValue = ServletUtils.getHeader(requestId.getHeaderName());
        if (requestIdValue == null) {
            requestIdValue = generateRequestId();
        }

        // 设置到 MDC 中，用于日志追踪
        MDC.put(requestId.getMdcName(), requestIdValue);

        // 设置响应头
        response.setHeader(requestId.getHeaderName(), requestIdValue);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清理 MDC
            MDC.remove(requestId.getMdcName());
        }
    }

    private static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}