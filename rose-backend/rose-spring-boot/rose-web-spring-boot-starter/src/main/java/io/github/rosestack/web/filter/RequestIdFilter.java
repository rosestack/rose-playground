package io.github.rosestack.web.filter;

import io.github.rosestack.core.Constants;
import io.github.rosestack.core.util.ServletUtils;
import io.github.rosestack.web.config.RoseWebProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static io.github.rosestack.core.Constants.HeaderName.HEADER_REQUEST_ID;
import static io.github.rosestack.core.Constants.MdcName.MDC_REQUEST_ID;

/**
 * 请求 ID 过滤器
 * <p>
 * 为每个请求生成唯一标识，支持链路追踪
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public class RequestIdFilter extends OncePerRequestFilter {

    private final RoseWebProperties roseWebProperties;

    public RequestIdFilter(RoseWebProperties roseWebProperties) {
        this.roseWebProperties = roseWebProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = ServletUtils.getCurrentRequestId();
        if (requestId == null) {
            requestId = generateRequestId();
            MDC.put(Constants.MdcName.MDC_REQUEST_ID, requestId);
        }
        // 设置响应头
        response.setHeader(HEADER_REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    protected static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}