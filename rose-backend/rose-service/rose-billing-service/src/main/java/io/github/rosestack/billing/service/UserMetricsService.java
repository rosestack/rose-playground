package io.github.rosestack.billing.service;

/**
 * 用户指标服务接口
 * 提供与用户相关的计量数据查询，避免计费模块直接依赖 IAM 模块。
 */
public interface UserMetricsService {

    /**
     * 统计指定租户下的用户数量。
     *
     * @param tenantId 租户ID，不能为空
     * @return 用户数量，未知时返回0
     */
    int countTenantUsers(String tenantId);
}

