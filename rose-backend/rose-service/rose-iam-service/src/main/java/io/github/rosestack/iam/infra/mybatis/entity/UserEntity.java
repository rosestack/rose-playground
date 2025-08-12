package io.github.rosestack.iam.infra.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 *
 * <p>继承自 BaseTenantEntity，包含多租户支持和审计字段。 使用 MyBatis Plus 注解进行数据库映射配置，包含乐观锁实现。
 *
 * @author 开发者姓名
 * @since 0.0.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserEntity extends BaseTenantEntity {

    /** 用户ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户名 */
    @TableField("username")
    private String username;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 密码（加密存储） */
    @TableField("password")
    private String password;

    /** 用户状态：ACTIVE-激活，INACTIVE-未激活，LOCKED-锁定 */
    @TableField("status")
    @EnumValue
    private UserStatus status;

    /** 最后登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /** 用户状态枚举 */
    public enum UserStatus {
        ACTIVE, // 激活
        INACTIVE, // 未激活
        LOCKED // 锁定
    }
}
