package io.github.rose.security.rbac.service.impl;

import io.github.rose.security.rbac.cache.PermissionCacheService;
import io.github.rose.security.rbac.domain.Permission;
import io.github.rose.security.rbac.domain.Role;
import io.github.rose.security.rbac.domain.RolePermission;
import io.github.rose.security.rbac.domain.UserRole;
import io.github.rose.security.rbac.repository.PermissionRepository;
import io.github.rose.security.rbac.repository.RolePermissionRepository;
import io.github.rose.security.rbac.repository.RoleRepository;
import io.github.rose.security.rbac.repository.UserRoleRepository;
import io.github.rose.security.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * <p>
 * 提供完整的RBAC权限管理功能，包括权限检查、角色管理、缓存优化等。
 * 支持多租户权限隔离和高性能权限检查。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionCacheService cacheService;

    // ==================== 权限检查 ====================

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission, String tenantId) {
        if (userId == null || permission == null || tenantId == null) {
            return false;
        }

        try {
            Set<String> userPermissions = getUserPermissions(userId, tenantId);
            boolean hasPermission = userPermissions.contains(permission);
            
            log.debug("权限检查结果: userId={}, permission={}, tenantId={}, result={}", 
                    userId, permission, tenantId, hasPermission);
            
            return hasPermission;
        } catch (Exception e) {
            log.error("权限检查异常: userId={}, permission={}, tenantId={}", userId, permission, tenantId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(Long userId, Set<String> permissions, String tenantId) {
        if (userId == null || CollectionUtils.isEmpty(permissions) || tenantId == null) {
            return false;
        }

        try {
            Set<String> userPermissions = getUserPermissions(userId, tenantId);
            boolean hasAny = permissions.stream().anyMatch(userPermissions::contains);
            
            log.debug("任一权限检查结果: userId={}, permissions={}, tenantId={}, result={}", 
                    userId, permissions, tenantId, hasAny);
            
            return hasAny;
        } catch (Exception e) {
            log.error("任一权限检查异常: userId={}, permissions={}, tenantId={}", userId, permissions, tenantId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAllPermissions(Long userId, Set<String> permissions, String tenantId) {
        if (userId == null || CollectionUtils.isEmpty(permissions) || tenantId == null) {
            return false;
        }

        try {
            Set<String> userPermissions = getUserPermissions(userId, tenantId);
            boolean hasAll = userPermissions.containsAll(permissions);
            
            log.debug("全部权限检查结果: userId={}, permissions={}, tenantId={}, result={}", 
                    userId, permissions, tenantId, hasAll);
            
            return hasAll;
        } catch (Exception e) {
            log.error("全部权限检查异常: userId={}, permissions={}, tenantId={}", userId, permissions, tenantId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Boolean> batchCheckPermissions(Long userId, Set<String> permissions, String tenantId) {
        if (userId == null || CollectionUtils.isEmpty(permissions) || tenantId == null) {
            return Collections.emptyMap();
        }

        try {
            Set<String> userPermissions = getUserPermissions(userId, tenantId);
            Map<String, Boolean> result = new HashMap<>();
            
            for (String permission : permissions) {
                result.put(permission, userPermissions.contains(permission));
            }
            
            log.debug("批量权限检查结果: userId={}, tenantId={}, result={}", userId, tenantId, result);
            
            return result;
        } catch (Exception e) {
            log.error("批量权限检查异常: userId={}, permissions={}, tenantId={}", userId, permissions, tenantId, e);
            return Collections.emptyMap();
        }
    }

    // ==================== 用户权限获取 ====================

    @Override
    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(Long userId, String tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        // 先从缓存获取
        Set<String> cachedPermissions = cacheService.getUserPermissions(userId, tenantId);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        try {
            // 从数据库加载用户权限
            Set<String> permissions = loadUserPermissionsFromDatabase(userId, tenantId);
            
            // 缓存权限
            cacheService.cacheUserPermissions(userId, tenantId, permissions);
            
            log.debug("加载用户权限: userId={}, tenantId={}, permissions={}", userId, tenantId, permissions.size());
            
            return permissions;
        } catch (Exception e) {
            log.error("获取用户权限异常: userId={}, tenantId={}", userId, tenantId, e);
            return Collections.emptySet();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> getUserRoles(Long userId, String tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        try {
            List<UserRole> userRoles = userRoleRepository.findByUserIdAndTenantId(userId, tenantId);
            if (CollectionUtils.isEmpty(userRoles)) {
                return Collections.emptySet();
            }

            Set<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toSet());

            List<Role> roles = roleRepository.findByIds(roleIds);
            return new HashSet<>(roles);
        } catch (Exception e) {
            log.error("获取用户角色异常: userId={}, tenantId={}", userId, tenantId, e);
            return Collections.emptySet();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UserRole> getUserRoleAssociations(Long userId, String tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        try {
            List<UserRole> userRoles = userRoleRepository.findByUserIdAndTenantId(userId, tenantId);
            return new HashSet<>(userRoles);
        } catch (Exception e) {
            log.error("获取用户角色关联异常: userId={}, tenantId={}", userId, tenantId, e);
            return Collections.emptySet();
        }
    }

    // ==================== 角色管理 ====================

    @Override
    public boolean assignRoleToUser(Long userId, Long roleId, String tenantId) {
        if (userId == null || roleId == null || tenantId == null) {
            return false;
        }

        try {
            // 检查关联是否已存在
            if (userRoleRepository.existsByUserIdAndRoleIdAndTenantId(userId, roleId, tenantId)) {
                log.warn("用户角色关联已存在: userId={}, roleId={}, tenantId={}", userId, roleId, tenantId);
                return true;
            }

            // 检查角色是否存在
            if (!roleRepository.findById(roleId).isPresent()) {
                log.warn("角色不存在: roleId={}", roleId);
                return false;
            }

            // 创建用户角色关联
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .tenantId(tenantId)
                    .createdTime(LocalDateTime.now())
                    .build();

            userRoleRepository.save(userRole);

            // 清除用户权限缓存
            cacheService.evictUserPermissionCache(userId, tenantId);

            log.info("分配角色成功: userId={}, roleId={}, tenantId={}", userId, roleId, tenantId);
            return true;
        } catch (Exception e) {
            log.error("分配角色异常: userId={}, roleId={}, tenantId={}", userId, roleId, tenantId, e);
            return false;
        }
    }

    @Override
    public boolean revokeRoleFromUser(Long userId, Long roleId, String tenantId) {
        if (userId == null || roleId == null || tenantId == null) {
            return false;
        }

        try {
            userRoleRepository.deleteByUserIdAndRoleIdAndTenantId(userId, roleId, tenantId);

            // 清除用户权限缓存
            cacheService.evictUserPermissionCache(userId, tenantId);

            log.info("撤销角色成功: userId={}, roleId={}, tenantId={}", userId, roleId, tenantId);
            return true;
        } catch (Exception e) {
            log.error("撤销角色异常: userId={}, roleId={}, tenantId={}", userId, roleId, tenantId, e);
            return false;
        }
    }

    @Override
    public Map<Long, Boolean> batchAssignRolesToUser(Long userId, Set<Long> roleIds, String tenantId) {
        if (userId == null || CollectionUtils.isEmpty(roleIds) || tenantId == null) {
            return Collections.emptyMap();
        }

        Map<Long, Boolean> result = new HashMap<>();
        
        for (Long roleId : roleIds) {
            result.put(roleId, assignRoleToUser(userId, roleId, tenantId));
        }

        return result;
    }

    // ==================== 权限管理 ====================

    @Override
    public boolean assignPermissionToRole(Long roleId, Long permissionId, String tenantId) {
        if (roleId == null || permissionId == null || tenantId == null) {
            return false;
        }

        try {
            // 检查关联是否已存在
            if (rolePermissionRepository.existsByRoleIdAndPermissionIdAndTenantId(roleId, permissionId, tenantId)) {
                log.warn("角色权限关联已存在: roleId={}, permissionId={}, tenantId={}", roleId, permissionId, tenantId);
                return true;
            }

            // 检查角色和权限是否存在
            if (!roleRepository.findById(roleId).isPresent()) {
                log.warn("角色不存在: roleId={}", roleId);
                return false;
            }

            if (!permissionRepository.findById(permissionId).isPresent()) {
                log.warn("权限不存在: permissionId={}", permissionId);
                return false;
            }

            // 创建角色权限关联
            RolePermission rolePermission = RolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permissionId)
                    .tenantId(tenantId)
                    .createdTime(LocalDateTime.now())
                    .build();

            rolePermissionRepository.save(rolePermission);

            // 清除角色权限缓存
            cacheService.evictRolePermissionCache(roleId, tenantId);

            log.info("分配权限成功: roleId={}, permissionId={}, tenantId={}", roleId, permissionId, tenantId);
            return true;
        } catch (Exception e) {
            log.error("分配权限异常: roleId={}, permissionId={}, tenantId={}", roleId, permissionId, tenantId, e);
            return false;
        }
    }

    @Override
    public boolean revokePermissionFromRole(Long roleId, Long permissionId, String tenantId) {
        if (roleId == null || permissionId == null || tenantId == null) {
            return false;
        }

        try {
            rolePermissionRepository.deleteByRoleIdAndPermissionIdAndTenantId(roleId, permissionId, tenantId);

            // 清除角色权限缓存
            cacheService.evictRolePermissionCache(roleId, tenantId);

            log.info("撤销权限成功: roleId={}, permissionId={}, tenantId={}", roleId, permissionId, tenantId);
            return true;
        } catch (Exception e) {
            log.error("撤销权限异常: roleId={}, permissionId={}, tenantId={}", roleId, permissionId, tenantId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getRolePermissions(Long roleId, String tenantId) {
        if (roleId == null || tenantId == null) {
            return Collections.emptySet();
        }

        try {
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdAndTenantId(roleId, tenantId);
            if (CollectionUtils.isEmpty(rolePermissions)) {
                return Collections.emptySet();
            }

            Set<Long> permissionIds = rolePermissions.stream()
                    .map(RolePermission::getPermissionId)
                    .collect(Collectors.toSet());

            List<Permission> permissions = permissionRepository.findByIds(permissionIds);
            return new HashSet<>(permissions);
        } catch (Exception e) {
            log.error("获取角色权限异常: roleId={}, tenantId={}", roleId, tenantId, e);
            return Collections.emptySet();
        }
    }

    // ==================== 缓存管理 ====================

    @Override
    public void clearUserPermissionCache(Long userId, String tenantId) {
        cacheService.evictUserPermissionCache(userId, tenantId);
        log.info("清除用户权限缓存: userId={}, tenantId={}", userId, tenantId);
    }

    @Override
    public void clearRolePermissionCache(Long roleId, String tenantId) {
        cacheService.evictRolePermissionCache(roleId, tenantId);
        log.info("清除角色权限缓存: roleId={}, tenantId={}", roleId, tenantId);
    }

    @Override
    public void clearAllPermissionCache() {
        cacheService.evictAllCache();
        log.info("清除所有权限缓存");
    }

    @Override
    public void preloadPermissionCache(String tenantId) {
        if (tenantId == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // 预加载租户的所有用户权限
                List<UserRole> userRoles = userRoleRepository.findByTenantId(tenantId);
                Set<Long> userIds = userRoles.stream()
                        .map(UserRole::getUserId)
                        .collect(Collectors.toSet());

                for (Long userId : userIds) {
                    getUserPermissions(userId, tenantId);
                }

                log.info("预加载权限缓存完成: tenantId={}, users={}", tenantId, userIds.size());
            } catch (Exception e) {
                log.error("预加载权限缓存异常: tenantId={}", tenantId, e);
            }
        });
    }

    // ==================== 权限查询 ====================

    @Override
    @Transactional(readOnly = true)
    public Permission findPermissionByName(String name, String tenantId) {
        if (name == null || tenantId == null) {
            return null;
        }

        try {
            return permissionRepository.findByNameAndTenantId(name, tenantId).orElse(null);
        } catch (Exception e) {
            log.error("查找权限异常: name={}, tenantId={}", name, tenantId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Role findRoleByName(String name, String tenantId) {
        if (name == null || tenantId == null) {
            return null;
        }

        try {
            return roleRepository.findByNameAndTenantId(name, tenantId).orElse(null);
        } catch (Exception e) {
            log.error("查找角色异常: name={}, tenantId={}", name, tenantId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getTenantPermissions(String tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }

        try {
            return permissionRepository.findEnabledByTenantId(tenantId);
        } catch (Exception e) {
            log.error("获取租户权限异常: tenantId={}", tenantId, e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getTenantRoles(String tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }

        try {
            return roleRepository.findEnabledByTenantId(tenantId);
        } catch (Exception e) {
            log.error("获取租户角色异常: tenantId={}", tenantId, e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 从数据库加载用户权限
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限集合
     */
    private Set<String> loadUserPermissionsFromDatabase(Long userId, String tenantId) {
        Set<String> permissions = new HashSet<>();

        try {
            // 获取用户角色
            List<UserRole> userRoles = userRoleRepository.findByUserIdAndTenantId(userId, tenantId);
            if (CollectionUtils.isEmpty(userRoles)) {
                return permissions;
            }

            // 获取角色权限
            Set<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toSet());

            for (Long roleId : roleIds) {
                Set<String> rolePermissions = loadRolePermissionsFromDatabase(roleId, tenantId);
                permissions.addAll(rolePermissions);
            }

        } catch (Exception e) {
            log.error("从数据库加载用户权限异常: userId={}, tenantId={}", userId, tenantId, e);
        }

        return permissions;
    }

    /**
     * 从数据库加载角色权限
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 权限集合
     */
    private Set<String> loadRolePermissionsFromDatabase(Long roleId, String tenantId) {
        // 先从缓存获取
        Set<String> cachedPermissions = cacheService.getRolePermissions(roleId, tenantId);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        Set<String> permissions = new HashSet<>();

        try {
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdAndTenantId(roleId, tenantId);
            if (!CollectionUtils.isEmpty(rolePermissions)) {
                Set<Long> permissionIds = rolePermissions.stream()
                        .map(RolePermission::getPermissionId)
                        .collect(Collectors.toSet());

                List<Permission> permissionList = permissionRepository.findByIds(permissionIds);
                permissions = permissionList.stream()
                        .filter(Permission::isEnabled)
                        .map(Permission::getName)
                        .collect(Collectors.toSet());
            }

            // 缓存角色权限
            cacheService.cacheRolePermissions(roleId, tenantId, permissions);

        } catch (Exception e) {
            log.error("从数据库加载角色权限异常: roleId={}, tenantId={}", roleId, tenantId, e);
        }

        return permissions;
    }
}