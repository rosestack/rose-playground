package io.github.rosestack.spring.boot.redis.lock;

import io.github.rosestack.spring.boot.redis.config.RoseRedisProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 分布式锁管理器
 *
 * <p>负责创建、管理和销毁分布式锁实例。提供锁的生命周期管理和资源清理。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseRedisProperties properties;

    // 锁实例缓存
    private final ConcurrentHashMap<String, DistributedLock> lockCache = new ConcurrentHashMap<>();

    // 续期任务调度器
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
                Thread thread = new Thread(r, "redis-lock-renewal");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * 获取分布式锁
     *
     * @param lockName 锁名称
     * @return 分布式锁实例
     */
    public DistributedLock getLock(String lockName) {
        return getLock(lockName, properties.getLock().getDefaultTimeout());
    }

    /**
     * 获取分布式锁（指定默认超时时间）
     *
     * @param lockName       锁名称
     * @param defaultTimeout 默认超时时间（毫秒）
     * @return 分布式锁实例
     */
    public DistributedLock getLock(String lockName, long defaultTimeout) {
        if (lockName == null || lockName.trim().isEmpty()) {
            throw new IllegalArgumentException("锁名称不能为空");
        }

        String fullLockName = buildLockKey(lockName);

        return lockCache.computeIfAbsent(fullLockName, key -> {
            log.debug("创建新的分布式锁实例: {}", key);
            return new RedisDistributedLock(
                    redisTemplate, key, defaultTimeout, properties.getLock().isAutoRenewal() ? scheduler : null);
        });
    }

    /**
     * 移除锁实例
     *
     * @param lockName 锁名称
     */
    public void removeLock(String lockName) {
        String fullLockName = buildLockKey(lockName);
        DistributedLock lock = lockCache.remove(fullLockName);
        if (lock != null) {
            log.debug("移除分布式锁实例: {}", fullLockName);
        }
    }

    /**
     * 获取当前缓存的锁数量
     *
     * @return 锁数量
     */
    public int getCachedLockCount() {
        return lockCache.size();
    }

    /**
     * 清理所有锁实例
     */
    public void clearAllLocks() {
        lockCache.clear();
        log.info("清理所有分布式锁实例");
    }

    /**
     * 构建完整的锁键名
     *
     * @param lockName 锁名称
     * @return 完整的锁键名
     */
    private String buildLockKey(String lockName) {
        String prefix = properties.getLock().getKeyPrefix();
        if (prefix == null || prefix.isEmpty()) {
            return lockName;
        }
        return prefix + lockName;
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        try {
            scheduler.shutdown();
            clearAllLocks();
            log.info("分布式锁管理器资源清理完成");
        } catch (Exception e) {
            log.error("分布式锁管理器资源清理失败", e);
        }
    }
}
