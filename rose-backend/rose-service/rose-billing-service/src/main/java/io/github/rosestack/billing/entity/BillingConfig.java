package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("billing_config")
public class BillingConfig extends BaseTenantEntity {
    private String id;
    private String tenantId;
    private String configKey;
    private String configValue;
    private String description;
}
