package io.github.rosestack.spring.boot.mybatis.support.datapermission;

import io.github.rosestack.spring.boot.mybatis.config.RoseMybatisProperties;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 数据权限缓存管理服务
 * <p>
 * 提供数据权限缓存的管理功能，包括：
 * 1. 定时清理过期缓存
 * 2. 手动清理缓存
 * 3. 缓存统计信息
 * 4. 缓存预热
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.mybatis.data-permission", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataPermissionCacheService {
    private final RoseMybatisProperties properties;
    private final RoseDataPermissionHandler dataPermissionHandler;
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "rose-mybatis-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        long intervalMinutes = properties.getDataPermission().getCache().getCleanupIntervalMinutes();

        log.info("开始调度定时清理任务, 清理间隔: {} 分钟", intervalMinutes);
        scheduledExecutorService.scheduleAtFixedRate(this::scheduledCacheCleanup,
                0, intervalMinutes, TimeUnit.MINUTES);
    }

    /**
     * 定时清理过期缓存
     * 每小时执行一次
     */
    public void scheduledCacheCleanup() {
        try {
            Map<String, Object> statsBefore = dataPermissionHandler.getCacheStats();
            log.info("开始定时清理数据权限缓存，当前缓存统计: {}", statsBefore);

            // 触发缓存清理（通过调用 getSqlSegment 方法间接触发）
            // 这里我们通过获取统计信息来触发清理逻辑
            Map<String, Object> statsAfter = dataPermissionHandler.getCacheStats();

            log.info("定时清理数据权限缓存完成，清理后缓存统计: {}", statsAfter);
        } catch (Exception e) {
            log.error("定时清理数据权限缓存失败", e);
        }
    }

    /**
     * 手动清空所有缓存
     */
    public void clearAllCache() {
        log.info("手动清空所有数据权限缓存");
        dataPermissionHandler.clearAllCache();
    }

    /**
     * 清空指定用户的权限缓存
     */
    public void clearUserCache(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("用户ID为空，无法清空用户缓存");
            return;
        }

        log.info("清空用户 {} 的数据权限缓存", userId);
        dataPermissionHandler.clearUserPermissionCache(userId);
    }

    /**
     * 批量清空多个用户的权限缓存
     */
    public void clearUsersCache(String... userIds) {
        if (userIds == null || userIds.length == 0) {
            log.warn("用户ID列表为空，无法清空用户缓存");
            return;
        }

        for (String userId : userIds) {
            clearUserCache(userId);
        }

        log.info("批量清空 {} 个用户的数据权限缓存完成", userIds.length);
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        return dataPermissionHandler.getCacheStats();
    }

    /**
     * 检查缓存健康状态
     */
    public CacheHealthStatus checkCacheHealth() {
        try {
            Map<String, Object> stats = getCacheStatistics();

            int annotationCacheSize = (Integer) stats.get("annotationCacheSize");
            int permissionCacheSize = (Integer) stats.get("permissionCacheSize");
            long expiredCount = (Long) stats.get("expiredPermissionCacheCount");

            // 计算过期率
            double expiredRate = permissionCacheSize > 0 ? (double) expiredCount / permissionCacheSize : 0;

            CacheHealthStatus status = new CacheHealthStatus();
            status.setHealthy(true);
            status.setAnnotationCacheSize(annotationCacheSize);
            status.setPermissionCacheSize(permissionCacheSize);
            status.setExpiredCacheCount(expiredCount);
            status.setExpiredRate(expiredRate);

            // 健康检查规则
            if (expiredRate > properties.getDataPermission().getCache().getExpiredRate()) { // 过期率超过50%
                status.setHealthy(false);
                status.addWarning("过期缓存比例过高: " + String.format("%.2f%%", expiredRate * 100));
            }

            if (annotationCacheSize > properties.getDataPermission().getCache().getMaxAnnotationCacheSize()) { // 注解缓存过大
                status.addWarning("注解缓存数量过多: " + annotationCacheSize);
            }

            if (permissionCacheSize > properties.getDataPermission().getCache().getMaxPermissionCacheSize()) { // 权限缓存过大
                status.addWarning("权限缓存数量过多: " + permissionCacheSize);
            }

            return status;

        } catch (Exception e) {
            log.error("检查缓存健康状态失败", e);

            CacheHealthStatus status = new CacheHealthStatus();
            status.setHealthy(false);
            status.addWarning("缓存健康检查异常: " + e.getMessage());
            return status;
        }
    }

    /**
     * 缓存健康状态
     */
    @Data
    public static class CacheHealthStatus {
        private boolean healthy = true;
        private int annotationCacheSize;
        private int permissionCacheSize;
        private long expiredCacheCount;
        private double expiredRate;
        private java.util.List<String> warnings = new java.util.ArrayList<>();

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        @Override
        public String toString() {
            return String.format("CacheHealthStatus{healthy=%s, annotationCache=%d, permissionCache=%d, expired=%d(%.2f%%), warnings=%s}",
                    healthy, annotationCacheSize, permissionCacheSize, expiredCacheCount, expiredRate * 100, warnings);
        }
    }

    /**
     * 获取缓存使用建议
     */
    public String getCacheUsageAdvice() {
        CacheHealthStatus health = checkCacheHealth();
        StringBuilder advice = new StringBuilder();

        advice.append("数据权限缓存使用建议:\n");
        advice.append("1. 当前缓存状态: ").append(health.isHealthy() ? "健康" : "需要关注").append("\n");
        advice.append("2. 注解缓存数量: ").append(health.getAnnotationCacheSize()).append("\n");
        advice.append("3. 权限缓存数量: ").append(health.getPermissionCacheSize()).append("\n");
        advice.append("4. 过期缓存比例: ").append(String.format("%.2f%%", health.getExpiredRate() * 100)).append("\n");

        if (!health.getWarnings().isEmpty()) {
            advice.append("5. 注意事项:\n");
            for (String warning : health.getWarnings()) {
                advice.append("   - ").append(warning).append("\n");
            }
        }

        advice.append("6. 优化建议:\n");
        advice.append("   - 定期清理不活跃用户的缓存\n");
        advice.append("   - 在用户权限变更时及时清理相关缓存\n");
        advice.append("   - 监控缓存命中率和过期率\n");

        return advice.toString();
    }
}
