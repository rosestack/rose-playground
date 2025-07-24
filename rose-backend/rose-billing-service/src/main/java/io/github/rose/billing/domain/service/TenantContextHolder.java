package io.github.rose.billing.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 租户上下文持有者
 *
 * @author rose
 */
@Service
@Slf4j
public class TenantContextHolder {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    public void setCurrentTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
        log.debug("设置当前租户ID: {}", tenantId);
    }

    public String getCurrentTenantId() {
        return TENANT_ID.get();
    }

    public void clear() {
        TENANT_ID.remove();
    }
}
