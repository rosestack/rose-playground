package io.github.rosestack.notice.sender;

/**
 * 重试策略接口，用于控制是否重试与各次重试间隔。
 */
public interface RetryPolicy {
    /** 是否应当重试（attempt 从1开始计数） */
    boolean shouldRetry(int attempt, Throwable ex);

    /** 下一次重试延迟毫秒（attempt 从1开始计数） */
    long nextDelayMillis(int attempt);
}


