package io.github.rosestack.core.util.concurrent;

import io.github.rosestack.core.lang.function.Try;
import io.github.rosestack.core.lang.function.checked.CheckedConsumer;
import io.github.rosestack.core.lang.function.checked.CheckedSupplier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 0.0.1
 */
public class TryLock {

    private static final int LOCK_TIMEOUT_SECONDS = 3;

    private final ReentrantLock lock = new ReentrantLock(true);

    public boolean tryLock() {
        return Try.tryGet(() -> lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS), e -> false)
                .get();
    }

    /**
     * Acquires the lock if it is not held by another thread within the given waiting time
     * and the current thread has not been {@linkplain Thread#interrupt interrupted}.
     * Then, execute the given supplier.
     *
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @return the result of the supplier
     */
    public <T> T tryLock(final CheckedSupplier<T> supplier) {
        if (tryLock()) {
            try {
                return supplier.get();
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
        return null;
    }

    /**
     * Acquires the lock if it is not held by another thread within the given waiting time
     * and the current thread has not been {@linkplain Thread#interrupt interrupted}.
     * Then, execute the given consumer.
     *
     * @param <T>      the type parameter
     * @param consumer the consumer
     */
    public <T> void tryLock(final CheckedConsumer<T> consumer) {
        if (tryLock()) {
            try {
                consumer.accept(null);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
    }

    /**
     * Attempts to release this lock.
     */
    public void unlock() {
        lock.unlock();
    }
}
