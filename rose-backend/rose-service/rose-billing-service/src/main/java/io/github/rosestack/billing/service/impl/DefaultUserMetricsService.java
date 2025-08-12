package io.github.rosestack.billing.service.impl;

import io.github.rosestack.billing.service.UserMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/** 默认用户指标服务实现（占位实现）。 若项目提供了真实实现（例如依赖 IAM 模块），可通过定义同名 Bean 覆盖本实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(UserMetricsService.class)
public class DefaultUserMetricsService implements UserMetricsService {

    @Override
    public int countTenantUsers(String tenantId) {
        // TODO: 替换为真实的 IAM 统计查询，例如调用 UserMapper 或远程服务
        log.warn("UserMetricsService 默认实现被使用，返回占位值 1。请提供真实实现以获得准确用户数。");
        return 1;
    }
}
