package io.github.rose.security.rbac.domain;

import io.github.rose.security.model.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色实体
 * <p>
 * 表示系统中的角色，角色是权限的集合。
 * 支持多租户隔离和角色继承。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色显示名称 */
    private String displayName;

    /** 角色描述 */
    private String description;

    /** 租户ID（支持多租户隔离，null表示全局角色） */
    private String tenantId;

    /** 基础权限级别（保持与现有Authority兼容） */
    private Authority authority;

    /** 是否启用 */
    private Boolean enabled;

    /** 是否为系统内置角色 */
    private Boolean systemRole;

    /** 角色级别（1-系统级，2-租户级，3-用户级） */
    private Integer level;

    /** 父角色ID（支持角色继承） */
    private Long parentId;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 角色权限集合 */
    private Set<String> permissions;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    /**
     * 检查角色是否为全局角色
     */
    public boolean isGlobalRole() {
        return tenantId == null;
    }

    /**
     * 检查角色是否为租户角色
     */
    public boolean isTenantRole() {
        return tenantId != null;
    }

    /**
     * 检查角色是否启用
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 检查是否为系统内置角色
     */
    public boolean isSystemRole() {
        return Boolean.TRUE.equals(systemRole);
    }

    /**
     * 获取角色的完整标识
     */
    public String getFullName() {
        if (tenantId != null) {
            return tenantId + ":" + name;
        }
        return name;
    }

    /**
     * 检查角色是否包含指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}