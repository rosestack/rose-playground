
package io.github.rose.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseAuditWithExtra<ID extends Serializable> extends BaseAudit<ID> implements HasExtra {

    @NotNull(message = "附加信息不能为空")
    protected transient JsonNode extra;

    @JsonIgnore
    @ToString.Exclude
    protected byte[] extraBytes;

    @Override
    public JsonNode getExtra() {
        return getJson(() -> extra, () -> extraBytes);
    }

    public void setExtra(JsonNode extra) {
        setJson(extra, json -> this.extra = json, bytes -> this.extraBytes = bytes);
    }
}
