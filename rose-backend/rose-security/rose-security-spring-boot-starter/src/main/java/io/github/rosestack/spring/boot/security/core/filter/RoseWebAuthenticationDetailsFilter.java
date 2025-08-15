package io.github.rosestack.spring.boot.security.core.filter;

import io.github.rosestack.spring.boot.security.core.support.RoseWebAuthenticationDetails;
import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rose Web 认证详情过滤器
 *
 * <p>为每个请求创建 RoseWebAuthenticationDetails 对象并存储到 request attribute 中，
 * 确保整个请求生命周期中都能获取到统一的认证详情信息。
 * 
 * <p>该过滤器在 Spring Security 过滤器链之前执行，为后续的认证和授权流程提供上下文信息。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RoseWebAuthenticationDetailsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        String requestId = ServletUtils.getRequestId();

        log.info("Rose Web 认证详情过滤器开始: {} {} [{}]", method, requestPath, requestId);

        try {
            // 创建 RoseWebAuthenticationDetails 对象
            RoseWebAuthenticationDetails authDetails = RoseWebAuthenticationDetails.fromRequest(request);
            
            // 存储到 request attribute 中
            request.setAttribute(RoseWebAuthenticationDetails.REQUEST_ATTRIBUTE_KEY, authDetails);
            
            log.info("已创建并存储 RoseWebAuthenticationDetails: {} {} [{}]", method, requestPath, requestId);

            // 继续过滤器链
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Rose Web 认证详情过滤器处理异常: {} {} [{}]", method, requestPath, requestId, e);
            throw e;
        } finally {
            // 记录过滤器完成时间（仅在 DEBUG 级别）
            if (log.isDebugEnabled()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                log.debug("Rose Web 认证详情过滤器完成: {} {} [{}] - 耗时: {}ms", method, requestPath, requestId, duration);
            }
        }
    }
}
