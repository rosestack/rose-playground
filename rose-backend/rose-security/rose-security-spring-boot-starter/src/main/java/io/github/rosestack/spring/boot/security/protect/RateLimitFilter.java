package io.github.rosestack.spring.boot.security.protect;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.core.util.JsonUtils;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter limiter;
    private final RoseSecurityProperties properties;

    public RateLimitFilter(RateLimiter limiter, RoseSecurityProperties properties) {
        this.limiter = limiter;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.getProtect().getRateLimit().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (!limiter.allow(request, username)) {
            response.setStatus(429);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JsonUtils.toString(ApiResponse.error(42900, "too many requests")));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
