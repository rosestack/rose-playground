package com.company.usermodulith.user.internal;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rosestack.core.entity.BaseEntity;
import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.enums.SensitiveType;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @SensitiveField(value = SensitiveType.EMAIL)
    private String email;

    @TableField("phone")
    @SensitiveField(value = SensitiveType.PHONE)
    private String phone;

    @TableField("password")
    @SensitiveField(value = SensitiveType.SECRET)
    private String password;

    @TableField("status")
    @EnumValue
    private UserStatus status;
}