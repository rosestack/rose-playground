package io.github.rose.tenant;

<<<<<<< HEAD
import io.github.rose.core.model.BaseAuditWithExtra;
import io.github.rose.core.model.HasCodeNameDescription;
=======
import io.github.rose.common.model.BaseAuditWithExtra;
import io.github.rose.common.model.HasCodeNameDescription;
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
import lombok.Data;

@Data
public class Tenant extends BaseAuditWithExtra<String> implements HasCodeNameDescription {
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
