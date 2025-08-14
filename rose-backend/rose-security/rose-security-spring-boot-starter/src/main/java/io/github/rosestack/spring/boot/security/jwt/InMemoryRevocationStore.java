package io.github.rosestack.spring.boot.security.jwt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存撤销黑名单
 */
public class InMemoryRevocationStore implements TokenRevocationStore {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @Override
    public void revoke(String accessToken) {
        blacklist.add(accessToken);
    }

    @Override
    public boolean isRevoked(String accessToken) {
        return blacklist.contains(accessToken);
    }
}
