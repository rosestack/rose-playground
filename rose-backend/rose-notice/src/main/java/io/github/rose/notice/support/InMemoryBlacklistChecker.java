package io.github.rose.notice.support;

import io.github.rose.notice.SendRequest;
import io.github.rose.notice.spi.BlacklistChecker;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 简单内存黑名单实现。
 */
public class InMemoryBlacklistChecker implements BlacklistChecker {
    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    public void add(String target) {
        blacklist.add(target);
    }

    public void remove(String target) {
        blacklist.remove(target);
    }

    @Override
    public boolean isBlacklisted(SendRequest request) {
        return blacklist.contains(request.getTarget());
    }
}
