package io.github.rosestack.notice.support;

import io.github.rosestack.notice.spi.IdempotencyStore;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单内存幂等存储实现。
 */
public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Set<String> store = ConcurrentHashMap.newKeySet();

    @Override
    public boolean exists(String requestId) {
        return store.contains(requestId);
    }

    @Override
    public void put(String requestId) {
        store.add(requestId);
    }
}
