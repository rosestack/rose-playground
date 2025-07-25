package com.company.usermodulith.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.rosestack.core.model.HasCodeNameDescription;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;

/**
 * B2E场景，可以新增组织或者部门，部门需要挂在组织下面
 * B2B场景，新增用户池时，自动创建一个用户池名称的组织
 */
@Data
public class Department extends BaseTenantEntity implements HasCodeNameDescription {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
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

    //org、depart
    private String type;

    /**
     * 父id
     */
    private String parentId;

    /**
     * 是否包含子部门
     */
    private Boolean hasChildren;

    /**
     * 是否是虚拟
     */
    private Boolean isVirtualNode;

    /**
     * 负责人 ID
     */
    private String leaderUserIds;

    private String userPoolId;

    /**
     * 状态
     */
    private Boolean status;
}
