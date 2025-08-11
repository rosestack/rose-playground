package io.github.rosestack.spring.boot.mybatis.support.tenant;

import io.github.rosestack.core.spring.AbstractBaseFilter;
import io.github.rosestack.core.util.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;

import static io.github.rosestack.core.Constants.HeaderName.HEADER_TENANT_ID;
import static io.github.rosestack.core.Constants.MdcName.MDC_TENANT_ID;

/**
 * 租户 ID 过滤器
 * <p>
 * 从请求头中提取租户 ID，设置到上下文和 MDC 中
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public class TenantIdFilter extends AbstractBaseFilter {

    public TenantIdFilter(List<String> excludePaths) {
        super(excludePaths.toArray(new String[0]));
    }

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