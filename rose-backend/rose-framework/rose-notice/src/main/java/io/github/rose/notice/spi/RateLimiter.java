package io.github.rose.notice.spi;

import io.github.rose.notice.SendRequest;

/**
 * 通用限流器 SPI，可按需实现全局、渠道、目标等多维度限流。
 */
public interface RateLimiter {
    /**
     * 是否允许本次发送
     */
    boolean allow(SendRequest request);

    /**
     * 记录一次发送（可选）
     */
    default void record(SendRequest request) {}
}
