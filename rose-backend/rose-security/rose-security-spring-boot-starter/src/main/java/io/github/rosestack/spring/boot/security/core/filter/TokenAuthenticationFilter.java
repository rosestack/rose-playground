package io.github.rosestack.spring.boot.security.core.filter;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.support.RoseAuthenticationDetailsSource;
import io.github.rosestack.spring.boot.security.core.support.RoseWebAuthenticationDetails;
import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static io.github.rosestack.spring.boot.security.core.service.TokenService.TOKEN_HEADER;

/**
 * Token 认证过滤器
 *
 * <p>从请求头中提取Token并验证用户身份，设置Spring Security上下文
 */
@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final RoseSecurityProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        String requestId = ServletUtils.getRequestId();

        log.info("请求开始: {} {} [{}]", method, requestPath, requestId);

        try {
            // 处理Token认证
            processTokenAuthentication(request);

            // 继续过滤器链
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Token认证过滤器处理异常: {} {} [{}]", method, requestPath, requestId, e);
            throw e;
        } finally {
            // 清理上下文和记录请求完成时间
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("请求完成: {} {} [{}] - 耗时: {}ms", method, requestPath, requestId, duration);

            // 清理Spring Security上下文
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 处理Token认证逻辑
     *
     * @param request 请求对象
     */
    private void processTokenAuthentication(HttpServletRequest request) {
        String token = ServletUtils.getRequestHeader(TOKEN_HEADER);

        // 创建认证详细信息对象，用于记录整个认证过程
        RoseWebAuthenticationDetails authDetails = new RoseAuthenticationDetailsSource().buildDetails(request);

        if (StringUtils.isBlank(token)) {
            log.debug("请求中未找到Token: {}", request.getRequestURI());
            authDetails.markAuthFailure("NO_TOKEN", "请求中未找到Token");
            return;
        }

        // 检查Spring Security上下文是否已设置认证
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Spring Security上下文已存在认证信息，跳过Token处理");
            authDetails.markAuthSuccess();
            return;
        }

        try {
            // 验证Token
            if (!tokenService.validateToken(token)) {
                log.warn("Token验证失败: {}", StringUtils.abbreviate(token, 8));
                authDetails.markAuthFailure("INVALID_TOKEN", "Token验证失败");
                return;
            }

            // 获取用户详细信息
            UserDetails userDetails = tokenService.getUserDetails(token);
            if (userDetails == null) {
                log.warn("无法从Token获取用户信息: {}", StringUtils.abbreviate(token, 8));
                authDetails.markAuthFailure("USER_NOT_FOUND", "无法从Token获取用户信息");
                return;
            }

            // 设置用户信息
            authDetails.setUsername(userDetails.getUsername());
            authDetails.markAuthSuccess();

            // 创建Spring Security认证Token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 设置认证详细信息
            authToken.setDetails(authDetails);

            // 设置到Spring Security上下文
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("已为用户 {} 设置认证上下文", userDetails.getUsername());

        } catch (Exception e) {
            log.error("处理Token认证时发生异常: {}", StringUtils.abbreviate(token, 8), e);

            // 记录异常信息到认证详细信息中
            authDetails.markAuthFailure("AUTH_EXCEPTION", "认证处理异常").withException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String loginPath = properties.getLoginPath();
        String logoutPath = properties.getLogoutPath();
        String refreshPath = properties.getRefreshPath();

        if (path.equals(loginPath) || path.equals(logoutPath) || path.equals(refreshPath)) {
            return true;
        }

        // 配置化的公共端点放行
        String[] permitPaths = properties.getPermitPaths();
        if (permitPaths != null && permitPaths.length > 0) {
            AntPathMatcher matcher = new AntPathMatcher();
            for (String pattern : permitPaths) {
                if (matcher.match(pattern, path)) {
                    return true;
                }
            }
        }
        return false;
    }
}
