package io.github.rose.role;

import lombok.Data;

@Data
public class Permission {
    private String id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private String path;
    private Integer type;
    private Integer sort;

    private String userPoolId;

    private String appId;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private String createdAt;
    /**
     * 修改时间
     */
    private String updatedAt;
}
