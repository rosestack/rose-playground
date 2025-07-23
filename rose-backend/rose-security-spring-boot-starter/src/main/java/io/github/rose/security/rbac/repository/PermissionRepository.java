package io.github.rose.security.rbac.repository;

import io.github.rose.security.rbac.domain.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限数据访问接口
 * <p>
 * 提供权限相关的数据访问操作，支持多租户隔离。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface PermissionRepository {

    /**
     * 根据ID查找权限
     *
     * @param id 权限ID
     * @return 权限对象
     */
    Optional<Permission> findById(Long id);

    /**
     * 根据名称和租户ID查找权限
     *
     * @param name     权限名称
     * @param tenantId 租户ID
     * @return 权限对象
     */
    Optional<Permission> findByNameAndTenantId(String name, String tenantId);

    /**
     * 根据租户ID查找所有权限
     *
     * @param tenantId 租户ID
     * @return 权限列表
     */
    List<Permission> findByTenantId(String tenantId);

    /**
     * 根据模块和租户ID查找权限
     *
     * @param module   模块名称
     * @param tenantId 租户ID
     * @return 权限列表
     */
    List<Permission> findByModuleAndTenantId(String module, String tenantId);

    /**
     * 根据分类和租户ID查找权限
     *
     * @param category 权限分类
     * @param tenantId 租户ID
     * @return 权限列表
     */
    List<Permission> findByCategoryAndTenantId(String category, String tenantId);

    /**
     * 根据权限ID集合查找权限
     *
     * @param ids 权限ID集合
     * @return 权限列表
     */
    List<Permission> findByIds(Set<Long> ids);

    /**
     * 根据权限名称集合和租户ID查找权限
     *
     * @param names    权限名称集合
     * @param tenantId 租户ID
     * @return 权限列表
     */
    List<Permission> findByNamesAndTenantId(Set<String> names, String tenantId);

    /**
     * 查找启用的权限
     *
     * @param tenantId 租户ID
     * @return 权限列表
     */
    List<Permission> findEnabledByTenantId(String tenantId);

    /**
     * 保存权限
     *
     * @param permission 权限对象
     * @return 保存后的权限对象
     */
    Permission save(Permission permission);

    /**
     * 批量保存权限
     *
     * @param permissions 权限列表
     * @return 保存后的权限列表
     */
    List<Permission> saveAll(List<Permission> permissions);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void deleteById(Long id);

    /**
     * 批量删除权限
     *
     * @param ids 权限ID集合
     */
    void deleteByIds(Set<Long> ids);

    /**
     * 检查权限是否存在
     *
     * @param name     权限名称
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByNameAndTenantId(String name, String tenantId);

    /**
     * 统计租户权限数量
     *
     * @param tenantId 租户ID
     * @return 权限数量
     */
    long countByTenantId(String tenantId);
}