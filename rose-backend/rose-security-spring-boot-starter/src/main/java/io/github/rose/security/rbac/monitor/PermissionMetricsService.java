package io.github.rose.security.rbac.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 权限系统监控指标服务
 * <p>
 * 提供权限系统的性能监控和指标收集，包括：
 * - 权限检查性能指标
 * - 缓存命中率监控
 * - 权限使用统计
 * - 系统健康状态监控
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionMetricsService {

    private final MeterRegistry meterRegistry;

    // 权限检查计数器
    private final Counter permissionCheckCounter;
    private final Counter permissionGrantedCounter;
    private final Counter permissionDeniedCounter;

    // 权限检查耗时计时器
    private final Timer permissionCheckTimer;

    // 缓存相关指标
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Timer cacheLoadTimer;

    // 权限使用统计
    private final Map<String, AtomicLong> permissionUsageCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> userPermissionCount = new ConcurrentHashMap<>();

    // 系统健康指标
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong totalPermissions = new AtomicLong(0);
    private final AtomicLong totalRoles = new AtomicLong(0);

    public PermissionMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化计数器
        this.permissionCheckCounter = Counter.builder("rbac.permission.check.total")
                .description("Total number of permission checks")
                .register(meterRegistry);

        this.permissionGrantedCounter = Counter.builder("rbac.permission.granted.total")
                .description("Total number of granted permissions")
                .register(meterRegistry);

        this.permissionDeniedCounter = Counter.builder("rbac.permission.denied.total")
                .description("Total number of denied permissions")
                .register(meterRegistry);

        // 初始化计时器
        this.permissionCheckTimer = Timer.builder("rbac.permission.check.duration")
                .description("Permission check duration")
                .register(meterRegistry);

        // 初始化缓存指标
        this.cacheHitCounter = Counter.builder("rbac.cache.hit.total")
                .description("Total number of cache hits")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("rbac.cache.miss.total")
                .description("Total number of cache misses")
                .register(meterRegistry);

        this.cacheLoadTimer = Timer.builder("rbac.cache.load.duration")
                .description("Cache load duration")
                .register(meterRegistry);

        // 注册系统健康指标
        registerHealthMetrics();
    }

    /**
     * 记录权限检查指标
     *
     * @param permission 权限名称
     * @param granted    是否授权
     * @param duration   检查耗时
     * @param tenantId   租户ID
     */
    public void recordPermissionCheck(String permission, boolean granted, Duration duration, String tenantId) {
        try {
            // 记录总检查次数
            permissionCheckCounter.increment(
                    "permission", permission,
                    "tenant", tenantId,
                    "result", granted ? "granted" : "denied"
            );

            // 记录授权/拒绝次数
            if (granted) {
                permissionGrantedCounter.increment("permission", permission, "tenant", tenantId);
            } else {
                permissionDeniedCounter.increment("permission", permission, "tenant", tenantId);
            }

            // 记录检查耗时
            permissionCheckTimer.record(duration);

            // 更新权限使用统计
            updatePermissionUsage(permission, tenantId);

        } catch (Exception e) {
            log.error("记录权限检查指标失败: permission={}, granted={}", permission, granted, e);
        }
    }

    /**
     * 记录缓存命中指标
     *
     * @param cacheType 缓存类型
     * @param hit       是否命中
     * @param tenantId  租户ID
     */
    public void recordCacheAccess(String cacheType, boolean hit, String tenantId) {
        try {
            if (hit) {
                cacheHitCounter.increment("type", cacheType, "tenant", tenantId);
            } else {
                cacheMissCounter.increment("type", cacheType, "tenant", tenantId);
            }
        } catch (Exception e) {
            log.error("记录缓存访问指标失败: type={}, hit={}", cacheType, hit, e);
        }
    }

    /**
     * 记录缓存加载耗时
     *
     * @param cacheType 缓存类型
     * @param duration  加载耗时
     * @param tenantId  租户ID
     */
    public void recordCacheLoad(String cacheType, Duration duration, String tenantId) {
        try {
            cacheLoadTimer.record(duration);
        } catch (Exception e) {
            log.error("记录缓存加载指标失败: type={}, duration={}", cacheType, duration, e);
        }
    }

    /**
     * 记录用户权限检查
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    public void recordUserPermissionCheck(Long userId, String tenantId) {
        try {
            String key = userId + ":" + tenantId;
            userPermissionCount.computeIfAbsent(key, k -> {
                AtomicLong counter = new AtomicLong(0);
                // 注册用户权限检查次数指标
                Gauge.builder("rbac.user.permission.check.count")
                        .description("User permission check count")
                        .tags("user", userId.toString(), "tenant", tenantId)
                        .register(meterRegistry, counter, AtomicLong::get);
                return counter;
            }).incrementAndGet();
        } catch (Exception e) {
            log.error("记录用户权限检查指标失败: userId={}, tenantId={}", userId, tenantId, e);
        }
    }

    /**
     * 更新活跃用户数
     *
     * @param count 活跃用户数
     */
    public void updateActiveUsers(long count) {
        activeUsers.set(count);
    }

    /**
     * 更新权限总数
     *
     * @param count 权限总数
     */
    public void updateTotalPermissions(long count) {
        totalPermissions.set(count);
    }

    /**
     * 更新角色总数
     *
     * @param count 角色总数
     */
    public void updateTotalRoles(long count) {
        totalRoles.set(count);
    }

    /**
     * 获取权限使用统计
     *
     * @return 权限使用统计
     */
    public Map<String, Long> getPermissionUsageStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        permissionUsageCount.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 获取用户权限检查统计
     *
     * @return 用户权限检查统计
     */
    public Map<String, Long> getUserPermissionStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        userPermissionCount.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 获取缓存命中率
     *
     * @param cacheType 缓存类型
     * @param tenantId  租户ID
     * @return 缓存命中率
     */
    public double getCacheHitRate(String cacheType, String tenantId) {
        try {
            double hits = cacheHitCounter.count();
            double misses = cacheMissCounter.count();
            double total = hits + misses;
            
            return total > 0 ? hits / total : 0.0;
        } catch (Exception e) {
            log.error("获取缓存命中率失败: type={}, tenant={}", cacheType, tenantId, e);
            return 0.0;
        }
    }

    /**
     * 获取权限检查平均耗时
     *
     * @return 平均耗时（毫秒）
     */
    public double getAveragePermissionCheckTime() {
        try {
            return permissionCheckTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("获取权限检查平均耗时失败", e);
            return 0.0;
        }
    }

    /**
     * 获取系统健康指标
     *
     * @return 系统健康指标
     */
    public SystemHealthMetrics getSystemHealthMetrics() {
        return SystemHealthMetrics.builder()
                .activeUsers(activeUsers.get())
                .totalPermissions(totalPermissions.get())
                .totalRoles(totalRoles.get())
                .averagePermissionCheckTime(getAveragePermissionCheckTime())
                .cacheHitRate(getCacheHitRate("all", "all"))
                .totalPermissionChecks(permissionCheckCounter.count())
                .totalPermissionGrants(permissionGrantedCounter.count())
                .totalPermissionDenials(permissionDeniedCounter.count())
                .build();
    }

    /**
     * 重置指标
     */
    public void resetMetrics() {
        try {
            permissionUsageCount.clear();
            userPermissionCount.clear();
            activeUsers.set(0);
            totalPermissions.set(0);
            totalRoles.set(0);
            
            log.info("权限系统指标已重置");
        } catch (Exception e) {
            log.error("重置权限系统指标失败", e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 注册系统健康指标
     */
    private void registerHealthMetrics() {
        // 活跃用户数指标
        Gauge.builder("rbac.users.active")
                .description("Number of active users")
                .register(meterRegistry, activeUsers, AtomicLong::get);

        // 权限总数指标
        Gauge.builder("rbac.permissions.total")
                .description("Total number of permissions")
                .register(meterRegistry, totalPermissions, AtomicLong::get);

        // 角色总数指标
        Gauge.builder("rbac.roles.total")
                .description("Total number of roles")
                .register(meterRegistry, totalRoles, AtomicLong::get);

        // 缓存命中率指标
        Gauge.builder("rbac.cache.hit.rate")
                .description("Cache hit rate")
                .register(meterRegistry, this, PermissionMetricsService::calculateOverallCacheHitRate);
    }

    /**
     * 更新权限使用统计
     */
    private void updatePermissionUsage(String permission, String tenantId) {
        String key = permission + ":" + tenantId;
        permissionUsageCount.computeIfAbsent(key, k -> {
            AtomicLong counter = new AtomicLong(0);
            // 注册权限使用次数指标
            Gauge.builder("rbac.permission.usage.count")
                    .description("Permission usage count")
                    .tags("permission", permission, "tenant", tenantId)
                    .register(meterRegistry, counter, AtomicLong::get);
            return counter;
        }).incrementAndGet();
    }

    /**
     * 计算总体缓存命中率
     */
    private double calculateOverallCacheHitRate() {
        double hits = cacheHitCounter.count();
        double misses = cacheMissCounter.count();
        double total = hits + misses;
        
        return total > 0 ? hits / total : 0.0;
    }

    // ==================== 内部类 ====================

    /**
     * 系统健康指标
     */
    public static class SystemHealthMetrics {
        private final long activeUsers;
        private final long totalPermissions;
        private final long totalRoles;
        private final double averagePermissionCheckTime;
        private final double cacheHitRate;
        private final double totalPermissionChecks;
        private final double totalPermissionGrants;
        private final double totalPermissionDenials;

        public static SystemHealthMetricsBuilder builder() {
            return new SystemHealthMetricsBuilder();
        }

        // Builder pattern implementation
        public static class SystemHealthMetricsBuilder {
            private long activeUsers;
            private long totalPermissions;
            private long totalRoles;
            private double averagePermissionCheckTime;
            private double cacheHitRate;
            private double totalPermissionChecks;
            private double totalPermissionGrants;
            private double totalPermissionDenials;

            public SystemHealthMetricsBuilder activeUsers(long activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }

            public SystemHealthMetricsBuilder totalPermissions(long totalPermissions) {
                this.totalPermissions = totalPermissions;
                return this;
            }

            public SystemHealthMetricsBuilder totalRoles(long totalRoles) {
                this.totalRoles = totalRoles;
                return this;
            }

            public SystemHealthMetricsBuilder averagePermissionCheckTime(double averagePermissionCheckTime) {
                this.averagePermissionCheckTime = averagePermissionCheckTime;
                return this;
            }

            public SystemHealthMetricsBuilder cacheHitRate(double cacheHitRate) {
                this.cacheHitRate = cacheHitRate;
                return this;
            }

            public SystemHealthMetricsBuilder totalPermissionChecks(double totalPermissionChecks) {
                this.totalPermissionChecks = totalPermissionChecks;
                return this;
            }

            public SystemHealthMetricsBuilder totalPermissionGrants(double totalPermissionGrants) {
                this.totalPermissionGrants = totalPermissionGrants;
                return this;
            }

            public SystemHealthMetricsBuilder totalPermissionDenials(double totalPermissionDenials) {
                this.totalPermissionDenials = totalPermissionDenials;
                return this;
            }

            public SystemHealthMetrics build() {
                return new SystemHealthMetrics(activeUsers, totalPermissions, totalRoles,
                        averagePermissionCheckTime, cacheHitRate, totalPermissionChecks,
                        totalPermissionGrants, totalPermissionDenials);
            }
        }

        private SystemHealthMetrics(long activeUsers, long totalPermissions, long totalRoles,
                                  double averagePermissionCheckTime, double cacheHitRate,
                                  double totalPermissionChecks, double totalPermissionGrants,
                                  double totalPermissionDenials) {
            this.activeUsers = activeUsers;
            this.totalPermissions = totalPermissions;
            this.totalRoles = totalRoles;
            this.averagePermissionCheckTime = averagePermissionCheckTime;
            this.cacheHitRate = cacheHitRate;
            this.totalPermissionChecks = totalPermissionChecks;
            this.totalPermissionGrants = totalPermissionGrants;
            this.totalPermissionDenials = totalPermissionDenials;
        }

        // Getters
        public long getActiveUsers() { return activeUsers; }
        public long getTotalPermissions() { return totalPermissions; }
        public long getTotalRoles() { return totalRoles; }
        public double getAveragePermissionCheckTime() { return averagePermissionCheckTime; }
        public double getCacheHitRate() { return cacheHitRate; }
        public double getTotalPermissionChecks() { return totalPermissionChecks; }
        public double getTotalPermissionGrants() { return totalPermissionGrants; }
        public double getTotalPermissionDenials() { return totalPermissionDenials; }
    }
}