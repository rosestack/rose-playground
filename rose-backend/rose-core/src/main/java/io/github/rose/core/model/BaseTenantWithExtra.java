package io.github.rose.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTenantWithExtra<ID extends Serializable> extends BaseAudit<ID> implements HasExtra {
    @Getter
    protected String tenantId;

    @NotNull(message = "附加信息不能为空")
    protected transient JsonNode extra;

    @JsonIgnore
    @ToString.Exclude
    protected byte[] extraBytes;

//    public BaseTenantWithExtra(ID id) {
//        super(id);
//    }
//
//    public BaseTenantWithExtra(BaseTenantWithExtra<ID> data) {
//        super(data);
//        this.tenantId = data.getTenantId();
//        setExtra(data.getExtra());
//    }

    @Override
    public JsonNode getExtra() {
        return getJson(() -> extra, () -> extraBytes);
    }

    public void setExtra(JsonNode extra) {
        setJson(extra, json -> this.extra = json, bytes -> this.extraBytes = bytes);
    }
}
