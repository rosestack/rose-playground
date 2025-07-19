package io.github.rose.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可审计的领域模型（ID + 审计字段）
 *
 * @param <ID> ID 类型
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AuditModel<ID extends Serializable> extends BaseModel<ID> implements Auditable<ID> {
    protected LocalDateTime createTime;
    protected LocalDateTime updateTime;
}
