package io.github.rosestack.spring.boot.security.protect;

import io.github.rosestack.core.util.ApiResponse;
import io.github.rosestack.core.util.JsonUtils;
import io.github.rosestack.spring.boot.security.config.SecurityProperties;
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

public class AccessListFilter extends OncePerRequestFilter {

    private final AccessListMatcher matcher;
    private final SecurityProperties properties;

    public AccessListFilter(AccessListMatcher matcher, SecurityProperties properties) {
        this.matcher = matcher;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.getProtect().getAccessList().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (!matcher.isAllowed(request, username)) {
            response.setStatus(403);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JsonUtils.toString(ApiResponse.error(40300, "forbidden by access list")));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
