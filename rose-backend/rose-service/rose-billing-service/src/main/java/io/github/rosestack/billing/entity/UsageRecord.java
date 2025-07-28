package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用量记录实体
 *
 * @author rose
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("usage_record")
public class UsageRecord extends BaseTenantEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 计量类型（API_CALLS, STORAGE, USERS, SMS, EMAIL等）
     */
    private String metricType;

    /**
     * 使用量数值
     */
    private BigDecimal quantity;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 关联的资源ID（用户ID、API密钥等）
     */
    private String resourceId;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 元数据（如API路径、文件类型等）
     */
    private String metadata;

    /**
     * 使用量描述
     */
    private String description;

    /**
     * 是否已计费
     */
    private Boolean billed;

    /**
     * 计费时间
     */
    private LocalDateTime billedAt;

    /**
     * 关联的账单ID
     */
    private String invoiceId;
}

