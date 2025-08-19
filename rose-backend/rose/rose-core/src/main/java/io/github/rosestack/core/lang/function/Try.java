package io.github.rosestack.core.lang.function;

import io.github.rosestack.core.lang.function.checked.*;
import io.github.rosestack.core.util.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.*;

/**
 * 简化的异常处理容器 提供比 Vavr Try 更简单、更易用的 API
 *
 * @param <T> 成功时的值类型
 * @author rose
 */
@Slf4j
public final class Try<T> {
	private final T value;
	private final Throwable cause;
	private final boolean isSuccess;

	private Try(T value, Throwable cause, boolean isSuccess) {
		this.value = value;
		this.cause = cause;
		this.isSuccess = isSuccess;
	}

	/**
	 * 检查是否成功
	 */
	public boolean isSuccess() {
		return isSuccess;
	}

	/**
	 * 检查是否失败
	 */
	public boolean isFailure() {
		return !isSuccess;
	}

	/**
	 * 获取成功值，失败时抛出异常
	 */
	public T get() {
		if (isSuccess) {
			return value;
		} else {
			throw new RuntimeException(cause);
		}
	}

	/**
	 * 获取成功值，失败时返回默认值
	 */
	public T getOrElse(T defaultValue) {
		return isSuccess ? value : defaultValue;
	}

	/**
	 * 获取成功值，失败时使用 Supplier 提供默认值
	 */
	public T getOrElseGet(Supplier<T> supplier) {
		return isSuccess ? value : supplier.get();
	}

	/**
	 * 获取错误信息
	 */
	public Throwable getCause() {
		if (isSuccess) {
			throw new IllegalStateException("Try is successful, no error available");
		}
		return cause;
	}

	/**
	 * 成功时执行操作
	 */
	public Try<T> onSuccess(Consumer<T> consumer) {
		if (isSuccess) {
			consumer.accept(value);
		}
		return this;
	}

	/**
	 * 失败时执行操作
	 */
	public Try<T> onFailure(Consumer<Throwable> consumer) {
		if (isFailure()) {
			consumer.accept(cause);
		}
		return this;
	}

	/**
	 * 转换成功值（可能抛出异常）
	 */
	public <R> Try<R> map(CheckedFunction<T, R> mapper) {
		if (isSuccess) {
			try {
				return success(mapper.apply(value));
			} catch (Throwable e) {
				return failure(e);
			}
		} else {
			return failure(cause);
		}
	}

	/**
	 * 扁平化转换
	 */
	public <R> Try<R> flatMap(Function<T, Try<R>> mapper) {
		if (isSuccess) {
			try {
				return mapper.apply(value);
			} catch (Throwable e) {
				return failure(e);
			}
		} else {
			return failure(cause);
		}
	}

	/**
	 * 恢复失败（可能抛出异常）
	 */
	public Try<T> recover(CheckedFunction<Throwable, T> recovery) {
		if (isSuccess) {
			return this;
		} else {
			try {
				return success(recovery.apply(cause));
			} catch (Throwable e) {
				return failure(e);
			}
		}
	}

	/**
	 * 转换为 Option
	 */
	public Optional<T> toOptional() {
		return isSuccess ? Optional.of(value) : Optional.empty();
	}

	public Option<T> toOption() {
		return isSuccess ? Option.some(value) : Option.none();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Try<?> other = (Try<?>) obj;
		if (isSuccess != other.isSuccess) return false;
		if (isSuccess) {
			return Objects.equals(value, other.value);
		} else {
			return Objects.equals(cause, other.cause);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, cause, isSuccess);
	}

	@Override
	public String toString() {
		return isSuccess ? "Success(" + value + ")" : "Failure(" + cause + ")";
	}

	/**
	 * 创建成功的 Try
	 */
	public static <T> Try<T> success(T value) {
		return new Try<>(value, null, true);
	}

	/**
	 * 创建失败的 Try
	 */
	public static <T> Try<T> failure(Throwable error) {
		return new Try<>(null, Objects.requireNonNull(error), false);
	}

