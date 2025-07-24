package io.github.rose.core.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础领域模型（只包含ID）
 *
 * @param <ID> ID 类型
 * @author rose
 */
@Data
public abstract class BaseModel<ID extends Serializable> implements HasId<ID> {
    protected ID id;
}
