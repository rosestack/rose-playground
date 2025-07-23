package io.github.rose.security.rbac.cache.enhanced;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.rose.security.rbac.cache.PermissionCacheService;
import io.github.rose.security.rbac.config.RbacProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 智能权限缓存服务
 * <p>
 * 提供智能化的权限缓存管理，包括：
 * - 自适应缓存策略
 * - 预测性缓存预热
 * - 智能失效策略
 * - 缓存性能优化
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartPermissionCacheService extends PermissionCacheService {

    private final RbacProperties rbacProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    // 智能缓存统计
    private final Map<String, CacheAccessStats> accessStats = new ConcurrentHashMap<>();
    private final AtomicLong totalCacheAccess = new AtomicLong(0);
    private final AtomicLong totalCacheHit = new AtomicLong(0);

    // 自适应缓存
    private LoadingCache<String, Set<String>> adaptiveUserPermissionCache;
    private LoadingCache<String, Set<String>> adaptiveRolePermissionCache;

    // 预测性缓存
    private final Map<String, PredictionModel> predictionModels = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeSmartCache() {
        initializeAdaptiveCache();
        initializePredictionModels();
        log.info("智能权限缓存服务初始化完成");
    }

    /**
     * 智能获取用户权限
     * 基于访问模式自动调整缓存策略
     */
    @Override
    public Set<String> getUserPermissions(Long userId, String tenantId) {
        String cacheKey = buildUserPermissionKey(userId, tenantId);
        
        // 记录访问统计
        recordCacheAccess(cacheKey);
        
        try {
            // 使用自适应缓存
            Set<String> permissions = adaptiveUserPermissionCache.get(cacheKey);
            
            if (permissions != null) {
                recordCacheHit(cacheKey);
                updateAccessPattern(cacheKey);
                return permissions;
            }
            
            // 回退到父类实现
            permissions = super.getUserPermissions(userId, tenantId);
            
            // 预测性缓存预热
            predictAndWarmup(userId, tenantId);
            
            return permissions;
            
        } catch (Exception e) {
            log.error("智能获取用户权限失败: userId={}, tenantId={}", userId, tenantId, e);
            return super.getUserPermissions(userId, tenantId);
        }
    }

    /**
     * 智能权限预热
     * 基于机器学习预测用户可能访问的权限
     */
    public CompletableFuture<Void> smartWarmup(String tenantId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 分析访问模式
                Map<String, Double> accessProbability = analyzeAccessPatterns(tenantId);
                
                // 预热高概率访问的权限
                accessProbability.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0.7) // 概率阈值
                        .forEach(entry -> {
                            String cacheKey = entry.getKey();
                            preloadCacheEntry(cacheKey);
                        });
                
                log.info("智能权限预热完成: tenantId={}, entries={}", 
                        tenantId, accessProbability.size());
                        
            } catch (Exception e) {
                log.error("智能权限预热失败: tenantId={}", tenantId, e);
            }
        });
    }

    /**
     * 自适应缓存清理
     * 基于访问频率和时间衰减智能清理缓存
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次
    public void adaptiveCacheCleanup() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            accessStats.entrySet().removeIf(entry -> {
                CacheAccessStats stats = entry.getValue();
                
                // 计算时间衰减因子
                long minutesSinceLastAccess = Duration.between(stats.getLastAccessTime(), now).toMinutes();
                double decayFactor = Math.exp(-minutesSinceLastAccess / 60.0); // 1小时衰减因子
                
                // 计算调整后的访问频率
                double adjustedFrequency = stats.getAccessCount() * decayFactor;
                
                // 清理低频访问的缓存项
                if (adjustedFrequency < 1.0) {
                    String cacheKey = entry.getKey();
                    adaptiveUserPermissionCache.invalidate(cacheKey);
                    adaptiveRolePermissionCache.invalidate(cacheKey);
                    
                    log.debug("清理低频缓存项: key={}, frequency={}", cacheKey, adjustedFrequency);
                    return true;
                }
                
                return false;
            });
            
        } catch (Exception e) {
            log.error("自适应缓存清理失败", e);
        }
    }

    /**
     * 缓存性能分析
     */
    public CachePerformanceReport generatePerformanceReport() {
        try {
            double overallHitRate = totalCacheAccess.get() > 0 ? 
                    (double) totalCacheHit.get() / totalCacheAccess.get() : 0.0;
            
            Map<String, Double> keyHitRates = new ConcurrentHashMap<>();
            accessStats.forEach((key, stats) -> {
                double hitRate = stats.getAccessCount() > 0 ? 
                        (double) stats.getHitCount() / stats.getAccessCount() : 0.0;
                keyHitRates.put(key, hitRate);
            });
            
            return CachePerformanceReport.builder()
                    .overallHitRate(overallHitRate)
                    .totalAccess(totalCacheAccess.get())
                    .totalHits(totalCacheHit.get())
                    .keyHitRates(keyHitRates)
                    .cacheSize(adaptiveUserPermissionCache.estimatedSize())
                    .generatedTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("生成缓存性能报告失败", e);
            return CachePerformanceReport.empty();
        }
    }

    /**
     * 缓存健康检查
     */
    public CacheHealthStatus checkCacheHealth() {
        try {
            double hitRate = totalCacheAccess.get() > 0 ? 
                    (double) totalCacheHit.get() / totalCacheAccess.get() : 0.0;
            
            long cacheSize = adaptiveUserPermissionCache.estimatedSize();
            long maxSize = rbacProperties.getCache().getLocal().getUserPermissionMaxSize();
            double utilizationRate = (double) cacheSize / maxSize;
            
            CacheHealthStatus.HealthLevel healthLevel;
            if (hitRate > 0.8 && utilizationRate < 0.9) {
                healthLevel = CacheHealthStatus.HealthLevel.HEALTHY;
            } else if (hitRate > 0.6 && utilizationRate < 0.95) {
                healthLevel = CacheHealthStatus.HealthLevel.WARNING;
            } else {
                healthLevel = CacheHealthStatus.HealthLevel.CRITICAL;
            }
            
            return CacheHealthStatus.builder()
                    .healthLevel(healthLevel)
                    .hitRate(hitRate)
                    .utilizationRate(utilizationRate)
                    .cacheSize(cacheSize)
                    .maxSize(maxSize)
                    .checkTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("缓存健康检查失败", e);
            return CacheHealthStatus.critical();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化自适应缓存
     */
    private void initializeAdaptiveCache() {
        RbacProperties.LocalCacheConfig localConfig = rbacProperties.getCache().getLocal();
        
        this.adaptiveUserPermissionCache = Caffeine.newBuilder()
                .maximumSize(localConfig.getUserPermissionMaxSize())
                .expireAfterWrite(localConfig.getUserPermissionExpireTime())
                .expireAfterAccess(Duration.ofMinutes(30)) // 访问后过期
                .refreshAfterWrite(Duration.ofMinutes(10)) // 写入后刷新
                .recordStats()
                .build(this::loadUserPermissionsFromDatabase);
        
        this.adaptiveRolePermissionCache = Caffeine.newBuilder()
                .maximumSize(localConfig.getRolePermissionMaxSize())
                .expireAfterWrite(localConfig.getRolePermissionExpireTime())
                .expireAfterAccess(Duration.ofHours(1))
                .refreshAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .build(this::loadRolePermissionsFromDatabase);
    }

    /**
     * 初始化预测模型
     */
    private void initializePredictionModels() {
        // 初始化简单的预测模型
        // 实际实现可以使用更复杂的机器学习算法
        predictionModels.put("user_permission", new SimplePredictionModel());
        predictionModels.put("role_permission", new SimplePredictionModel());
    }

    /**
     * 记录缓存访问
     */
    private void recordCacheAccess(String cacheKey) {
        totalCacheAccess.incrementAndGet();
        
        accessStats.computeIfAbsent(cacheKey, k -> new CacheAccessStats())
                .incrementAccess();
    }

    /**
     * 记录缓存命中
     */
    private void recordCacheHit(String cacheKey) {
        totalCacheHit.incrementAndGet();
        
        CacheAccessStats stats = accessStats.get(cacheKey);
        if (stats != null) {
            stats.incrementHit();
        }
    }

    /**
     * 更新访问模式
     */
    private void updateAccessPattern(String cacheKey) {
        CacheAccessStats stats = accessStats.get(cacheKey);
        if (stats != null) {
            stats.updateLastAccessTime();
        }
    }

    /**
     * 分析访问模式
     */
    private Map<String, Double> analyzeAccessPatterns(String tenantId) {
        Map<String, Double> patterns = new ConcurrentHashMap<>();
        
        accessStats.entrySet().stream()
                .filter(entry -> entry.getKey().contains(tenantId))
                .forEach(entry -> {
                    String key = entry.getKey();
                    CacheAccessStats stats = entry.getValue();
                    
                    // 简单的概率计算：基于访问频率和时间衰减
                    long minutesSinceLastAccess = Duration.between(
                            stats.getLastAccessTime(), LocalDateTime.now()).toMinutes();
                    double timeFactor = Math.exp(-minutesSinceLastAccess / 120.0); // 2小时衰减
                    double probability = Math.min(1.0, stats.getAccessCount() * 0.1 * timeFactor);
                    
                    patterns.put(key, probability);
                });
        
        return patterns;
    }

    /**
     * 预测性缓存预热
     */
    private void predictAndWarmup(Long userId, String tenantId) {
        CompletableFuture.runAsync(() -> {
            try {
                PredictionModel model = predictionModels.get("user_permission");
                if (model != null) {
                    Set<String> predictedKeys = model.predict(userId, tenantId);
                    predictedKeys.forEach(this::preloadCacheEntry);
                }
            } catch (Exception e) {
                log.debug("预测性缓存预热失败: userId={}, tenantId={}", userId, tenantId, e);
            }
        });
    }

    /**
     * 预加载缓存项
     */
    private void preloadCacheEntry(String cacheKey) {
        try {
            if (cacheKey.contains("user:permissions:")) {
                adaptiveUserPermissionCache.get(cacheKey);
            } else if (cacheKey.contains("role:permissions:")) {
                adaptiveRolePermissionCache.get(cacheKey);
            }
        } catch (Exception e) {
            log.debug("预加载缓存项失败: key={}", cacheKey, e);
        }
    }

    /**
     * 从数据库加载用户权限
     */
    private Set<String> loadUserPermissionsFromDatabase(String cacheKey) {
        // 解析缓存键获取用户ID和租户ID
        String[] parts = cacheKey.replace("user:permissions:", "").split(":");
        if (parts.length == 2) {
            Long userId = Long.parseLong(parts[0]);
            String tenantId = parts[1];
            return super.getUserPermissions(userId, tenantId);
        }
        return Set.of();
    }

    /**
     * 从数据库加载角色权限
     */
    private Set<String> loadRolePermissionsFromDatabase(String cacheKey) {
        // 解析缓存键获取角色ID和租户ID
        String[] parts = cacheKey.replace("role:permissions:", "").split(":");
        if (parts.length == 2) {
            Long roleId = Long.parseLong(parts[0]);
            String tenantId = parts[1];
            return super.getRolePermissions(roleId, tenantId);
        }
        return Set.of();
    }

    // ==================== 内部类 ====================

    /**
     * 缓存访问统计
     */
    private static class CacheAccessStats {
        private final AtomicLong accessCount = new AtomicLong(0);
        private final AtomicLong hitCount = new AtomicLong(0);
        private volatile LocalDateTime lastAccessTime = LocalDateTime.now();

        public void incrementAccess() {
            accessCount.incrementAndGet();
        }

        public void incrementHit() {
            hitCount.incrementAndGet();
        }

        public void updateLastAccessTime() {
            lastAccessTime = LocalDateTime.now();
        }

        public long getAccessCount() { return accessCount.get(); }
        public long getHitCount() { return hitCount.get(); }
        public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    }

    /**
     * 简单预测模型
     */
    private static class SimplePredictionModel implements PredictionModel {
        @Override
        public Set<String> predict(Long userId, String tenantId) {
            // 简单的预测逻辑：预测用户可能访问的相关权限
            return Set.of(
                    "user:permissions:" + userId + ":" + tenantId,
                    "user:roles:" + userId + ":" + tenantId
            );
        }
    }

    /**
     * 预测模型接口
     */
    private interface PredictionModel {
        Set<String> predict(Long userId, String tenantId);
    }

    /**
     * 缓存性能报告
     */
    public static class CachePerformanceReport {
        private final double overallHitRate;
        private final long totalAccess;
        private final long totalHits;
        private final Map<String, Double> keyHitRates;
        private final long cacheSize;
        private final LocalDateTime generatedTime;

        public static CachePerformanceReportBuilder builder() {
            return new CachePerformanceReportBuilder();
        }

        public static CachePerformanceReport empty() {
            return new CachePerformanceReport(0.0, 0, 0, Map.of(), 0, LocalDateTime.now());
        }

        // Builder和构造函数实现...
        public static class CachePerformanceReportBuilder {
            private double overallHitRate;
            private long totalAccess;
            private long totalHits;
            private Map<String, Double> keyHitRates;
            private long cacheSize;
            private LocalDateTime generatedTime;

            public CachePerformanceReportBuilder overallHitRate(double overallHitRate) {
                this.overallHitRate = overallHitRate;
                return this;
            }

            public CachePerformanceReportBuilder totalAccess(long totalAccess) {
                this.totalAccess = totalAccess;
                return this;
            }

            public CachePerformanceReportBuilder totalHits(long totalHits) {
                this.totalHits = totalHits;
                return this;
            }

            public CachePerformanceReportBuilder keyHitRates(Map<String, Double> keyHitRates) {
                this.keyHitRates = keyHitRates;
                return this;
            }

            public CachePerformanceReportBuilder cacheSize(long cacheSize) {
                this.cacheSize = cacheSize;
                return this;
            }

            public CachePerformanceReportBuilder generatedTime(LocalDateTime generatedTime) {
                this.generatedTime = generatedTime;
                return this;
            }

            public CachePerformanceReport build() {
                return new CachePerformanceReport(overallHitRate, totalAccess, totalHits, 
                        keyHitRates, cacheSize, generatedTime);
            }
        }

        private CachePerformanceReport(double overallHitRate, long totalAccess, long totalHits,
                                     Map<String, Double> keyHitRates, long cacheSize, LocalDateTime generatedTime) {
            this.overallHitRate = overallHitRate;
            this.totalAccess = totalAccess;
            this.totalHits = totalHits;
            this.keyHitRates = keyHitRates;
            this.cacheSize = cacheSize;
            this.generatedTime = generatedTime;
        }

        // Getters
        public double getOverallHitRate() { return overallHitRate; }
        public long getTotalAccess() { return totalAccess; }
        public long getTotalHits() { return totalHits; }
        public Map<String, Double> getKeyHitRates() { return keyHitRates; }
        public long getCacheSize() { return cacheSize; }
        public LocalDateTime getGeneratedTime() { return generatedTime; }
    }

    /**
     * 缓存健康状态
     */
    public static class CacheHealthStatus {
        public enum HealthLevel { HEALTHY, WARNING, CRITICAL }

        private final HealthLevel healthLevel;
        private final double hitRate;
        private final double utilizationRate;
        private final long cacheSize;
        private final long maxSize;
        private final LocalDateTime checkTime;

        public static CacheHealthStatusBuilder builder() {
            return new CacheHealthStatusBuilder();
        }

        public static CacheHealthStatus critical() {
            return new CacheHealthStatus(HealthLevel.CRITICAL, 0.0, 1.0, 0, 0, LocalDateTime.now());
        }

        public static class CacheHealthStatusBuilder {
            private HealthLevel healthLevel;
            private double hitRate;
            private double utilizationRate;
            private long cacheSize;
            private long maxSize;
            private LocalDateTime checkTime;

            public CacheHealthStatusBuilder healthLevel(HealthLevel healthLevel) {
                this.healthLevel = healthLevel;
                return this;
            }

            public CacheHealthStatusBuilder hitRate(double hitRate) {
                this.hitRate = hitRate;
                return this;
            }

            public CacheHealthStatusBuilder utilizationRate(double utilizationRate) {
                this.utilizationRate = utilizationRate;
                return this;
            }

            public CacheHealthStatusBuilder cacheSize(long cacheSize) {
                this.cacheSize = cacheSize;
                return this;
            }

            public CacheHealthStatusBuilder maxSize(long maxSize) {
                this.maxSize = maxSize;
                return this;
            }

            public CacheHealthStatusBuilder checkTime(LocalDateTime checkTime) {
                this.checkTime = checkTime;
                return this;
            }

            public CacheHealthStatus build() {
                return new CacheHealthStatus(healthLevel, hitRate, utilizationRate, 
                        cacheSize, maxSize, checkTime);
            }
        }

        private CacheHealthStatus(HealthLevel healthLevel, double hitRate, double utilizationRate,
                                long cacheSize, long maxSize, LocalDateTime checkTime) {
            this.healthLevel = healthLevel;
            this.hitRate = hitRate;
            this.utilizationRate = utilizationRate;
            this.cacheSize = cacheSize;
            this.maxSize = maxSize;
            this.checkTime = checkTime;
        }

        // Getters
        public HealthLevel getHealthLevel() { return healthLevel; }
        public double getHitRate() { return hitRate; }
        public double getUtilizationRate() { return utilizationRate; }
        public long getCacheSize() { return cacheSize; }
        public long getMaxSize() { return maxSize; }
        public LocalDateTime getCheckTime() { return checkTime; }
    }
}