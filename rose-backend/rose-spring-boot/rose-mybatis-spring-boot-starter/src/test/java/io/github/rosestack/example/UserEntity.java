package io.github.rosestack.example;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rosestack.spring.boot.common.annotation.EncryptField;
import io.github.rosestack.spring.boot.common.encryption.enums.EncryptType;
import io.github.rosestack.spring.boot.common.encryption.enums.HashType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体示例
 * <p>
 * 演示如何使用安全加密 + 哈希查询的混合方案
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@TableName("user")
public class UserEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    // 手机号：安全加密存储 + 哈希查询
    @EncryptField(value = EncryptType.AES, searchable = true)
    private String phone;

    @TableField("phone_hash")
    private String phoneHash; // 自动维护，无需手动设置

    // 邮箱：安全加密存储 + 哈希查询（明确指定 HMAC-SHA256）
    @EncryptField(value = EncryptType.AES, searchable = true, hashType = HashType.HMAC_SHA256)
    private String email;

    @TableField("email_hash")
    private String emailHash; // 自动维护

    // 身份证：仅加密存储（不需要查询）
    @EncryptField(EncryptType.AES)
    private String idCard;

    // 银行卡号：仅加密存储（不需要查询）
    @EncryptField(EncryptType.AES)
    private String bankCard;

    // 自动填充字段
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    // 乐观锁版本字段
    @Version
    private Integer version;

    // 租户字段（自动填充）
    private String tenantId;

    // 数据权限字段
    private String userId;
    private String deptId;
}
