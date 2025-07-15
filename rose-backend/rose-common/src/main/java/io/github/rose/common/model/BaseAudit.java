package io.github.rose.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@ToString(callSuper = true)
public abstract class BaseAudit<ID extends Serializable> implements HasId<ID> {
    protected static final String NORM_DATETIME_PATTERN = "YYYY-MM-dd HH:mm:ss";

    @Getter
    protected ID id;

    @Getter
    @JsonFormat(pattern = NORM_DATETIME_PATTERN)
    protected LocalDateTime createTime;

    @Getter
    @JsonFormat(pattern = NORM_DATETIME_PATTERN)
    protected LocalDateTime updateTime;

    public BaseAudit(ID id) {
        this.id = id;
    }

    public BaseAudit(BaseAudit<ID> data) {
        this.id = data.getId();
        this.createTime = data.getCreateTime();
        this.updateTime = data.getUpdateTime();
    }
}
