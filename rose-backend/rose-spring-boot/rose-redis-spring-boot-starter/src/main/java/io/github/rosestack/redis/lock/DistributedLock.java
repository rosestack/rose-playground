package io.github.rosestack.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 * <p>
 * 定义分布式锁的基本操作，支持可重入、超时、自动续期等特性。
 * 基于 Redis 实现，确保在分布式环境下的线程安全。
 * </p>
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
     * @param timeout  锁的超时时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(long timeout, TimeUnit timeUnit);

    /**
     * 尝试获取锁（阻塞，指定等待时间和锁超时时间）
     *
     * @param waitTime    等待获取锁的时间
     * @param leaseTime   锁的超时时间
     * @param timeUnit    时间单位
     * @return 是否成功获取锁
     * @throws InterruptedException 等待过程中被中断
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 获取锁（阻塞）
     * <p>
     * 如果锁不可用，当前线程将被阻塞直到获取到锁。
     * </p>
     *
     * @throws InterruptedException 等待过程中被中断
     */
    void lock() throws InterruptedException;

    /**
     * 获取锁（阻塞，指定锁超时时间）
     *
     * @param leaseTime 锁的超时时间
     * @param timeUnit  时间单位
     * @throws InterruptedException 等待过程中被中断
     */
    void lock(long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 释放锁
     * <p>
     * 只有锁的持有者才能释放锁。支持可重入锁的计数递减。
     * </p>
     *
     * @return 是否成功释放锁
     */
    boolean unlock();

    /**
     * 强制释放锁
     * <p>
     * 强制释放锁，不检查锁的持有者。谨慎使用！
     * </p>
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
     * 检查当前线程是否持有锁
     *
     * @return 当前线程是否持有锁
     */
    boolean isHeldByCurrentThread();

    /**
     * 获取锁的重入次数
     *
     * @return 重入次数，如果当前线程未持有锁则返回 0
     */
    int getHoldCount();

    /**
     * 获取锁的剩余时间（毫秒）
     *
     * @return 剩余时间，如果锁未被持有或没有设置超时则返回 -1
     */
    long getRemainingTimeToLive();

    /**
     * 续期锁
     * <p>
     * 延长锁的超时时间，通常用于自动续期机制。
     * </p>
     *
     * @param leaseTime 续期时间
     * @param timeUnit  时间单位
     * @return 是否成功续期
     */
    boolean renewLease(long leaseTime, TimeUnit timeUnit);

    /**
     * 获取锁的名称
     *
     * @return 锁的名称
     */
    String getName();
}