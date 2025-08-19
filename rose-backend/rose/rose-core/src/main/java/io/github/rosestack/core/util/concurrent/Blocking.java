package io.github.rosestack.core.util.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

/**
 * A factory class for methods that wrap functional interfaces like {@link Supplier} in a
 * "blocking" ({@link ManagedBlocker}) equivalent, which can be used with the
 * {@link ForkJoinPool}.
 *
 * @author Lukas Eder
 */
public final class Blocking {
    private Blocking() {}

    public static Runnable runnable(Runnable runnable) {
        return () -> supplier(() -> {
                    runnable.run();
                    return null;
                })
                .get();
    }

    public static <T, U> BiConsumer<T, U> biConsumer(BiConsumer<? super T, ? super U> biConsumer) {
        return (t, u) -> runnable(() -> biConsumer.accept(t, u)).run();
    }

    public static <T, U, R> BiFunction<T, U, R> biFunction(BiFunction<? super T, ? super U, ? extends R> biFunction) {
        return (t, u) -> supplier(() -> biFunction.apply(t, u)).get();
    }

    public static <T, U> BiPredicate<T, U> biPredicate(BiPredicate<? super T, ? super U> biPredicate) {
        return (t, u) -> supplier(() -> biPredicate.test(t, u)).get();
    }

    public static <T> Consumer<T> consumer(Consumer<? super T> consumer) {
        return t -> runnable(() -> consumer.accept(t)).run();
    }

    public static <T, R> Function<T, R> function(Function<? super T, ? extends R> function) {
        return t -> supplier(() -> function.apply(t)).get();
    }

    public static <T> Predicate<T> predicate(Predicate<? super T> predicate) {
        return t -> supplier(() -> predicate.test(t)).get();
    }

    public static <T> Supplier<T> supplier(Supplier<? extends T> supplier) {
        return new BlockingSupplier<>(supplier);
    }

    static class BlockingSupplier<T> implements Supplier<T> {
        private static final Object NULL_MARKER = new Object();
        private final Supplier<? extends T> supplier;
        private final AtomicReference<Object> resultRef = new AtomicReference<>(NULL_MARKER);

        BlockingSupplier(Supplier<? extends T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            try {
                ForkJoinPool.managedBlock(new TaskBlocker());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return (T) resultRef.get();
        }

        private class TaskBlocker implements ForkJoinPool.ManagedBlocker {
            @Override
            public boolean block() {
                T result = supplier.get();
                resultRef.set(result);
                return true;
            }

            @Override
            public boolean isReleasable() {
                return resultRef.get() != NULL_MARKER;
            }
        }
    }
}
