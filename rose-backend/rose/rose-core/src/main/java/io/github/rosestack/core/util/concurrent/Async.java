package io.github.rosestack.core.util.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.*;

public final class Async {

    private Async() {}

    public static <U> CompletionStage<U> supplyAsync(Supplier<U> supplier) {
        return SameExecutorCompletionStage.of(CompletableFuture.supplyAsync(supplier), null);
    }

    public static <U> CompletionStage<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return SameExecutorCompletionStage.of(CompletableFuture.supplyAsync(supplier, executor), executor);
    }

    public static CompletionStage<Void> runAsync(Runnable runnable, Executor executor) {
        return SameExecutorCompletionStage.of(CompletableFuture.runAsync(runnable, executor), executor);
    }

    public static CompletionStage<Void> runAsync(Runnable runnable) {
        return SameExecutorCompletionStage.of(CompletableFuture.runAsync(runnable), null);
    }

    public static final class SameExecutorCompletionStage<T> implements CompletionStage<T> {

        private final CompletionStage<T> delegate;

        private final Executor defaultExecutor;

        SameExecutorCompletionStage(CompletionStage<T> delegate, Executor defaultExecutor) {
            this.delegate = delegate;
            this.defaultExecutor = defaultExecutor;
        }

        static <T> SameExecutorCompletionStage<T> of(CompletionStage<T> delegate, Executor defaultExecutor) {
            return new SameExecutorCompletionStage<>(delegate, defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> fn) {
            return of(delegate.thenApply(fn), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
            if (defaultExecutor == null) {
                return of(delegate.thenApplyAsync(fn), null);
            }
            return of(delegate.thenApplyAsync(fn, defaultExecutor), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
            return of(delegate.thenApplyAsync(fn, executor), executor);
        }

        @Override
        public CompletionStage<Void> thenAccept(Consumer<? super T> action) {
            return of(delegate.thenAccept(action), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action) {
            if (defaultExecutor == null) {
                return of(delegate.thenAcceptAsync(action), null);
            }
            return of(delegate.thenAcceptAsync(action, defaultExecutor), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
            return of(delegate.thenAcceptAsync(action, executor), executor);
        }

        @Override
        public CompletionStage<Void> thenRun(Runnable action) {
            return of(delegate.thenRun(action), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> thenRunAsync(Runnable action) {
            if (defaultExecutor == null) {
                return of(delegate.thenRunAsync(action), null);
            }
            return of(delegate.thenRunAsync(action, defaultExecutor), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
            return of(delegate.thenRunAsync(action, executor), executor);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombine(
                CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
            return of(delegate.thenCombine(other, fn), defaultExecutor);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombineAsync(
                CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
            if (defaultExecutor == null) {
                return of(delegate.thenCombineAsync(other, fn), null);
            }
            return of(delegate.thenCombineAsync(other, fn, defaultExecutor), defaultExecutor);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombineAsync(
                CompletionStage<? extends U> other,
                BiFunction<? super T, ? super U, ? extends V> fn,
                Executor executor) {
            return of(delegate.thenCombineAsync(other, fn, executor), executor);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBoth(
                CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
            return of(delegate.thenAcceptBoth(other, action), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBothAsync(
                CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
            if (defaultExecutor == null) {
                return of(delegate.thenAcceptBothAsync(other, action), null);
            }
            return of(delegate.thenAcceptBothAsync(other, action, defaultExecutor), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBothAsync(
                CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
            return of(delegate.thenAcceptBothAsync(other, action, executor), executor);
        }

        @Override
        public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
            return of(delegate.runAfterBoth(other, action), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
            if (defaultExecutor == null) {
                return of(delegate.runAfterBothAsync(other, action), null);
            }
            return of(delegate.runAfterBothAsync(other, action, defaultExecutor), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
            return of(delegate.runAfterBothAsync(other, action, executor), executor);
        }

        @Override
        public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
            return of(delegate.applyToEither(other, fn), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> applyToEitherAsync(
                CompletionStage<? extends T> other, Function<? super T, U> fn) {
            if (defaultExecutor == null) {
                return of(delegate.applyToEitherAsync(other, fn), null);
            }
            return of(delegate.applyToEitherAsync(other, fn, defaultExecutor), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> applyToEitherAsync(
                CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
            return of(delegate.applyToEitherAsync(other, fn, executor), executor);
        }

        @Override
        public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
            return of(delegate.acceptEither(other, action), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
            if (defaultExecutor == null) {
                return of(delegate.acceptEitherAsync(other, action), null);
            }
            return of(delegate.acceptEitherAsync(other, action, defaultExecutor), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> acceptEitherAsync(
                CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
            return of(delegate.acceptEitherAsync(other, action, executor), executor);
        }

        @Override
        public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
            return of(delegate.runAfterEither(other, action), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
            if (defaultExecutor == null) {
                return of(delegate.runAfterEitherAsync(other, action), null);
            }
            return of(delegate.runAfterEitherAsync(other, action, defaultExecutor), defaultExecutor);
        }

        @Override
        public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
            return of(delegate.runAfterEitherAsync(other, action, executor), executor);
        }

        @Override
        public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
            return of(delegate.thenCompose(fn), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
            if (defaultExecutor == null) {
                return of(delegate.thenComposeAsync(fn), null);
            }
            return of(delegate.thenComposeAsync(fn, defaultExecutor), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> thenComposeAsync(
                Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
            return of(delegate.thenComposeAsync(fn, executor), executor);
        }

        @Override
        public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) {
            return of(delegate.exceptionally(fn), defaultExecutor);
        }

        @Override
        public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
            return of(delegate.whenComplete(action), defaultExecutor);
        }

        @Override
        public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
            if (defaultExecutor == null) {
                return of(delegate.whenCompleteAsync(action), null);
            }
            return of(delegate.whenCompleteAsync(action, defaultExecutor), defaultExecutor);
        }

        @Override
        public CompletionStage<T> whenCompleteAsync(
                BiConsumer<? super T, ? super Throwable> action, Executor executor) {
            return of(delegate.whenCompleteAsync(action, executor), executor);
        }

        @Override
        public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
            return of(delegate.handle(fn), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
            if (defaultExecutor == null) {
                return of(delegate.handleAsync(fn), null);
            }
            return of(delegate.handleAsync(fn, defaultExecutor), defaultExecutor);
        }

        @Override
        public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
            return of(delegate.handleAsync(fn, executor), executor);
        }

        @Override
        public CompletableFuture<T> toCompletableFuture() {
            return delegate.toCompletableFuture();
        }
    }
}
