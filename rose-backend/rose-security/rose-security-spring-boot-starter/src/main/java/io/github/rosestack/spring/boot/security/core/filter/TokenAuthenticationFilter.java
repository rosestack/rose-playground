package io.github.rosestack.spring.boot.security.core.filter;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

        String token = extractTokenFromRequest(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 验证Token并获取用户信息
            if (tokenService.validateToken(token)) {
                UserDetails userDetails = tokenService.getUserDetails(token);

                if (userDetails != null) {
                    // 创建认证Token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到Spring Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("已为用户 {} 设置认证上下文", userDetails.getUsername());
                }
            } else {
                log.debug("Token无效: {}", StringUtils.abbreviate(token, 8));
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取Token
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token != null) {
            return token;
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String loginPath = properties.getAuth().getLoginPath();
        String logoutPath = properties.getAuth().getLogoutPath();
        String refreshPath = properties.getAuth().getRefreshPath();

        if (path.equals(loginPath) || path.equals(logoutPath) || path.equals(refreshPath)) {
            return true;
        }

        // 配置化的公共端点放行
        String[] permitPaths = properties.getAuth().getPermitPaths();
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
