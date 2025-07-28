package io.github.rosestack.billing.service;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文持有者
 *
 * @author rose
 */
@Slf4j
public class TenantContextHolder {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    public static void setCurrentTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
        log.debug("设置当前租户ID: {}", tenantId);
    }

    public static String getCurrentTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
