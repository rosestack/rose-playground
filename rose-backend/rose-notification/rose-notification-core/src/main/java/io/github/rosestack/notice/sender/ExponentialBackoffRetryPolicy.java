package io.github.rosestack.notice.sender;

import io.github.rosestack.notice.NoticeRetryableException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 指数退避 + 抖动的重试策略。
 */
public class ExponentialBackoffRetryPolicy implements RetryPolicy {
    private final int maxAttempts;
    private final long initialDelayMillis;
    private final long jitterBoundMillis;

    public ExponentialBackoffRetryPolicy(int maxAttempts, long initialDelayMillis, long jitterBoundMillis) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialDelayMillis = Math.max(0L, initialDelayMillis);
        this.jitterBoundMillis = Math.max(0L, jitterBoundMillis);
    }

    @Override
    public boolean shouldRetry(int attempt, Throwable ex) {
        return attempt < maxAttempts && ex instanceof NoticeRetryableException;
    }

    @Override
    public long nextDelayMillis(int attempt) {
        long base = (long) (initialDelayMillis * Math.pow(2, Math.max(0, attempt - 1)));
        long jitter = jitterBoundMillis == 0 ? 0 : ThreadLocalRandom.current().nextLong(jitterBoundMillis);
        return base + jitter;
    }
}
