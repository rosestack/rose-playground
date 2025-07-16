package io.github.rose.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTenant<ID extends Serializable> extends BaseAudit<ID> {
    protected String tenantId;

//    public BaseTenant(ID id) {
//        super(id);
//    }
//
//    public BaseTenant(BaseTenant<ID> data) {
//        super(data);
//        this.tenantId = data.getTenantId();
//    }
}
