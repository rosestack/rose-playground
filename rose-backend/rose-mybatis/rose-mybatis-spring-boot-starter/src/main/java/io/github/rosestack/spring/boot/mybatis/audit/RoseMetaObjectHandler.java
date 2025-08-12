package io.github.rosestack.spring.boot.mybatis.audit;

import io.github.rosestack.mybatis.audit.AbstractMetaObjectHandler;
import io.github.rosestack.mybatis.tenant.TenantContextHolder;
import io.github.rosestack.spring.boot.mybatis.config.RoseMybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Starter 层实现，委托核心抽象进行字段填充。 */
@Slf4j
@RequiredArgsConstructor
public class RoseMetaObjectHandler extends AbstractMetaObjectHandler {

    private final RoseMybatisProperties properties;

    @Override
    protected String getCreateTimeColumn() {
        return properties.getFieldFill().getCreateTimeColumn();
    }

    @Override
    protected String getUpdateTimeColumn() {
        return properties.getFieldFill().getUpdateTimeColumn();
    }

    @Override
    protected String getCreatedByColumn() {
        return properties.getFieldFill().getCreatedByColumn();
    }

    @Override
    protected String getTenantColumn() {
        return properties.getTenant().getColumn();
    }

    @Override
    protected String getCurrentTenantId() {
        return TenantContextHolder.getCurrentTenantId();
    }

    @Override
    protected String getCurrentUsername() {
        return properties.getFieldFill().getDefaultUser();
    }
}
