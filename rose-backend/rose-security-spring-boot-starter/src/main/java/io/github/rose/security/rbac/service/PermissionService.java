package io.github.rose.security.rbac.service;

import io.github.rose.security.rbac.domain.Permission;
import io.github.rose.security.rbac.domain.Role;
import io.github.rose.security.rbac.domain.UserRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限服务接口
 * <p>
 * 提供权限检查、权限管理、角色管理等核心功能。
 * 支持多租户权限隔离和高性能缓存。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface PermissionService {

    // ==================== 权限检查 ====================

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId     用户ID
     * @param permission 权限名称
     * @param tenantId   租户ID
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permission, String tenantId);

    /**
     * 检查用户是否拥有任一权限
     *
     * @param userId      用户ID
     * @param permissions 权限名称集合
     * @param tenantId    租户ID
     * @return 是否拥有任一权限
     */
    boolean hasAnyPermission(Long userId, Set<String> permissions, String tenantId);

    /**
     * 检查用户是否拥有所有权限
     *
     * @param userId      用户ID
     * @param permissions 权限名称集合
     * @param tenantId    租户ID
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(Long userId, Set<String> permissions, String tenantId);

    /**
     * 批量检查用户权限
     *
     * @param userId      用户ID
     * @param permissions 权限名称集合
     * @param tenantId    租户ID
     * @return 权限检查结果映射
     */
    Map<String, Boolean> batchCheckPermissions(Long userId, Set<String> permissions, String tenantId);

    // ==================== 用户权限获取 ====================

    /**
     * 获取用户的所有权限
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限名称集合
     */
    Set<String> getUserPermissions(Long userId, String tenantId);

    /**
     * 获取用户的所有角色
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 角色集合
     */
    Set<Role> getUserRoles(Long userId, String tenantId);

    /**
     * 获取用户的有效角色关联
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 用户角色关联集合
     */
    Set<UserRole> getUserRoleAssociations(Long userId, String tenantId);

    // ==================== 角色管理 ====================

    /**
     * 为用户分配角色
     *
     * @param userId   用户ID
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 是否分配成功
     */
    boolean assignRoleToUser(Long userId, Long roleId, String tenantId);

    /**
     * 撤销用户角色
     *
     * @param userId   用户ID
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 是否撤销成功
     */
    boolean revokeRoleFromUser(Long userId, Long roleId, String tenantId);

    /**
     * 批量为用户分配角色
     *
     * @param userId   用户ID
     * @param roleIds  角色ID集合
     * @param tenantId 租户ID
     * @return 分配结果映射
     */
    Map<Long, Boolean> batchAssignRolesToUser(Long userId, Set<Long> roleIds, String tenantId);

    // ==================== 权限管理 ====================

    /**
     * 为角色分配权限
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 是否分配成功
     */
    boolean assignPermissionToRole(Long roleId, Long permissionId, String tenantId);

    /**
     * 撤销角色权限
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 是否撤销成功
     */
    boolean revokePermissionFromRole(Long roleId, Long permissionId, String tenantId);

    /**
     * 获取角色的所有权限
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 权限集合
     */
    Set<Permission> getRolePermissions(Long roleId, String tenantId);

    // ==================== 缓存管理 ====================

    /**
     * 清除用户权限缓存
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    void clearUserPermissionCache(Long userId, String tenantId);

    /**
     * 清除角色权限缓存
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     */
    void clearRolePermissionCache(Long roleId, String tenantId);

    /**
     * 清除所有权限缓存
     */
    void clearAllPermissionCache();

    /**
     * 预加载权限缓存
     *
     * @param tenantId 租户ID
     */
    void preloadPermissionCache(String tenantId);

    // ==================== 权限查询 ====================

    /**
     * 根据名称查找权限
     *
     * @param name     权限名称
     * @param tenantId 租户ID
     * @return 权限对象
     */
    Permission findPermissionByName(String name, String tenantId);

    /**
     * 根据名称查找角色
     *
     * @param name     角色名称
     * @param tenantId 租户ID
     * @return 角色对象
     */
    Role findRoleByName(String name, String tenantId);

    /**
     * 获取租户的所有权限
     *
     * @param tenantId 租户ID
     * @return 权限列表
     */
    List<Permission> getTenantPermissions(String tenantId);

    /**
     * 获取租户的所有角色
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<Role> getTenantRoles(String tenantId);
}