package io.github.rosestack.mybatis.provider;

import io.github.rosestack.core.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import static io.github.rosestack.mybatis.MybatisConstants.HEADER_TENANT_ID;
import static io.github.rosestack.mybatis.MybatisConstants.MDC_TENANT_ID;

public class DefaultCurrentTenantProvider implements CurrentTenantProvider {
    @Override
    public String getCurrentTenantId() {
        HttpServletRequest request = ServletUtils.getCurrentRequest();
        if (request == null) {
            return null;
        }

        String requestId = request.getHeader(HEADER_TENANT_ID);
        if (StringUtils.hasLength(requestId)) {
            return requestId;
        }

        requestId = MDC.get(MDC_TENANT_ID);
        if (StringUtils.hasLength(requestId)) {
            return requestId;
        }
        return "DEFAULT";
    }
}


