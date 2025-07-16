package io.github.rose.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
public abstract class BaseAudit<ID extends Serializable> implements HasId<ID> {
    protected static final String NORM_DATETIME_PATTERN = "YYYY-MM-dd HH:mm:ss";

    protected ID id;

    @JsonFormat(pattern = NORM_DATETIME_PATTERN)
    protected LocalDateTime createTime;

    @JsonFormat(pattern = NORM_DATETIME_PATTERN)
    protected LocalDateTime updateTime;

//    public BaseAudit(ID id) {
//        this.id = id;
//    }
//
//    public BaseAudit(BaseAudit<ID> data) {
//        this.id = data.getId();
//        this.createTime = data.getCreateTime();
//        this.updateTime = data.getUpdateTime();
//    }
}
