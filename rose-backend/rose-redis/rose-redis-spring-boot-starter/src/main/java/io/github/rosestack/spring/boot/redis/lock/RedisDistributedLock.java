package io.github.rosestack.spring.boot.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 Redis 的分布式锁实现
 *
 * <p>支持可重入、超时、自动续期等特性。使用 Lua 脚本确保操作的原子性。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class RedisDistributedLock implements DistributedLock {

	// Lua 脚本：获取锁
	private static final String LOCK_SCRIPT = "if redis.call('exists', KEYS[1]) == 0 then "
		+ "  redis.call('hset', KEYS[1], ARGV[2], 1) "
		+ "  redis.call('pexpire', KEYS[1], ARGV[1]) "
		+ "  return nil "
		+ "end "
		+ "if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then "
		+ "  redis.call('hincrby', KEYS[1], ARGV[2], 1) "
		+ "  redis.call('pexpire', KEYS[1], ARGV[1]) "
		+ "  return nil "
		+ "end "
		+ "return redis.call('pttl', KEYS[1])";
	// Lua 脚本：释放锁
	private static final String UNLOCK_SCRIPT = "if redis.call('hexists', KEYS[1], ARGV[2]) == 0 then "
		+ "  return nil "
		+ "end "
		+ "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1) "
		+ "if counter > 0 then "
		+ "  redis.call('pexpire', KEYS[1], ARGV[1]) "
		+ "  return 0 "
		+ "else "
		+ "  redis.call('del', KEYS[1]) "
		+ "  return 1 "
		+ "end";
	// Lua 脚本：续期锁
	private static final String RENEWAL_SCRIPT = "if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then "
		+ "  redis.call('pexpire', KEYS[1], ARGV[1]) "
		+ "  return 1 "
		+ "else "
		+ "  return 0 "
		+ "end";
	// Lua 脚本：强制释放锁
	private static final String FORCE_UNLOCK_SCRIPT =
		"if redis.call('del', KEYS[1]) == 1 then " + "  return 1 " + "else " + "  return 0 " + "end";
	private final RedisTemplate<String, Object> redisTemplate;
	private final String lockName;
	private final String lockValue;
	private final long defaultLeaseTime;
	private final ScheduledExecutorService scheduler;
	// 可重入锁计数器
	private final ThreadLocal<AtomicInteger> holdCount = ThreadLocal.withInitial(() -> new AtomicInteger(0));
	// 续期任务
	private final ConcurrentHashMap<String, ScheduledFuture<?>> renewalTasks = new ConcurrentHashMap<>();

	public RedisDistributedLock(
		RedisTemplate<String, Object> redisTemplate,
		String lockName,
		long defaultLeaseTime,
		ScheduledExecutorService scheduler) {
		this.redisTemplate = redisTemplate;
		this.lockName = lockName;
		this.lockValue =
			UUID.randomUUID().toString() + ":" + Thread.currentThread().getId();
		this.defaultLeaseTime = defaultLeaseTime;
		this.scheduler = scheduler;
	}

	@Override
	public boolean tryLock() {
		return tryLock(defaultLeaseTime, TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit timeUnit) {
		long leaseTime = timeUnit.toMillis(timeout);
		return acquireLock(leaseTime);
	}

	@Override
	public boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
		long waitTimeMs = timeUnit.toMillis(waitTime);
		long leaseTimeMs = timeUnit.toMillis(leaseTime);
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < waitTimeMs) {
			if (acquireLock(leaseTimeMs)) {
				return true;
			}

			// 短暂等待后重试
			Thread.sleep(50);
		}

		return false;
	}

	@Override
	public void lock() throws InterruptedException {
		lock(defaultLeaseTime, TimeUnit.MILLISECONDS);
	}

	@Override
	public void lock(long leaseTime, TimeUnit timeUnit) throws InterruptedException {
		long leaseTimeMs = timeUnit.toMillis(leaseTime);

		while (!acquireLock(leaseTimeMs)) {
			// 等待一段时间后重试
			Thread.sleep(100);
		}
	}

	@Override
	public boolean unlock() {
		try {
			AtomicInteger count = holdCount.get();
			if (count.get() == 0) {
				log.warn("尝试释放未持有的锁: {}", lockName);
				return false;
			}

			DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
			Long result =
				redisTemplate.execute(script, Collections.singletonList(lockName), defaultLeaseTime, lockValue);

			if (result != null) {
				count.decrementAndGet();

				// 如果完全释放锁，清理续期任务
				if (result == 1) {
					count.set(0);
					holdCount.remove();
					cancelRenewalTask();
					log.debug("成功释放锁: {}", lockName);
				}
				return true;
			}

			return false;
		} catch (Exception e) {
			log.error("释放锁失败: {}", lockName, e);
			return false;
		}
	}

	@Override
	public boolean forceUnlock() {
		try {
			DefaultRedisScript<Long> script = new DefaultRedisScript<>(FORCE_UNLOCK_SCRIPT, Long.class);
			Long result = redisTemplate.execute(script, Collections.singletonList(lockName));

			if (result != null && result == 1) {
				holdCount.get().set(0);
				holdCount.remove();
				cancelRenewalTask();
				log.debug("强制释放锁: {}", lockName);
				return true;
			}

			return false;
		} catch (Exception e) {
			log.error("强制释放锁失败: {}", lockName, e);
			return false;
		}
	}

	@Override
	public boolean isLocked() {
		try {
			return redisTemplate.hasKey(lockName);
		} catch (Exception e) {
			log.error("检查锁状态失败: {}", lockName, e);
			return false;
		}
	}

	@Override
	public boolean isHeldByCurrentThread() {
		try {
			Object value = redisTemplate.opsForHash().get(lockName, lockValue);
			return value != null;
		} catch (Exception e) {
			log.error("检查锁持有状态失败: {}", lockName, e);
			return false;
		}
	}

	@Override
	public int getHoldCount() {
		return holdCount.get().get();
	}

	@Override
	public long getRemainingTimeToLive() {
		try {
			Long ttl = redisTemplate.getExpire(lockName, TimeUnit.MILLISECONDS);
			return ttl != null ? ttl : -1;
		} catch (Exception e) {
			log.error("获取锁剩余时间失败: {}", lockName, e);
			return -1;
		}
	}

	@Override
	public boolean renewLease(long leaseTime, TimeUnit timeUnit) {
		try {
			long leaseTimeMs = timeUnit.toMillis(leaseTime);
			DefaultRedisScript<Long> script = new DefaultRedisScript<>(RENEWAL_SCRIPT, Long.class);
			Long result = redisTemplate.execute(script, Collections.singletonList(lockName), leaseTimeMs, lockValue);

			return result != null && result == 1;
		} catch (Exception e) {
			log.error("续期锁失败: {}", lockName, e);
			return false;
		}
	}

	@Override
	public String getName() {
		return lockName;
	}

	/**
	 * 获取锁的核心逻辑
	 */
	private boolean acquireLock(long leaseTime) {
		try {
			DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
			Long result = redisTemplate.execute(script, Collections.singletonList(lockName), leaseTime, lockValue);

			if (result == null) {
				// 成功获取锁
				holdCount.get().incrementAndGet();
				scheduleRenewalTask(leaseTime);
				log.debug("成功获取锁: {}, 重入次数: {}", lockName, holdCount.get().get());
				return true;
			}

			return false;
		} catch (Exception e) {
			log.error("获取锁失败: {}", lockName, e);
			return false;
		}
	}

	/**
	 * 调度续期任务
	 */
	private void scheduleRenewalTask(long leaseTime) {
		if (scheduler == null) {
			return;
		}

		String taskKey = lockName + ":" + lockValue;

		// 取消之前的续期任务
		ScheduledFuture<?> existingTask = renewalTasks.get(taskKey);
		if (existingTask != null && !existingTask.isCancelled()) {
			existingTask.cancel(false);
		}

		// 调度新的续期任务，在锁过期前 1/3 时间进行续期
		long renewalInterval = leaseTime / 3;
		ScheduledFuture<?> renewalTask = scheduler.scheduleAtFixedRate(
			() -> {
				try {
					if (isHeldByCurrentThread()) {
						boolean renewed = renewLease(leaseTime, TimeUnit.MILLISECONDS);
						if (renewed) {
							log.debug("锁续期成功: {}", lockName);
						} else {
							log.warn("锁续期失败: {}", lockName);
							cancelRenewalTask();
						}
					} else {
						log.debug("锁不再被当前线程持有，取消续期任务: {}", lockName);
						cancelRenewalTask();
					}
				} catch (Exception e) {
					log.error("锁续期任务执行失败: {}", lockName, e);
					cancelRenewalTask();
				}
			},
			renewalInterval,
			renewalInterval,
			TimeUnit.MILLISECONDS);

		renewalTasks.put(taskKey, renewalTask);
	}

	/**
	 * 取消续期任务
	 */
	private void cancelRenewalTask() {
		String taskKey = lockName + ":" + lockValue;
		ScheduledFuture<?> task = renewalTasks.remove(taskKey);
		if (task != null && !task.isCancelled()) {
			task.cancel(false);
			log.debug("取消锁续期任务: {}", lockName);
		}
	}
}
