package io.github.rose.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.rose.core.domain.HasCodeNameDescription;
import io.github.rose.core.entity.BaseTenantEntity;
import lombok.Data;

@Data
public class Group extends BaseTenantEntity implements HasCodeNameDescription {
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

    private String userPoolId;

    /**
     * 状态
     */
    private Boolean status;
}
