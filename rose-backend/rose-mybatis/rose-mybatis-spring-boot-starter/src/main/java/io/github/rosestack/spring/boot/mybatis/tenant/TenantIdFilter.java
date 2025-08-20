package io.github.rosestack.spring.boot.mybatis.tenant;

import static io.github.rosestack.mybatis.MybatisConstants.HEADER_TENANT_ID;
import static io.github.rosestack.mybatis.MybatisConstants.MDC_TENANT_ID;

import io.github.rosestack.mybatis.tenant.CurrentTenantProvider;
import io.github.rosestack.mybatis.tenant.TenantContextHolder;
import io.github.rosestack.spring.filter.AbstractRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.MDC;

/**
 * 租户 ID 过滤器
 *
 * <p>从请求头中提取租户 ID，设置到上下文和 MDC 中
 *
 * @author rosestack
 * @since 1.0.0
 */
public class TenantIdFilter extends AbstractRequestFilter {
    private final CurrentTenantProvider currentTenantProvider = new DefaultCurrentTenantProvider();

    public TenantIdFilter(List<String> excludePaths) {
        super(excludePaths.toArray(new String[0]));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = currentTenantProvider.getCurrentTenantId();
        if (tenantId != null) {
            TenantContextHolder.setCurrentTenantId(tenantId);
            MDC.put(MDC_TENANT_ID, tenantId);
        }
        response.setHeader(HEADER_TENANT_ID, tenantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TENANT_ID);
            TenantContextHolder.clear();
        }
    }
}
