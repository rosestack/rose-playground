package io.github.rose.user.domain;

<<<<<<< HEAD
import io.github.rose.core.model.BaseTenantWithExtra;
import io.github.rose.core.model.HasCodeNameDescription;
import lombok.Data;

=======
import io.github.rose.common.model.BaseTenantWithExtra;
import io.github.rose.common.model.HasCodeNameDescription;
import lombok.Data;

import java.util.Map;

>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
/**
 * B2E场景，可以新增组织或者部门，部门需要挂在组织下面
 * B2B场景，新增用户池时，自动创建一个用户池名称的组织
 */
@Data
public class Department extends BaseTenantWithExtra<Long> implements HasCodeNameDescription {
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