	public static <T, R> Function<T, R> tryApply(
		final CheckedFunction<T, R> trueFunction, final CheckedFunction<Throwable, R> errorHandler) {
		return tryApply(Predicates.alwaysTrue(), trueFunction, null, errorHandler, null);
	}

	public static <T, R> Function<T, R> tryApply(
		final CheckedFunction<T, R> trueFunction,
		final CheckedFunction<Throwable, R> errorHandler,
		final CheckedConsumer<T> finalConsumer) {
		return tryApply(Predicates.alwaysTrue(), trueFunction, null, errorHandler, finalConsumer);
	}

	public static <T, R> Function<T, R> tryApply(
		final CheckedFunction<T, R> trueFunction, final CheckedConsumer<T> finalConsumer) {
		return tryApply(Predicates.alwaysTrue(), trueFunction, null, null, finalConsumer);
	}

	public static <T, R> Function<T, R> tryApply(
		final Predicate<T> condition, final CheckedFunction<T, R> trueFunction) {
		return tryApply(condition, trueFunction, null, null, null);
	}

	public static <T, R> Function<T, R> tryApply(
		final Predicate<T> condition,
		final CheckedFunction<T, R> trueFunction,
		final CheckedFunction<T, R> falseFunction) {
		return tryApply(condition, trueFunction, falseFunction, null, null);
	}

	public static <T, R> Function<T, R> tryApply(
		final boolean condition,
		final CheckedFunction<T, R> trueFunction,
		final CheckedFunction<T, R> falseFunction) {
		return tryApply(Predicates.of(condition), trueFunction, falseFunction, null, null);
	}

	public static <T, R> Function<T, R> tryApply(final boolean condition, final CheckedFunction<T, R> trueFunction) {
		return tryApply(Predicates.of(condition), trueFunction, null, null, null);
	}

	public static <T, R> Function<T, R> tryApply(
		final Predicate<T> condition,
		final CheckedFunction<T, R> trueFunction,
		final CheckedFunction<T, R> falseFunction,
		final CheckedFunction<Throwable, R> errorHandler,
		final CheckedConsumer<T> finalConsumer) {
		Objects.nonNull(condition);
		Objects.nonNull(trueFunction);
		return t -> {
			try {
				if (condition.test(t)) {
					return trueFunction.apply(t);
				} else if (falseFunction != null) {
					return falseFunction.apply(t);
				}
				return null;
			} catch (final Exception e) {
				log.warn("tryApply error", e);
				if (errorHandler != null) {
					return errorHandler.unchecked().apply(e);
				}
				ExceptionUtils.uncheckedThrow(e);
				return null;
			} finally {
				if (finalConsumer != null) {
					finalConsumer.unchecked().accept(t);
				}
			}
		};
	}

	public static <R> Consumer<R> tryAccept(
		final CheckedConsumer<R> trueConsumer, final CheckedFunction<Throwable, R> errorHandler) {
		return tryAccept(Predicates.alwaysTrue(), trueConsumer, null, errorHandler, null);
	}

	public static <R> Consumer<R> tryAccept(final Predicate<R> condition, final CheckedConsumer<R> trueConsumer) {
		return tryAccept(condition, trueConsumer, null, null, null);
	}

	public static <R> Consumer<R> tryAccept(
		final Predicate<R> condition,
		final CheckedConsumer<R> trueConsumer,
		final CheckedConsumer<R> falseConsumer) {
		return tryAccept(condition, trueConsumer, falseConsumer, null, null);
	}

	public static <R> Consumer<R> tryAccept(
		final Predicate<R> condition,
		final CheckedConsumer<R> trueConsumer,
		final CheckedConsumer<R> falseConsumer,
		final CheckedFunction<Throwable, R> errorHandler) {
		return tryAccept(condition, trueConsumer, falseConsumer, errorHandler, null);
	}

	public static <R> Consumer<R> tryAccept(final boolean condition, final CheckedConsumer<R> trueConsumer) {
		return tryAccept(Predicates.of(condition), trueConsumer, null, null, null);
	}

