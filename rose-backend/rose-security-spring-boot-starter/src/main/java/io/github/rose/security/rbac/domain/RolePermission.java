package io.github.rose.security.rbac.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体
 * <p>
 * 表示角色与权限的关联关系，支持多租户隔离。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    /** 关联ID */
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 权限ID */
    private Long permissionId;

    /** 租户ID */
    private String tenantId;

    /** 是否启用 */
    private Boolean enabled;

    /** 权限类型（GRANT-授予，DENY-拒绝） */
    private PermissionType type;

    /** 生效时间 */
    private LocalDateTime effectiveTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 创建人 */
    private String createdBy;

    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        /** 授予权限 */
        GRANT,
        /** 拒绝权限 */
        DENY
    }

    /**
     * 检查关联是否启用
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 检查关联是否有效（在有效期内）
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查生效时间
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }
        
        // 检查过期时间
        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }
        
        return isEnabled();
    }

    /**
     * 检查是否为授予权限
     */
    public boolean isGrantPermission() {
        return PermissionType.GRANT.equals(type);
    }

    /**
     * 检查是否为拒绝权限
     */
    public boolean isDenyPermission() {
        return PermissionType.DENY.equals(type);
    }

    /**
     * 获取关联的完整标识
     */
    public String getFullIdentifier() {
        return String.format("%s:%d:%d:%s", tenantId, roleId, permissionId, type);
    }
}