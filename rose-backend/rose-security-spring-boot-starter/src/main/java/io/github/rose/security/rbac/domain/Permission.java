package io.github.rose.security.rbac.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限实体
 * <p>
 * 表示系统中的一个具体权限，如 user:create、user:read 等。
 * 支持多租户隔离和层次化权限管理。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    /** 权限ID */
    private Long id;

    /** 权限名称（如：user:create, user:read） */
    private String name;

    /** 权限显示名称 */
    private String displayName;

    /** 权限描述 */
    private String description;

    /** 权限分类（如：用户管理、系统管理） */
    private String category;

    /** 所属模块（如：user, system, data） */
    private String module;

    /** 租户ID（支持多租户隔离，null表示全局权限） */
    private String tenantId;

    /** 是否启用 */
    private Boolean enabled;

    /** 权限级别（1-系统级，2-租户级，3-用户级） */
    private Integer level;

    /** 父权限ID（支持权限层次结构） */
    private Long parentId;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    /**
     * 检查权限是否为全局权限
     */
    public boolean isGlobalPermission() {
        return tenantId == null;
    }

    /**
     * 检查权限是否为租户权限
     */
    public boolean isTenantPermission() {
        return tenantId != null;
    }

    /**
     * 检查权限是否启用
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 获取权限的完整标识
     */
    public String getFullName() {
        if (tenantId != null) {
            return tenantId + ":" + name;
        }
        return name;
    }
}