	public static <R> Consumer<R> tryAccept(
		final boolean condition, final CheckedConsumer<R> trueConsumer, final CheckedConsumer<R> falseConsumer) {
		return tryAccept(Predicates.of(condition), trueConsumer, falseConsumer, null, null);
	}

	public static <R> Consumer<R> tryAccept(
		final boolean condition,
		final CheckedConsumer<R> trueConsumer,
		final CheckedConsumer<R> falseConsumer,
		final CheckedFunction<Throwable, R> errorHandler) {
		return tryAccept(Predicates.of(condition), trueConsumer, falseConsumer, errorHandler, null);
	}

	public static <R> Consumer<R> tryAccept(
		final Predicate<R> condition,
		final CheckedConsumer<R> trueConsumer,
		final CheckedConsumer<R> falseConsumer,
		final CheckedFunction<Throwable, R> errorHandler,
		final CheckedConsumer<R> finalConsumer) {
		Objects.nonNull(condition);
		Objects.nonNull(trueConsumer);
		return t -> {
			try {
				if (condition.test(t)) {
					trueConsumer.accept(t);
				} else if (falseConsumer != null) {
					falseConsumer.accept(t);
				}
			} catch (final Exception e) {
				log.warn("tryAccept error", e);
				if (errorHandler != null) {
					errorHandler.unchecked().apply(e);
				}
				ExceptionUtils.uncheckedThrow(e);
			} finally {
				if (finalConsumer != null) {
					finalConsumer.unchecked().accept(t);
				}
			}
		};
	}

	public static <R> Supplier<R> tryGet(final boolean condition, final CheckedSupplier<R> trueSupplier) {
		return tryGet(condition, trueSupplier, null, null);
	}

	public static <R> Supplier<R> tryGet(
		final boolean condition, final CheckedSupplier<R> trueSupplier, final CheckedSupplier<R> falseSupplier) {
		return tryGet(condition, trueSupplier, falseSupplier, null);
	}

	public static <R> Supplier<R> tryGet(
		final boolean condition,
		final CheckedSupplier<R> trueSupplier,
		final CheckedSupplier<R> falseSupplier,
		final CheckedFunction<Throwable, R> errorHandler) {
		return tryGet(condition, trueSupplier, falseSupplier, errorHandler, null);
	}

	public static <R> Supplier<R> tryGet(
		final CheckedSupplier<R> trueSupplier, final CheckedFunction<Throwable, R> errorHandler) {
		return tryGet(true, trueSupplier, null, errorHandler, null);
	}

	public static <R> Supplier<R> tryGet(
		final boolean condition,
		final CheckedSupplier<R> trueSupplier,
		final CheckedSupplier<R> falseSupplier,
		final CheckedFunction<Throwable, R> errorHandler,
		final CheckedConsumer<R> finalConsumer) {
		Objects.nonNull(trueSupplier);

		return () -> {
			try {
				if (condition) {
					return trueSupplier.get();
				} else if (falseSupplier != null) {
					return falseSupplier.get();
				}
				return null;
			} catch (final Throwable e) {
				log.warn("tryGet error", e);
				if (errorHandler != null) {
					return errorHandler.unchecked().apply(e);
				}
				ExceptionUtils.uncheckedThrow(e);
				return null;
			} finally {
				if (finalConsumer != null) {
					finalConsumer.unchecked().accept(null);
				}
			}
		};
	}

	public static String throwIfBlank(final String value) throws Throwable {
		throwIf(StringUtils.isBlank(value), () -> new IllegalArgumentException("Value cannot be empty or blank"));
		return value;
	}

	public static <T> T throwIfNull(final T value, final CheckedSupplier<Throwable> handler) throws Throwable {
		throwIf(value == null, handler);
		return value;
	}

	public static void throwIf(final boolean condition, final CheckedSupplier<? extends Throwable> throwable)
		throws Throwable {
		if (condition) {
			throw throwable.get();
		}
	}

