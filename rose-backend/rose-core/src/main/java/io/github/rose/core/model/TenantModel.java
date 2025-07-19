package io.github.rose.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 租户领域模型（审计 + 租户）
 *
 * @param <ID> ID 类型
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantModel<ID extends Serializable> extends AuditModel<ID> implements HasTenantId {
    protected String tenantId;
}
