package io.github.rosestack.mybatis.support.tenant;

import io.github.rosestack.core.util.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static io.github.rosestack.core.Constants.HeaderName.HEADER_TENANT_ID;
import static io.github.rosestack.core.Constants.MdcName.MDC_TENANT_ID;

public class TenantIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = ServletUtils.getCurrentTenantId();
        if (tenantId != null) {
            TenantContextHolder.setCurrentTenantId(tenantId);
            MDC.put(MDC_TENANT_ID, tenantId);
        }
        // 设置响应头
        response.setHeader(HEADER_TENANT_ID, tenantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TENANT_ID);
            TenantContextHolder.clear();
        }
    }
}