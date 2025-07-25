package com.company.usermodulith.user.infra.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 继承自 BaseTenantEntity，包含多租户支持和审计字段。
 * 使用 MyBatis Plus 注解进行数据库映射配置，包含乐观锁实现。
 * </p>
 *
 * @author 开发者姓名
 * @since 0.0.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserEntity extends BaseTenantEntity {

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 用户状态：ACTIVE-激活，INACTIVE-未激活，LOCKED-锁定
     */
    @TableField("status")
    @EnumValue
    private UserStatus status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    // 继承的字段会自动映射，但需要在子类中重新定义以添加 MyBatis Plus 注解
    
    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 乐观锁版本号
     * <p>
     * 用于乐观锁控制，防止并发更新冲突。
     * 每次更新时版本号会自动递增。
     * </p>
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE,     // 激活
        INACTIVE,   // 未激活
        LOCKED      // 锁定
    }
} 