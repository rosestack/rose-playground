package io.github.rose.security.rbac.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * <p>
 * 表示用户与角色的关联关系，支持多租户隔离。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    /** 关联ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

    /** 租户ID */
    private String tenantId;

    /** 是否启用 */
    private Boolean enabled;

    /** 生效时间 */
    private LocalDateTime effectiveTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 创建人 */
    private String createdBy;

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
     * 获取关联的完整标识
     */
    public String getFullIdentifier() {
        return String.format("%s:%d:%d", tenantId, userId, roleId);
    }
}