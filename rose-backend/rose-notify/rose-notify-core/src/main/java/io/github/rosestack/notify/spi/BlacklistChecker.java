package io.github.rosestack.notify.spi;

import io.github.rosestack.notify.SendRequest;

/**
 * 通用黑名单 SPI，可按需实现目标、渠道等黑名单。
 */
public interface BlacklistChecker {
    /**
     * 是否在黑名单中
     */
    boolean isBlacklisted(SendRequest request);
}
