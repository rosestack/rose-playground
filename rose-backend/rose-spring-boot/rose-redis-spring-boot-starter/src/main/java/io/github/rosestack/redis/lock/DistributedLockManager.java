package io.github.rosestack.redis.lock;

import io.github.rosestack.redis.config.RoseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁管理器
 * <p>
 * 负责创建、管理和销毁分布式锁实例。提供锁的生命周期管理和资源清理。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DistributedLockManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoseRedisProperties properties;
    
    // 锁实例缓存
    private final ConcurrentHashMap<String, DistributedLock> lockCache = new ConcurrentHashMap<>();
    
    // 续期任务调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(), 
            r -> {
                Thread thread = new Thread(r, "redis-lock-renewal");
                thread.setDaemon(true);
                return thread;
            }
    );

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
     * @param lockName        锁名称
     * @param defaultTimeout  默认超时时间（毫秒）
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
                    redisTemplate, 
                    key, 
                    defaultTimeout,
                    properties.getLock().isAutoRenewal() ? scheduler : null
            );
        });
    }

    /**
     * 尝试获取锁并执行任务
     *
     * @param lockName 锁名称
     * @param task     要执行的任务
     * @param <T>      返回值类型
     * @return 任务执行结果
     * @throws LockException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockName, LockTask<T> task) throws LockException {
        return executeWithLock(lockName, properties.getLock().getDefaultTimeout(), TimeUnit.MILLISECONDS, task);
    }

    /**
     * 尝试获取锁并执行任务（指定超时时间）
     *
     * @param lockName  锁名称
     * @param timeout   超时时间
     * @param timeUnit  时间单位
     * @param task      要执行的任务
     * @param <T>       返回值类型
     * @return 任务执行结果
     * @throws LockException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockName, long timeout, TimeUnit timeUnit, LockTask<T> task) throws LockException {
        DistributedLock lock = getLock(lockName);
        
        try {
            if (lock.tryLock(timeout, timeUnit)) {
                try {
                    return task.execute();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new LockException("获取锁超时: " + lockName);
            }
        } catch (Exception e) {
            if (e instanceof LockException) {
                throw (LockException) e;
            }
            throw new LockException("执行锁任务失败: " + lockName, e);
        }
    }

    /**
     * 尝试获取锁并执行任务（带等待时间）
     *
     * @param lockName   锁名称
     * @param waitTime   等待时间
     * @param leaseTime  锁持有时间
     * @param timeUnit   时间单位
     * @param task       要执行的任务
     * @param <T>        返回值类型
     * @return 任务执行结果
     * @throws LockException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockName, long waitTime, long leaseTime, TimeUnit timeUnit, LockTask<T> task) throws LockException {
        DistributedLock lock = getLock(lockName);
        
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                try {
                    return task.execute();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new LockException("获取锁超时: " + lockName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("等待锁被中断: " + lockName, e);
        } catch (Exception e) {
            if (e instanceof LockException) {
                throw (LockException) e;
            }
            throw new LockException("执行锁任务失败: " + lockName, e);
        }
    }

    /**
     * 强制释放锁
     *
     * @param lockName 锁名称
     * @return 是否成功释放
     */
    public boolean forceUnlock(String lockName) {
        String fullLockName = buildLockKey(lockName);
        DistributedLock lock = lockCache.get(fullLockName);
        
        if (lock != null) {
            return lock.forceUnlock();
        }
        
        // 如果缓存中没有，创建一个临时实例来释放
        DistributedLock tempLock = new RedisDistributedLock(
                redisTemplate, 
                fullLockName, 
                properties.getLock().getDefaultTimeout(),
                null
        );
        
        return tempLock.forceUnlock();
    }

    /**
     * 检查锁是否被持有
     *
     * @param lockName 锁名称
     * @return 是否被持有
     */
    public boolean isLocked(String lockName) {
        String fullLockName = buildLockKey(lockName);
        DistributedLock lock = lockCache.get(fullLockName);
        
        if (lock != null) {
            return lock.isLocked();
        }
        
        // 如果缓存中没有，直接检查 Redis
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullLockName));
    }

    /**
     * 获取锁的剩余时间
     *
     * @param lockName 锁名称
     * @return 剩余时间（毫秒），如果锁不存在返回 -1
     */
    public long getRemainingTimeToLive(String lockName) {
        String fullLockName = buildLockKey(lockName);
        DistributedLock lock = lockCache.get(fullLockName);
        
        if (lock != null) {
            return lock.getRemainingTimeToLive();
        }
        
        // 如果缓存中没有，直接查询 Redis
        Long ttl = redisTemplate.getExpire(fullLockName, TimeUnit.MILLISECONDS);
        return ttl != null ? ttl : -1;
    }

    /**
     * 清理锁缓存
     */
    public void clearLockCache() {
        lockCache.clear();
        log.info("已清理分布式锁缓存");
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
     * 构建完整的锁 key
     */
    private String buildLockKey(String lockName) {
        return properties.getLock().getKeyPrefix() + lockName;
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭分布式锁管理器...");
        
        // 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 清理缓存
        clearLockCache();
        
        log.info("分布式锁管理器已关闭");
    }

    /**
     * 锁任务接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface LockTask<T> {
        /**
         * 执行任务
         *
         * @return 任务结果
         * @throws Exception 任务执行异常
         */
        T execute() throws Exception;
    }

    /**
     * 锁异常
     */
    public static class LockException extends Exception {
        public LockException(String message) {
            super(message);
        }

        public LockException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}