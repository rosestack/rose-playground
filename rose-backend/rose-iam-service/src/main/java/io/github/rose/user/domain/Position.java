package io.github.rose.user.domain;

<<<<<<< HEAD
import io.github.rose.core.model.BaseTenantWithExtra;
import io.github.rose.core.model.HasCodeNameDescription;
import lombok.Data;

=======
import io.github.rose.common.model.BaseTenantWithExtra;
import io.github.rose.common.model.HasCodeNameDescription;
import lombok.Data;

import java.time.LocalDateTime;

>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
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
