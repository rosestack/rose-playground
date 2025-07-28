package io.github.rosestack.tenant;

import io.github.rosestack.core.entity.BaseEntity;
import io.github.rosestack.core.model.HasCodeNameDescription;
import lombok.Data;

@Data
public class Tenant extends BaseEntity implements HasCodeNameDescription {
    private String id;

    private String name;
    private String code;
    private String description;
    private String logo;

    private String sourceAppId;

    private String source;

    /**
     * 企业邮箱域名
     */
    private String emailDomain;

    /**
     * 租户过期时间
     */
    private String expireTime;

    /**
     * 租户 MAU 上限
     */
    private Integer mauAmount;
    /**
     * 租户成员上限
     */
    private Integer memberAmount;

    /**
     * 租户管理员上限
     */
    private Integer adminAmount;

    private String userPoolId;

    /**
     * 状态
     */
    private Boolean status;
}
