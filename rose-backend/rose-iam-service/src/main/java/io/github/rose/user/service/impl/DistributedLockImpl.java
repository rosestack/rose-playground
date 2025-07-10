package io.github.rose.user.service.impl;

import io.github.rose.user.service.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;

@Service
public class DistributedLockImpl implements DistributedLock {
    private static final Logger log = LoggerFactory.getLogger(DistributedLockImpl.class);
    private final RedissonClient redissonClient;
    public DistributedLockImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    @Override
    public void lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
    }
    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
