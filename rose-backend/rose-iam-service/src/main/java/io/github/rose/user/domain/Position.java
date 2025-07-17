package io.github.rose.user.domain;

import io.github.rose.core.model.BaseTenantWithExtra;
import io.github.rose.core.model.HasCodeNameDescription;
import lombok.Data;

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
