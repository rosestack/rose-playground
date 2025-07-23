package io.github.rose.security.rbac.repository;

import io.github.rose.security.rbac.domain.RolePermission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色权限关联数据访问接口
 * <p>
 * 提供角色权限关联相关的数据访问操作，支持多租户隔离。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface RolePermissionRepository {

    /**
     * 根据ID查找角色权限关联
     *
     * @param id 关联ID
     * @return 角色权限关联对象
     */
    Optional<RolePermission> findById(Long id);

    /**
     * 根据角色ID和租户ID查找权限关联
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 角色权限关联列表
     */
    List<RolePermission> findByRoleIdAndTenantId(Long roleId, String tenantId);

    /**
     * 根据权限ID和租户ID查找角色关联
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 角色权限关联列表
     */
    List<RolePermission> findByPermissionIdAndTenantId(Long permissionId, String tenantId);

    /**
     * 根据角色ID、权限ID和租户ID查找关联
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 角色权限关联对象
     */
    Optional<RolePermission> findByRoleIdAndPermissionIdAndTenantId(Long roleId, Long permissionId, String tenantId);

    /**
     * 根据角色ID集合和租户ID查找权限关联
     *
     * @param roleIds  角色ID集合
     * @param tenantId 租户ID
     * @return 角色权限关联列表
     */
    List<RolePermission> findByRoleIdsAndTenantId(Set<Long> roleIds, String tenantId);

    /**
     * 根据权限ID集合和租户ID查找角色关联
     *
     * @param permissionIds 权限ID集合
     * @param tenantId      租户ID
     * @return 角色权限关联列表
     */
    List<RolePermission> findByPermissionIdsAndTenantId(Set<Long> permissionIds, String tenantId);

    /**
     * 根据租户ID查找所有角色权限关联
     *
     * @param tenantId 租户ID
     * @return 角色权限关联列表
     */
    List<RolePermission> findByTenantId(String tenantId);

    /**
     * 保存角色权限关联
     *
     * @param rolePermission 角色权限关联对象
     * @return 保存后的角色权限关联对象
     */
    RolePermission save(RolePermission rolePermission);

    /**
     * 批量保存角色权限关联
     *
     * @param rolePermissions 角色权限关联列表
     * @return 保存后的角色权限关联列表
     */
    List<RolePermission> saveAll(List<RolePermission> rolePermissions);

    /**
     * 删除角色权限关联
     *
     * @param id 关联ID
     */
    void deleteById(Long id);

    /**
     * 根据角色ID和权限ID删除关联
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     */
    void deleteByRoleIdAndPermissionIdAndTenantId(Long roleId, Long permissionId, String tenantId);

    /**
     * 根据角色ID删除所有权限关联
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     */
    void deleteByRoleIdAndTenantId(Long roleId, String tenantId);

    /**
     * 根据权限ID删除所有角色关联
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     */
    void deleteByPermissionIdAndTenantId(Long permissionId, String tenantId);

    /**
     * 批量删除角色权限关联
     *
     * @param ids 关联ID集合
     */
    void deleteByIds(Set<Long> ids);

    /**
     * 检查角色权限关联是否存在
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 是否存在
     */
    boolean existsByRoleIdAndPermissionIdAndTenantId(Long roleId, Long permissionId, String tenantId);

    /**
     * 统计角色的权限数量
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 权限数量
     */
    long countByRoleIdAndTenantId(Long roleId, String tenantId);

    /**
     * 统计权限的角色数量
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 角色数量
     */
    long countByPermissionIdAndTenantId(Long permissionId, String tenantId);
}