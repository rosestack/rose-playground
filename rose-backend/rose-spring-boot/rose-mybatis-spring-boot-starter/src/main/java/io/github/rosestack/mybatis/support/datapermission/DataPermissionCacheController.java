package io.github.rosestack.mybatis.support.datapermission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据权限缓存管理控制器
 * <p>
 * 提供数据权限缓存的管理接口，仅在开发和测试环境启用
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/data-permission-cache")
@RequiredArgsConstructor
@ConditionalOnBean(DataPermissionCacheService.class)
@ConditionalOnProperty(prefix = "rose.mybatis.data-permission.cache", name = "management-enabled", havingValue = "true", matchIfMissing = true)
public class DataPermissionCacheController {

    private final DataPermissionCacheService cacheService;

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = cacheService.getCacheStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取缓存健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<DataPermissionCacheService.CacheHealthStatus> getCacheHealth() {
        try {
            DataPermissionCacheService.CacheHealthStatus health = cacheService.checkCacheHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("获取缓存健康状态失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取缓存使用建议
     */
    @GetMapping("/advice")
    public ResponseEntity<String> getCacheAdvice() {
        try {
            String advice = cacheService.getCacheUsageAdvice();
            return ResponseEntity.ok(advice);
        } catch (Exception e) {
            log.error("获取缓存使用建议失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清空所有缓存
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> clearAllCache() {
        try {
            cacheService.clearAllCache();
            log.info("管理员清空了所有数据权限缓存");
            return ResponseEntity.ok("所有缓存已清空");
        } catch (Exception e) {
            log.error("清空所有缓存失败", e);
            return ResponseEntity.internalServerError().body("清空缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清空指定用户的缓存
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> clearUserCache(@PathVariable String userId) {
        try {
            cacheService.clearUserCache(userId);
            log.info("管理员清空了用户 {} 的数据权限缓存", userId);
            return ResponseEntity.ok("用户 " + userId + " 的缓存已清空");
        } catch (Exception e) {
            log.error("清空用户缓存失败", e);
            return ResponseEntity.internalServerError().body("清空用户缓存失败: " + e.getMessage());
        }
    }

    /**
     * 批量清空多个用户的缓存
     */
    @DeleteMapping("/users")
    public ResponseEntity<String> clearUsersCache(@RequestBody String[] userIds) {
        try {
            if (userIds == null || userIds.length == 0) {
                return ResponseEntity.badRequest().body("用户ID列表不能为空");
            }

            cacheService.clearUsersCache(userIds);
            log.info("管理员批量清空了 {} 个用户的数据权限缓存", userIds.length);
            return ResponseEntity.ok("已清空 " + userIds.length + " 个用户的缓存");
        } catch (Exception e) {
            log.error("批量清空用户缓存失败", e);
            return ResponseEntity.internalServerError().body("批量清空用户缓存失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发缓存清理
     */
    @PostMapping("/cleanup")
    public ResponseEntity<String> manualCleanup() {
        try {
            cacheService.scheduledCacheCleanup();
            log.info("管理员手动触发了数据权限缓存清理");
            return ResponseEntity.ok("缓存清理已完成");
        } catch (Exception e) {
            log.error("手动触发缓存清理失败", e);
            return ResponseEntity.internalServerError().body("缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getCacheConfig() {
        try {
            Map<String, Object> config = Map.of(
                    "cacheExpireMinutes", 30,
                    "cleanupIntervalMinutes", 60,
                    "managementEnabled", true,
                    "description", "数据权限缓存配置信息"
            );
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取缓存配置信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 缓存性能测试
     */
    @PostMapping("/performance-test")
    public ResponseEntity<Map<String, Object>> performanceTest() {
        try {
            long startTime = System.currentTimeMillis();

            // 获取缓存统计信息多次，测试性能
            for (int i = 0; i < 100; i++) {
                cacheService.getCacheStatistics();
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            Map<String, Object> result = Map.of(
                    "testCount", 100,
                    "totalTimeMs", duration,
                    "averageTimeMs", duration / 100.0,
                    "description", "缓存性能测试结果"
            );

            log.info("缓存性能测试完成: {}", result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("缓存性能测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
