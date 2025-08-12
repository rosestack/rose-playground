package io.github.rosestack.mybatis.provider;

/**
 * 当前租户ID提供器抽象。
 */
public interface CurrentTenantProvider {
    String getCurrentTenantId();
}


