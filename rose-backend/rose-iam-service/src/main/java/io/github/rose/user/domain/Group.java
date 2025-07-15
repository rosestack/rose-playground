package io.github.rose.user.domain;

import io.github.rose.common.model.BaseTenantWithExtra;
import io.github.rose.common.model.HasCodeNameDescription;
import lombok.Data;

import java.util.Map;

@Data
public class Group extends BaseTenantWithExtra<Long> implements HasCodeNameDescription {
    /**
     * 名称
     */
    private String name;

    /**
     * 识别码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    private String userPoolId;

    /**
     * 状态
     */
    private Boolean status;
}
