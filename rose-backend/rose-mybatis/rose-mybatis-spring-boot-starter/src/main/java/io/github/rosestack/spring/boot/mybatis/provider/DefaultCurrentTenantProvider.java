package io.github.rosestack.spring.boot.mybatis.provider;

import static io.github.rosestack.mybatis.MybatisConstants.HEADER_TENANT_ID;
import static io.github.rosestack.mybatis.MybatisConstants.MDC_TENANT_ID;

import io.github.rosestack.mybatis.provider.CurrentTenantProvider;
import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class DefaultCurrentTenantProvider implements CurrentTenantProvider {
    @Override
    public String getCurrentTenantId() {
        HttpServletRequest request = ServletUtils.getCurrentRequest();
        if (request == null) {
            return null;
        }

        String requestId = request.getHeader(HEADER_TENANT_ID);
        if (StringUtils.isNotBlank(requestId)) {
            return requestId;
        }

        requestId = MDC.get(MDC_TENANT_ID);
        if (StringUtils.isNotBlank(requestId)) {
            return requestId;
        }
        return "DEFAULT";
    }
}
