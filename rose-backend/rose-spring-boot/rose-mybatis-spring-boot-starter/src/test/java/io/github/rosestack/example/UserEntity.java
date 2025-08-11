package io.github.rosestack.example;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rosestack.core.entity.BaseTenantEntity;
import io.github.rosestack.spring.boot.common.encryption.annotation.EncryptField;
import io.github.rosestack.spring.boot.common.encryption.enums.EncryptType;
import io.github.rosestack.spring.boot.common.encryption.enums.HashType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体示例
 * <p>
 * 演示如何使用安全加密 + 哈希查询的混合方案
 * 继承自 BaseTenantEntity，获取通用字段
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserEntity extends BaseTenantEntity {

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

    // 数据权限字段
    private String userId;
    private String deptId;
}
