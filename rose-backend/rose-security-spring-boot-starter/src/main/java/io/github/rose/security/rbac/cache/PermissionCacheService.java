package io.github.rose.security.rbac.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限缓存服务
 * <p>
 * 提供多级缓存支持，包括本地缓存（Caffeine）和分布式缓存（Redis）。
 * 支持权限预加载、批量操作和智能失效策略。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // L1缓存：本地高速缓存
    private final Cache<String, Set<String>> userPermissionCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build();

    private final Cache<String, Set<String>> rolePermissionCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofHours(1))
            .recordStats()
            .build();

    private final Cache<String, Set<String>> userRoleCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build();

    // 缓存键前缀
    private static final String USER_PERMISSION_PREFIX = "user:permissions:";
    private static final String ROLE_PERMISSION_PREFIX = "role:permissions:";
    private static final String USER_ROLE_PREFIX = "user:roles:";
    private static final String TENANT_PERMISSION_PREFIX = "tenant:permissions:";

    // Redis缓存过期时间
    private static final Duration REDIS_EXPIRE_TIME = Duration.ofHours(2);

    /**
     * 获取用户权限（多级缓存）
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限集合
     */
    public Set<String> getUserPermissions(Long userId, String tenantId) {
        String cacheKey = buildUserPermissionKey(userId, tenantId);
        
        // 先从L1缓存获取
        Set<String> permissions = userPermissionCache.getIfPresent(cacheKey);
        if (permissions != null) {
            log.debug("从L1缓存获取用户权限: userId={}, tenantId={}, permissions={}", userId, tenantId, permissions.size());
            return permissions;
        }

        // 从L2缓存（Redis）获取
        try {
            @SuppressWarnings("unchecked")
            Set<String> redisPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (redisPermissions != null) {
                // 放入L1缓存
                userPermissionCache.put(cacheKey, redisPermissions);
                log.debug("从L2缓存获取用户权限: userId={}, tenantId={}, permissions={}", userId, tenantId, redisPermissions.size());
                return redisPermissions;
            }
        } catch (Exception e) {
            log.warn("从Redis获取用户权限失败: userId={}, tenantId={}", userId, tenantId, e);
        }

        return null; // 缓存未命中，需要从数据库加载
    }

    /**
     * 缓存用户权限
     *
     * @param userId      用户ID
     * @param tenantId    租户ID
     * @param permissions 权限集合
     */
    public void cacheUserPermissions(Long userId, String tenantId, Set<String> permissions) {
        String cacheKey = buildUserPermissionKey(userId, tenantId);
        
        // 放入L1缓存
        userPermissionCache.put(cacheKey, permissions);
        
        // 放入L2缓存（Redis）
        try {
            redisTemplate.opsForValue().set(cacheKey, permissions, REDIS_EXPIRE_TIME);
            log.debug("缓存用户权限: userId={}, tenantId={}, permissions={}", userId, tenantId, permissions.size());
        } catch (Exception e) {
            log.warn("缓存用户权限到Redis失败: userId={}, tenantId={}", userId, tenantId, e);
        }
    }

    /**
     * 获取角色权限
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 权限集合
     */
    public Set<String> getRolePermissions(Long roleId, String tenantId) {
        String cacheKey = buildRolePermissionKey(roleId, tenantId);
        
        // 先从L1缓存获取
        Set<String> permissions = rolePermissionCache.getIfPresent(cacheKey);
        if (permissions != null) {
            return permissions;
        }

        // 从L2缓存获取
        try {
            @SuppressWarnings("unchecked")
            Set<String> redisPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (redisPermissions != null) {
                rolePermissionCache.put(cacheKey, redisPermissions);
                return redisPermissions;
            }
        } catch (Exception e) {
            log.warn("从Redis获取角色权限失败: roleId={}, tenantId={}", roleId, tenantId, e);
        }

        return null;
    }

    /**
     * 缓存角色权限
     *
     * @param roleId      角色ID
     * @param tenantId    租户ID
     * @param permissions 权限集合
     */
    public void cacheRolePermissions(Long roleId, String tenantId, Set<String> permissions) {
        String cacheKey = buildRolePermissionKey(roleId, tenantId);
        
        rolePermissionCache.put(cacheKey, permissions);
        
        try {
            redisTemplate.opsForValue().set(cacheKey, permissions, REDIS_EXPIRE_TIME);
        } catch (Exception e) {
            log.warn("缓存角色权限到Redis失败: roleId={}, tenantId={}", roleId, tenantId, e);
        }
    }

    /**
     * 获取用户角色
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 角色集合
     */
    public Set<String> getUserRoles(Long userId, String tenantId) {
        String cacheKey = buildUserRoleKey(userId, tenantId);
        
        Set<String> roles = userRoleCache.getIfPresent(cacheKey);
        if (roles != null) {
            return roles;
        }

        try {
            @SuppressWarnings("unchecked")
            Set<String> redisRoles = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (redisRoles != null) {
                userRoleCache.put(cacheKey, redisRoles);
                return redisRoles;
            }
        } catch (Exception e) {
            log.warn("从Redis获取用户角色失败: userId={}, tenantId={}", userId, tenantId, e);
        }

        return null;
    }

    /**
     * 缓存用户角色
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param roles    角色集合
     */
    public void cacheUserRoles(Long userId, String tenantId, Set<String> roles) {
        String cacheKey = buildUserRoleKey(userId, tenantId);
        
        userRoleCache.put(cacheKey, roles);
        
        try {
            redisTemplate.opsForValue().set(cacheKey, roles, REDIS_EXPIRE_TIME);
        } catch (Exception e) {
            log.warn("缓存用户角色到Redis失败: userId={}, tenantId={}", userId, tenantId, e);
        }
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    public void evictUserPermissionCache(Long userId, String tenantId) {
        String permissionKey = buildUserPermissionKey(userId, tenantId);
        String roleKey = buildUserRoleKey(userId, tenantId);
        
        // 清除L1缓存
        userPermissionCache.invalidate(permissionKey);
        userRoleCache.invalidate(roleKey);
        
        // 清除L2缓存
        try {
            redisTemplate.delete(permissionKey);
            redisTemplate.delete(roleKey);
            log.debug("清除用户权限缓存: userId={}, tenantId={}", userId, tenantId);
        } catch (Exception e) {
            log.warn("清除用户权限Redis缓存失败: userId={}, tenantId={}", userId, tenantId, e);
        }
    }

    /**
     * 清除角色权限缓存
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     */
    public void evictRolePermissionCache(Long roleId, String tenantId) {
        String cacheKey = buildRolePermissionKey(roleId, tenantId);
        
        rolePermissionCache.invalidate(cacheKey);
        
        try {
            redisTemplate.delete(cacheKey);
            log.debug("清除角色权限缓存: roleId={}, tenantId={}", roleId, tenantId);
        } catch (Exception e) {
            log.warn("清除角色权限Redis缓存失败: roleId={}, tenantId={}", roleId, tenantId, e);
        }
    }

    /**
     * 清除租户所有权限缓存
     *
     * @param tenantId 租户ID
     */
    public void evictTenantCache(String tenantId) {
        try {
            String pattern = "*:" + tenantId;
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("清除租户权限缓存: tenantId={}, keys={}", tenantId, keys.size());
            }
        } catch (Exception e) {
            log.warn("清除租户权限缓存失败: tenantId={}", tenantId, e);
        }
        
        // 清除本地缓存
        userPermissionCache.invalidateAll();
        rolePermissionCache.invalidateAll();
        userRoleCache.invalidateAll();
    }

    /**
     * 清除所有权限缓存
     */
    public void evictAllCache() {
        // 清除本地缓存
        userPermissionCache.invalidateAll();
        rolePermissionCache.invalidateAll();
        userRoleCache.invalidateAll();
        
        // 清除Redis缓存
        try {
            Set<String> keys = redisTemplate.keys(USER_PERMISSION_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            keys = redisTemplate.keys(ROLE_PERMISSION_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            keys = redisTemplate.keys(USER_ROLE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            log.info("清除所有权限缓存完成");
        } catch (Exception e) {
            log.warn("清除所有权限Redis缓存失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("userPermissionCache", Map.of(
                "size", userPermissionCache.estimatedSize(),
                "hitRate", userPermissionCache.stats().hitRate(),
                "missRate", userPermissionCache.stats().missRate(),
                "evictionCount", userPermissionCache.stats().evictionCount()
        ));
        
        stats.put("rolePermissionCache", Map.of(
                "size", rolePermissionCache.estimatedSize(),
                "hitRate", rolePermissionCache.stats().hitRate(),
                "missRate", rolePermissionCache.stats().missRate(),
                "evictionCount", rolePermissionCache.stats().evictionCount()
        ));
        
        stats.put("userRoleCache", Map.of(
                "size", userRoleCache.estimatedSize(),
                "hitRate", userRoleCache.stats().hitRate(),
                "missRate", userRoleCache.stats().missRate(),
                "evictionCount", userRoleCache.stats().evictionCount()
        ));
        
        return stats;
    }

    // ==================== 私有方法 ====================

    private String buildUserPermissionKey(Long userId, String tenantId) {
        return USER_PERMISSION_PREFIX + userId + ":" + tenantId;
    }

    private String buildRolePermissionKey(Long roleId, String tenantId) {
        return ROLE_PERMISSION_PREFIX + roleId + ":" + tenantId;
    }

    private String buildUserRoleKey(Long userId, String tenantId) {
        return USER_ROLE_PREFIX + userId + ":" + tenantId;
    }
}