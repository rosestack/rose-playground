package io.github.rose.user.domain;

import io.github.rose.core.model.ExtraTenantModel;
import io.github.rose.core.model.HasCodeNameDescription;
import lombok.Data;

@Data
public class Group extends ExtraTenantModel<Long> implements HasCodeNameDescription {
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
