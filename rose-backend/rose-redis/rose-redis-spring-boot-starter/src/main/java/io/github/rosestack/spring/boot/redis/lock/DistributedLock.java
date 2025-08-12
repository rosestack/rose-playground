package io.github.rosestack.spring.boot.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 *
 * <p>定义分布式锁的基本操作，支持可重入、超时、自动续期等特性。 基于 Redis 实现，确保在分布式环境下的线程安全。
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface DistributedLock {

    /**
     * 尝试获取锁（非阻塞）
     *
     * @return 是否成功获取锁
     */
    boolean tryLock();

    /**
     * 尝试获取锁（非阻塞，指定超时时间）
     *
     * @param timeout 锁的超时时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(long timeout, TimeUnit timeUnit);

    /**
     * 尝试获取锁（阻塞，指定等待时间和锁超时时间）
     *
     * @param waitTime 等待获取锁的时间
     * @param leaseTime 锁的超时时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     * @throws InterruptedException 等待过程中被中断
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 获取锁（阻塞）
     *
     * <p>如果锁不可用，当前线程将被阻塞直到获取到锁。
     *
     * @throws InterruptedException 等待过程中被中断
     */
    void lock() throws InterruptedException;

    /**
     * 获取锁（阻塞，指定锁超时时间）
     *
     * @param leaseTime 锁的超时时间
     * @param timeUnit 时间单位
     * @throws InterruptedException 等待过程中被中断
     */
    void lock(long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 释放锁
     *
     * @return 是否成功释放锁
     */
    boolean unlock();

    /**
     * 强制释放锁
     *
     * <p>无论锁是否由当前线程持有，都会强制释放锁。 谨慎使用，可能会导致并发问题。
     *
     * @return 是否成功释放锁
     */
    boolean forceUnlock();

    /**
     * 检查锁是否被持有
     *
     * @return 锁是否被持有
     */
    boolean isLocked();

    /**
     * 检查锁是否被当前线程持有
     *
     * @return 锁是否被当前线程持有
     */
    boolean isHeldByCurrentThread();

    /**
     * 获取锁的重入次数
     *
     * @return 重入次数
     */
    int getHoldCount();

    /**
     * 获取锁的剩余生存时间
     *
     * @return 剩余时间（毫秒），-1 表示永不过期，-2 表示锁不存在
     */
    long getRemainingTimeToLive();

    /**
     * 续期锁
     *
     * @param leaseTime 续期时间
     * @param timeUnit 时间单位
     * @return 是否续期成功
     */
    boolean renewLease(long leaseTime, TimeUnit timeUnit);

    /**
     * 获取锁名称
     *
     * @return 锁名称
     */
    String getName();
}
