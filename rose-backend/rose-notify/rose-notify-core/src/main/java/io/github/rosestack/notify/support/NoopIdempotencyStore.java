package io.github.rosestack.notify.support;

import io.github.rosestack.notify.spi.IdempotencyStore;

/**
 * 简单内存幂等存储实现。
 */
public class NoopIdempotencyStore implements IdempotencyStore {

    @Override
    public boolean exists(String requestId) {
        return false;
    }

    @Override
    public void put(String requestId) {}
}
