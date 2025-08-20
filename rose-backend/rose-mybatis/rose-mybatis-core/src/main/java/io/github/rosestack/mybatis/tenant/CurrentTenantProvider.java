package io.github.rosestack.mybatis.tenant;

/**
 * 当前租户ID提供器抽象。
 */
public interface CurrentTenantProvider {
    String getCurrentTenantId();
}
