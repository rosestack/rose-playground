package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.annotation.*;
import com.company.usermodulith.shared.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 放在 internal 包中隐藏实现细节
 * 继承 BaseEntity 获取通用字段
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("password")
    private String password;

    @TableField("status")
    private String status;

    // 重新声明继承的字段，确保 MyBatis Plus 正确映射
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;

    @Version
    @TableField(value = "version")
    private Integer version;
}