package io.github.rose.security.rbac.repository;

import io.github.rose.security.rbac.domain.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色数据访问接口
 * <p>
 * 提供角色相关的数据访问操作，支持多租户隔离。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface RoleRepository {

    /**
     * 根据ID查找角色
     *
     * @param id 角色ID
     * @return 角色对象
     */
    Optional<Role> findById(Long id);

    /**
     * 根据名称和租户ID查找角色
     *
     * @param name     角色名称
     * @param tenantId 租户ID
     * @return 角色对象
     */
    Optional<Role> findByNameAndTenantId(String name, String tenantId);

    /**
     * 根据租户ID查找所有角色
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<Role> findByTenantId(String tenantId);

    /**
     * 根据角色ID集合查找角色
     *
     * @param ids 角色ID集合
     * @return 角色列表
     */
    List<Role> findByIds(Set<Long> ids);

    /**
     * 查找启用的角色
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<Role> findEnabledByTenantId(String tenantId);

    /**
     * 查找系统内置角色
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<Role> findSystemRolesByTenantId(String tenantId);

    /**
     * 保存角色
     *
     * @param role 角色对象
     * @return 保存后的角色对象
     */
    Role save(Role role);

    /**
     * 批量保存角色
     *
     * @param roles 角色列表
     * @return 保存后的角色列表
     */
    List<Role> saveAll(List<Role> roles);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteById(Long id);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID集合
     */
    void deleteByIds(Set<Long> ids);

    /**
     * 检查角色是否存在
     *
     * @param name     角色名称
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByNameAndTenantId(String name, String tenantId);

    /**
     * 统计租户角色数量
     *
     * @param tenantId 租户ID
     * @return 角色数量
     */
    long countByTenantId(String tenantId);
}