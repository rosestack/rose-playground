package io.github.rose.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseAddressTenant<ID extends Serializable> extends BaseAddress<ID> {
    @Getter
    protected String tenantId;

    public BaseAddressTenant(ID id) {
        super(id);
    }

    public BaseAddressTenant(BaseAddressTenant<ID> data) {
        super(data);
        this.tenantId = data.getTenantId();
    }
}
