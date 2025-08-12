package io.github.rosestack.notice.support;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.spi.BlacklistChecker;

/** 简单内存黑名单实现。 */
public class NoopBlacklistChecker implements BlacklistChecker {

    public void add(String target) {}

    public void remove(String target) {}

    @Override
    public boolean isBlacklisted(SendRequest request) {
        return false;
    }
}
