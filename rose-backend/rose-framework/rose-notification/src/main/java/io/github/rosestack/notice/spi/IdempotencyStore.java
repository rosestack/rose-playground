package io.github.rosestack.notice.spi;

/**
 * 幂等存储 SPI，防止重复发送。
 */
public interface IdempotencyStore {
    /**
     * 检查 requestId 是否已处理，已处理返回 true 表示已处理，否则 false
     */
    boolean exists(String requestId);

    /**
     * 记录本次发送 requestId
     */
    void put(String requestId);
}
