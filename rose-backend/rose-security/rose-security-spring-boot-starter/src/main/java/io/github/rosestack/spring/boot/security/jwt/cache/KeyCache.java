package io.github.rosestack.spring.boot.security.jwt.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * 密钥缓存管理器
 *
 * <p>特性：
 * <ul>
 *   <li>线程安全的读写锁机制</li>
 *   <li>支持TTL过期检查</li>
 *   <li>支持缓存回退策略</li>
 *   <li>泛型设计，支持各种类型的缓存对象</li>
 * </ul>
 * </p>
 *
 * @param <T> 缓存对象类型
 */
@Slf4j
public class KeyCache<T> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final String cacheName;
    private final Duration ttl;
    private final boolean fallbackToCache;

    private volatile T cachedValue;
    private volatile Instant lastLoadTime;
    private volatile Exception lastException;

    public KeyCache(String cacheName, Duration ttl, boolean fallbackToCache) {
        this.cacheName = cacheName;
        this.ttl = ttl;
        this.fallbackToCache = fallbackToCache;
    }

    /**
     * 获取缓存值，如果过期或不存在则重新加载
     *
     * @param loader 加载器函数
     * @return 缓存的值
     * @throws Exception 加载失败且无可用缓存时抛出异常
     */
    public T get(Supplier<T> loader) throws Exception {
        // 先尝试读锁
        lock.readLock().lock();
        try {
            if (cachedValue != null && !isExpired()) {
                log.debug("缓存命中: {}", cacheName);
                return cachedValue;
            }
        } finally {
            lock.readLock().unlock();
        }

        // 需要重新加载，获取写锁
        lock.writeLock().lock();
        try {
            // 双重检查，防止并发重复加载
            if (cachedValue != null && !isExpired()) {
                return cachedValue;
            }

            log.debug("重新加载缓存: {}", cacheName);
            try {
                T newValue = loader.get();
                if (newValue != null) {
                    cachedValue = newValue;
                    lastLoadTime = Instant.now();
                    lastException = null;
                    log.debug("缓存加载成功: {}", cacheName);
                    return newValue;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("缓存加载失败: {}, 错误: {}", cacheName, e.getMessage());

                // 如果支持回退且有旧缓存，则使用旧缓存
                if (fallbackToCache && cachedValue != null) {
                    log.info("使用回退缓存: {}", cacheName);
                    return cachedValue;
                }

                throw e;
            }

            throw new IllegalStateException("加载器返回了null值");

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 强制刷新缓存
     *
     * @param loader 加载器函数
     * @return 新的缓存值
     * @throws Exception 加载失败时抛出异常
     */
    public T refresh(Supplier<T> loader) throws Exception {
        lock.writeLock().lock();
        try {
            log.debug("强制刷新缓存: {}", cacheName);
            T newValue = loader.get();
            if (newValue != null) {
                cachedValue = newValue;
                lastLoadTime = Instant.now();
                lastException = null;
                log.debug("缓存刷新成功: {}", cacheName);
                return newValue;
            }
            throw new IllegalStateException("加载器返回了null值");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 清除缓存
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cachedValue = null;
            lastLoadTime = null;
            lastException = null;
            log.debug("缓存已清除: {}", cacheName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取缓存状态信息
     *
     * @return 缓存状态
     */
    public CacheStatus getStatus() {
        lock.readLock().lock();
        try {
            return new CacheStatus(cacheName, cachedValue != null, lastLoadTime, isExpired(), lastException);
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isExpired() {
        if (lastLoadTime == null || ttl == null) {
            return true;
        }
        return Instant.now().isAfter(lastLoadTime.plus(ttl));
    }

    /**
     * 缓存状态信息
     */
    public static class CacheStatus {
        public final String name;
        public final boolean hasValue;
        public final Instant lastLoadTime;
        public final boolean expired;
        public final Exception lastException;

        public CacheStatus(
                String name, boolean hasValue, Instant lastLoadTime, boolean expired, Exception lastException) {
            this.name = name;
            this.hasValue = hasValue;
            this.lastLoadTime = lastLoadTime;
            this.expired = expired;
            this.lastException = lastException;
        }

        @Override
        public String toString() {
            return String.format(
                    "CacheStatus{name='%s', hasValue=%s, expired=%s, lastLoad=%s}",
                    name, hasValue, expired, lastLoadTime);
        }
    }
}