	public static <T> Try<T> ofSupplier(Supplier<T> supplier) {
		return ofCheckedSupplier(CheckedSupplier.from(supplier));
	}

	public static <T> Try<T> ofCheckedSupplier(CheckedSupplier<T> supplier) {
		Objects.requireNonNull(supplier, "supplier cannot be null");
		try {
			return success(supplier.get());
		} catch (Throwable e) {
			return failure(e);
		}
	}

	public static <T, R> Try<R> ofFunction(T input, Function<T, R> function) {
		return ofCheckedFunction(input, CheckedFunction.from(function));
	}

	/**
	 * 从 CheckedFunction 创建 Try
	 */
	public static <T, R> Try<R> ofCheckedFunction(T input, CheckedFunction<T, R> function) {
		Objects.requireNonNull(function, "function cannot be null");
		try {
			return success(function.apply(input));
		} catch (Throwable e) {
			return failure(e);
		}
	}

	public static <T, U, R> Try<R> ofBiFunction(T t, U u, BiFunction<T, U, R> function) {
		return ofCheckedBiFunction(t, u, CheckedBiFunction.from(function));
	}

	/**
	 * 从 CheckedBiFunction 创建 Try
	 */
	public static <T, U, R> Try<R> ofCheckedBiFunction(T t, U u, CheckedBiFunction<T, U, R> function) {
		Objects.requireNonNull(function, "function cannot be null");
		try {
			return success(function.apply(t, u));
		} catch (Throwable e) {
			return failure(e);
		}
	}

	public static <T> Try<Void> ofConsumer(T input, Consumer<T> consumer) {
		return ofCheckedConsumer(input, CheckedConsumer.from(consumer));
	}

	/**
	 * 从 CheckedConsumer 创建 Try<Void>
	 */
	public static <T> Try<Void> ofCheckedConsumer(T input, CheckedConsumer<T> consumer) {
		Objects.requireNonNull(consumer, "consumer cannot be null");
		try {
			consumer.accept(input);
			return success(null);
		} catch (Throwable e) {
			return failure(e);
		}
	}

	public static <T, U> Try<Void> ofBiConsumer(T t, U u, BiConsumer<T, U> consumer) {
		return ofCheckedBiConsumer(t, u, CheckedBiConsumer.from(consumer));
	}

	/**
	 * 从 CheckedBiConsumer 创建 Try<Void>
	 */
	public static <T, U> Try<Void> ofCheckedBiConsumer(T t, U u, CheckedBiConsumer<T, U> consumer) {
		Objects.requireNonNull(consumer, "consumer cannot be null");
		try {
			consumer.accept(t, u);
			return success(null);
		} catch (Throwable e) {
			return failure(e);
		}
	}

	/**
	 * 从 CheckedRunnable 创建 Try<Void>
	 */
	public static Try<Void> ofRunnable(CheckedRunnable runnable) {
		Objects.requireNonNull(runnable, "runnable cannot be null");
		try {
			runnable.run();
			return success(null);
		} catch (Throwable e) {
			return failure(e);
		}
	}

	public static <T> Try<T> ofCallable(Callable<T> callable) {
		Objects.requireNonNull(callable, "callable cannot be null");
		return ofCheckedSupplier(callable::call);
	}

	/**
	 * 从 CheckedPredicate 创建 Try
	 */
	public static <T> Try<Boolean> ofPredicate(T input, CheckedPredicate<T> predicate) {
		Objects.requireNonNull(predicate, "predicate cannot be null");
		try {
			return success(predicate.test(input));
		} catch (Throwable e) {
			return failure(e);
		}
	}

	/**
	 * 从 CheckedBiPredicate 创建 Try
	 */
	public static <T, U> Try<Boolean> ofBiPredicate(T t, U u, CheckedBiPredicate<T, U> predicate) {
		Objects.requireNonNull(predicate, "predicate cannot be null");
		try {
			return success(predicate.test(t, u));
		} catch (Throwable e) {
			return failure(e);
		}
	}
}
