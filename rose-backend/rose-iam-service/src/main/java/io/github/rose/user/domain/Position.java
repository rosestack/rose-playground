package io.github.rose.user.domain;

import io.github.rose.common.model.BaseTenantWithExtra;
import io.github.rose.common.model.HasCodeNameDescription;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Position extends BaseTenantWithExtra<Long> implements HasCodeNameDescription {
    /**
     * 分组名称
     */
    private String name;
    /**
     * 分组 code
     */
    private String code;

    /**
     * 分组描述
     */
    private String description;

    private String userPoolId;

    /**
     * 状态
     */
    private Boolean status;
}
