package io.github.rose.user.service;

public interface DistributedLock {
    void lock(String key);
    void unlock(String key);
}
