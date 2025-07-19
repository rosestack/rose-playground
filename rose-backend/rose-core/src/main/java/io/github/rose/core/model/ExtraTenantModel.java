package io.github.rose.core.model;

import com.fasterxml.jackson.databind.JsonNode;
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
public abstract class ExtraTenantModel<ID extends Serializable> extends TenantModel<ID> implements HasExtra {
    protected JsonNode extra;
}
