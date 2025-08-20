package io.github.rosestack.notify.support;

import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notify.spi.BlacklistChecker;

/**
 * 简单内存黑名单实现。
 */
public class NoopBlacklistChecker implements BlacklistChecker {

    public void add(String target) {}

    public void remove(String target) {}

    @Override
    public boolean isBlacklisted(SendRequest request) {
        return false;
    }
}
