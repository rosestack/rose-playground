
package io.github.rose.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseAuditWithExtra<ID extends Serializable> extends BaseAudit<ID> implements HasExtra {

    @NotNull(message = "附加信息不能为空")
    protected transient JsonNode extra;

    @JsonIgnore
    @ToString.Exclude
    protected byte[] extraBytes;

    public BaseAuditWithExtra(ID id) {
        super(id);
    }

    public BaseAuditWithExtra(BaseAuditWithExtra baseData) {
        super(baseData);
        setExtra(baseData.getExtra());
    }

    @Override
    public JsonNode getExtra() {
        return getJson(() -> extra, () -> extraBytes);
    }

    public void setExtra(JsonNode extra) {
        setJson(extra, json -> this.extra = json, bytes -> this.extraBytes = bytes);
    }
}
