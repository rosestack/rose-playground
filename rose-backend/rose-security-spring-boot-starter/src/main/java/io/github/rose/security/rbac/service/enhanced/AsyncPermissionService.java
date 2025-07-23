package io.github.rose.security.rbac.service.enhanced;

import io.github.rose.security.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 异步权限服务
 * <p>
 * 提供异步权限检查和批量权限操作，提升高并发场景下的性能。
 * 支持权限预加载、批量检查和智能缓存预热。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPermissionService {

    private final PermissionService permissionService;

    /**
     * 异步权限检查
     *
     * @param userId     用户ID
     * @param permission 权限名称
     * @param tenantId   租户ID
     * @return 权限检查结果的Future
     */
    @Async("permissionExecutor")
    public CompletableFuture<Boolean> hasPermissionAsync(Long userId, String permission, String tenantId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return permissionService.hasPermission(userId, permission, tenantId);
            } catch (Exception e) {
                log.error("异步权限检查失败: userId={}, permission={}, tenantId={}", userId, permission, tenantId, e);
                return false;
            }
        });
    }

    /**
     * 批量异步权限检查
     *
     * @param userId      用户ID
     * @param permissions 权限名称集合
     * @param tenantId    租户ID
     * @return 权限检查结果映射的Future
     */
    @Async("permissionExecutor")
    public CompletableFuture<Map<String, Boolean>> batchCheckPermissionsAsync(
            Long userId, Set<String> permissions, String tenantId) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 优化：先获取用户所有权限，然后批量检查
                Set<String> userPermissions = permissionService.getUserPermissions(userId, tenantId);
                
                Map<String, Boolean> result = new ConcurrentHashMap<>();
                permissions.parallelStream().forEach(permission -> {
                    result.put(permission, userPermissions.contains(permission));
                });
                
                return result;
            } catch (Exception e) {
                log.error("批量异步权限检查失败: userId={}, permissions={}, tenantId={}", 
                        userId, permissions, tenantId, e);
                return permissions.stream()
                        .collect(Collectors.toMap(p -> p, p -> false));
            }
        });
    }

    /**
     * 批量用户权限检查
     *
     * @param userIds    用户ID集合
     * @param permission 权限名称
     * @param tenantId   租户ID
     * @return 用户权限检查结果映射的Future
     */
    @Async("permissionExecutor")
    public CompletableFuture<Map<Long, Boolean>> batchCheckUsersPermissionAsync(
            Set<Long> userIds, String permission, String tenantId) {
        
        return CompletableFuture.supplyAsync(() -> {
            Map<Long, Boolean> result = new ConcurrentHashMap<>();
            
            // 并行处理用户权限检查
            userIds.parallelStream().forEach(userId -> {
                try {
                    boolean hasPermission = permissionService.hasPermission(userId, permission, tenantId);
                    result.put(userId, hasPermission);
                } catch (Exception e) {
                    log.error("用户权限检查失败: userId={}, permission={}, tenantId={}", 
                            userId, permission, tenantId, e);
                    result.put(userId, false);
                }
            });
            
            return result;
        });
    }

    /**
     * 预加载用户权限
     *
     * @param userIds  用户ID集合
     * @param tenantId 租户ID
     * @return 预加载完成的Future
     */
    @Async("permissionExecutor")
    public CompletableFuture<Void> preloadUserPermissionsAsync(Set<Long> userIds, String tenantId) {
        return CompletableFuture.runAsync(() -> {
            try {
                userIds.parallelStream().forEach(userId -> {
                    try {
                        permissionService.getUserPermissions(userId, tenantId);
                        log.debug("预加载用户权限完成: userId={}, tenantId={}", userId, tenantId);
                    } catch (Exception e) {
                        log.error("预加载用户权限失败: userId={}, tenantId={}", userId, tenantId, e);
                    }
                });
                
                log.info("批量预加载用户权限完成: users={}, tenantId={}", userIds.size(), tenantId);
            } catch (Exception e) {
                log.error("批量预加载用户权限失败: tenantId={}", tenantId, e);
            }
        });
    }

    /**
     * 智能权限预热
     * 基于历史访问模式预加载热点权限
     *
     * @param tenantId 租户ID
     * @return 预热完成的Future
     */
    @Async("permissionExecutor")
    public CompletableFuture<Void> smartPermissionWarmupAsync(String tenantId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 获取热点权限（可以从监控指标中获取）
                Set<String> hotPermissions = getHotPermissions(tenantId);
                
                // 获取活跃用户
                Set<Long> activeUsers = getActiveUsers(tenantId);
                
                // 预加载热点权限
                activeUsers.parallelStream().forEach(userId -> {
                    hotPermissions.forEach(permission -> {
                        try {
                            permissionService.hasPermission(userId, permission, tenantId);
                        } catch (Exception e) {
                            log.debug("预热权限失败: userId={}, permission={}", userId, permission);
                        }
                    });
                });
                
                log.info("智能权限预热完成: tenantId={}, users={}, permissions={}", 
                        tenantId, activeUsers.size(), hotPermissions.size());
            } catch (Exception e) {
                log.error("智能权限预热失败: tenantId={}", tenantId, e);
            }
        });
    }

    /**
     * 权限检查结果聚合
     *
     * @param futures 权限检查Future列表
     * @return 聚合结果的Future
     */
    public CompletableFuture<PermissionCheckResult> aggregatePermissionResults(
            List<CompletableFuture<Boolean>> futures) {
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    long grantedCount = futures.stream()
                            .mapToLong(future -> {
                                try {
                                    return future.get() ? 1 : 0;
                                } catch (Exception e) {
                                    return 0;
                                }
                            })
                            .sum();
                    
                    return PermissionCheckResult.builder()
                            .totalChecks(futures.size())
                            .grantedCount(grantedCount)
                            .deniedCount(futures.size() - grantedCount)
                            .successRate((double) grantedCount / futures.size())
                            .build();
                });
    }

    // ==================== 私有方法 ====================

    /**
     * 获取热点权限
     */
    private Set<String> getHotPermissions(String tenantId) {
        // 这里可以从监控指标或缓存中获取热点权限
        // 暂时返回一些常用权限
        return Set.of(
                "user:read", "user:list", "role:read", 
                "permission:read", "dashboard:view"
        );
    }

    /**
     * 获取活跃用户
     */
    private Set<Long> getActiveUsers(String tenantId) {
        // 这里可以从用户活动记录中获取活跃用户
        // 暂时返回空集合，实际实现需要根据业务需求
        return Set.of();
    }

    // ==================== 内部类 ====================

    /**
     * 权限检查结果
     */
    public static class PermissionCheckResult {
        private final int totalChecks;
        private final long grantedCount;
        private final long deniedCount;
        private final double successRate;

        public static PermissionCheckResultBuilder builder() {
            return new PermissionCheckResultBuilder();
        }

        public static class PermissionCheckResultBuilder {
            private int totalChecks;
            private long grantedCount;
            private long deniedCount;
            private double successRate;

            public PermissionCheckResultBuilder totalChecks(int totalChecks) {
                this.totalChecks = totalChecks;
                return this;
            }

            public PermissionCheckResultBuilder grantedCount(long grantedCount) {
                this.grantedCount = grantedCount;
                return this;
            }

            public PermissionCheckResultBuilder deniedCount(long deniedCount) {
                this.deniedCount = deniedCount;
                return this;
            }

            public PermissionCheckResultBuilder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }

            public PermissionCheckResult build() {
                return new PermissionCheckResult(totalChecks, grantedCount, deniedCount, successRate);
            }
        }

        private PermissionCheckResult(int totalChecks, long grantedCount, long deniedCount, double successRate) {
            this.totalChecks = totalChecks;
            this.grantedCount = grantedCount;
            this.deniedCount = deniedCount;
            this.successRate = successRate;
        }

        // Getters
        public int getTotalChecks() { return totalChecks; }
        public long getGrantedCount() { return grantedCount; }
        public long getDeniedCount() { return deniedCount; }
        public double getSuccessRate() { return successRate; }
    }
}