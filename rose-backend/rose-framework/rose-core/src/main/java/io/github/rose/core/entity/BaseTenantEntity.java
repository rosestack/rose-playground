package io.github.rose.core.entity;

import io.github.rose.core.domain.HasTenantId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户领域模型（审计 + 租户）
 *
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTenantEntity extends BaseEntity implements HasTenantId {
    protected String tenantId;
}
