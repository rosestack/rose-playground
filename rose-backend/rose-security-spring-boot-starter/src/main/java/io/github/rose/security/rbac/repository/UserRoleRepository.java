package io.github.rose.security.rbac.repository;

import io.github.rose.security.rbac.domain.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户角色关联数据访问接口
 * <p>
 * 提供用户角色关联相关的数据访问操作，支持多租户隔离。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface UserRoleRepository {

    /**
     * 根据ID查找用户角色关联
     *
     * @param id 关联ID
     * @return 用户角色关联对象
     */
    Optional<UserRole> findById(Long id);

    /**
     * 根据用户ID和租户ID查找角色关联
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserIdAndTenantId(Long userId, String tenantId);

    /**
     * 根据角色ID和租户ID查找用户关联
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleIdAndTenantId(Long roleId, String tenantId);

    /**
     * 根据用户ID、角色ID和租户ID查找关联
     *
     * @param userId   用户ID
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 用户角色关联对象
     */
    Optional<UserRole> findByUserIdAndRoleIdAndTenantId(Long userId, Long roleId, String tenantId);

    /**
     * 根据用户ID集合和租户ID查找角色关联
     *
     * @param userIds  用户ID集合
     * @param tenantId 租户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserIdsAndTenantId(Set<Long> userIds, String tenantId);

    /**
     * 根据角色ID集合和租户ID查找用户关联
     *
     * @param roleIds  角色ID集合
     * @param tenantId 租户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleIdsAndTenantId(Set<Long> roleIds, String tenantId);

    /**
     * 根据租户ID查找所有用户角色关联
     *
     * @param tenantId 租户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByTenantId(String tenantId);

    /**
     * 保存用户角色关联
     *
     * @param userRole 用户角色关联对象
     * @return 保存后的用户角色关联对象
     */
    UserRole save(UserRole userRole);

    /**
     * 批量保存用户角色关联
     *
     * @param userRoles 用户角色关联列表
     * @return 保存后的用户角色关联列表
     */
    List<UserRole> saveAll(List<UserRole> userRoles);

    /**
     * 删除用户角色关联
     *
     * @param id 关联ID
     */
    void deleteById(Long id);

    /**
     * 根据用户ID和角色ID删除关联
     *
     * @param userId   用户ID
     * @param roleId   角色ID
     * @param tenantId 租户ID
     */
    void deleteByUserIdAndRoleIdAndTenantId(Long userId, Long roleId, String tenantId);

    /**
     * 根据用户ID删除所有角色关联
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    void deleteByUserIdAndTenantId(Long userId, String tenantId);

    /**
     * 根据角色ID删除所有用户关联
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     */
    void deleteByRoleIdAndTenantId(Long roleId, String tenantId);

    /**
     * 批量删除用户角色关联
     *
     * @param ids 关联ID集合
     */
    void deleteByIds(Set<Long> ids);

    /**
     * 检查用户角色关联是否存在
     *
     * @param userId   用户ID
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByUserIdAndRoleIdAndTenantId(Long userId, Long roleId, String tenantId);

    /**
     * 统计用户的角色数量
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 角色数量
     */
    long countByUserIdAndTenantId(Long userId, String tenantId);

    /**
     * 统计角色的用户数量
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 用户数量
     */
    long countByRoleIdAndTenantId(Long roleId, String tenantId);
}