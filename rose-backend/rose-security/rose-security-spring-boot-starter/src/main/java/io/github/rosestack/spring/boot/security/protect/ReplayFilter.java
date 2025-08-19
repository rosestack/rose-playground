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
import org.springframework.web.filter.OncePerRequestFilter;

public class ReplayFilter extends OncePerRequestFilter {

    private final ReplayProtection protection;
    private final RoseSecurityProperties properties;

    public ReplayFilter(ReplayProtection protection, RoseSecurityProperties properties) {
        this.protection = protection;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.getProtect().getReplay().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!protection.check(request)) {
            response.setStatus(400);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JsonUtils.toString(ApiResponse.error(40010, "replay detected")));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
