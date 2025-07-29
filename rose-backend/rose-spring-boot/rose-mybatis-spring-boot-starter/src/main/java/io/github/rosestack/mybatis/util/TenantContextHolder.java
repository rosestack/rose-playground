package io.github.rosestack.mybatis.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文持有者
 * <p>
 * 使用 InheritableThreadLocal 存储当前线程的租户信息，确保多租户数据隔离。
 * 支持父子线程间的上下文传递，适用于异步处理场景。
 * 主要管理租户ID，用户ID的管理应该由权限模块负责。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class TenantContextHolder {

    /**
     * 租户ID的线程本地存储，支持父子线程传递
     */
    private static final InheritableThreadLocal<String> TENANT_ID = new InheritableThreadLocal<>();

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setCurrentTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
        log.debug("设置当前租户ID: {}", tenantId);
    }

    /**
     * 获取当前租户ID
     *
     * @return 当前租户ID，如果未设置则返回 null
     */
    public static String getCurrentTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 检查是否存在租户上下文
     *
     * @return 如果存在租户ID则返回 true，否则返回 false
     */
    public static boolean hasTenantContext() {
        return TENANT_ID.get() != null;
    }

    /**
     * 清除当前线程的租户上下文
     */
    public static void clear() {
        TENANT_ID.remove();
        log.debug("清除租户上下文");
    }

    /**
     * 获取当前上下文信息的字符串表示
     *
     * @return 上下文信息字符串
     */
    public static String getContextInfo() {
        return String.format("TenantContext[tenantId=%s]", getCurrentTenantId());
    }

    /**
     * 在指定的租户上下文中执行操作
     *
     * @param tenantId 租户ID
     * @param runnable 要执行的操作
     */
    public static void runWithTenant(String tenantId, Runnable runnable) {
        String originalTenantId = getCurrentTenantId();
        try {
            setCurrentTenantId(tenantId);
            runnable.run();
        } finally {
            if (originalTenantId != null) {
                setCurrentTenantId(originalTenantId);
            } else {
                clear();
            }
        }
    }

    /**
     * 在指定的租户上下文中执行操作并返回结果
     *
     * @param tenantId 租户ID
     * @param supplier 要执行的操作
     * @param <T>      返回值类型
     * @return 操作结果
     */
    public static <T> T runWithTenant(String tenantId, java.util.function.Supplier<T> supplier) {
        String originalTenantId = getCurrentTenantId();
        try {
            setCurrentTenantId(tenantId);
            return supplier.get();
        } finally {
            if (originalTenantId != null) {
                setCurrentTenantId(originalTenantId);
            } else {
                clear();
            }
        }
    }
}
