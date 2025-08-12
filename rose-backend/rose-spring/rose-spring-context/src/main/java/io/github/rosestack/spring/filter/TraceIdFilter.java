package io.github.rosestack.spring.filter;

import static io.github.rosestack.core.Constants.HeaderName.HEADER_TRACE_ID;
import static io.github.rosestack.core.Constants.MdcName.MDC_REQUEST_ID;

import io.github.rosestack.core.Constants;
import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * 请求 ID 过滤器
 *
 * <p>为每个请求生成唯一标识，支持链路追踪
 *
 * @author rosestack
 * @since 1.0.0
 */
public class TraceIdFilter extends AbstractBaseFilter {

    public TraceIdFilter(String[] excludePaths) {
        super(excludePaths);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = ServletUtils.getTraceId();
        if (requestId == null) {
            requestId = generateRequestId();
            MDC.put(Constants.MdcName.MDC_REQUEST_ID, requestId);
        }
        // 设置响应头
        response.setHeader(HEADER_TRACE_ID, requestId);

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
