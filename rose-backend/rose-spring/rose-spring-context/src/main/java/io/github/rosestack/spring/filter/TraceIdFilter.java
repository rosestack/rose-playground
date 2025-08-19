package io.github.rosestack.spring.filter;

import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;

import static io.github.rosestack.spring.util.ServletUtils.HEADER_REQUEST_ID;

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
		String requestId = ServletUtils.getRequestId();
		MDC.put(HEADER_REQUEST_ID, requestId);
		response.setHeader(HEADER_REQUEST_ID, requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(HEADER_REQUEST_ID);
		}
	}
}
