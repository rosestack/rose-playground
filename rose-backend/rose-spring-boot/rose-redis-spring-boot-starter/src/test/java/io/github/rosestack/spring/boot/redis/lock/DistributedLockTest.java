package io.github.rosestack.spring.boot.redis.lock;

import io.github.rosestack.spring.boot.redis.config.RoseRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DistributedLockTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private RoseRedisProperties properties;
    private DistributedLockManager lockManager;

    @BeforeEach
    void setUp() {
        properties = new RoseRedisProperties();
        properties.getLock().setKeyPrefix("test:lock:");
        properties.getLock().setDefaultTimeout(30000L);
        properties.getLock().setAutoRenewal(false); // 禁用自动续期以简化测试
        
        lockManager = new DistributedLockManager(redisTemplate, properties);
    }

    @Test
    void testGetLock() {
        // 测试获取锁实例
        DistributedLock lock1 = lockManager.getLock("test-lock");
        DistributedLock lock2 = lockManager.getLock("test-lock");
        
        assertNotNull(lock1);
        assertNotNull(lock2);
        assertSame(lock1, lock2); // 应该返回同一个实例
        assertEquals("test:lock:test-lock", lock1.getName());
    }

    @Test
    void testTryLockSuccess() {
        // 模拟成功获取锁
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(null); // null 表示成功获取锁

        DistributedLock lock = lockManager.getLock("test-lock");
        boolean acquired = lock.tryLock();

        assertTrue(acquired);
        assertEquals(1, lock.getHoldCount());
    }

    @Test
    void testTryLockFailed() {
        // 模拟获取锁失败
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(5000L); // 返回剩余时间表示获取失败

        DistributedLock lock = lockManager.getLock("test-lock");
        boolean acquired = lock.tryLock();

        assertFalse(acquired);
        assertEquals(0, lock.getHoldCount());
    }

    @Test
    void testReentrantLock() {
        // 模拟可重入锁
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(null); // 成功获取锁

        DistributedLock lock = lockManager.getLock("test-lock");
        
        // 第一次获取锁
        assertTrue(lock.tryLock());
        assertEquals(1, lock.getHoldCount());
        
        // 第二次获取锁（重入）
        assertTrue(lock.tryLock());
        assertEquals(2, lock.getHoldCount());
    }

    @Test
    void testUnlock() {
        // 模拟获取锁和释放锁
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(null) // 获取锁成功
                .thenReturn(1L);  // 释放锁成功

        DistributedLock lock = lockManager.getLock("test-lock");
        
        // 获取锁
        assertTrue(lock.tryLock());
        assertEquals(1, lock.getHoldCount());
        
        // 释放锁
        assertTrue(lock.unlock());
        assertEquals(0, lock.getHoldCount());
    }

    @Test
    void testLockName() {
        DistributedLock lock = lockManager.getLock("my-business-lock");
        assertEquals("test:lock:my-business-lock", lock.getName());
    }

    @Test
    void testGetRemainingTimeToLive() {
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(15000L);

        DistributedLock lock = lockManager.getLock("test-lock");
        long ttl = lock.getRemainingTimeToLive();

        assertEquals(15000L, ttl);
    }

    @Test
    void testIsLocked() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        DistributedLock lock = lockManager.getLock("test-lock");
        assertTrue(lock.isLocked());
    }

    @Test
    void testForceUnlock() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
                .thenReturn(1L); // 强制释放成功

        DistributedLock lock = lockManager.getLock("test-lock");
        assertTrue(lock.forceUnlock());
    }
}