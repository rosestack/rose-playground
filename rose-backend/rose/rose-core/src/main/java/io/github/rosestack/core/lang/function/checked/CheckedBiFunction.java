package io.github.rosestack.core.lang.function.checked;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 受检异常的双参数函数接口 对应 JDK 的 BiFunction<T, U, R>，但可以抛出受检异常
 *
 * @param <T> 第一个输入参数类型
 * @param <U> 第二个输入参数类型
 * @param <R> 返回值类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {

	/**
	 * 从 JDK BiFunction 创建 CheckedBiFunction
	 */
	static <T, U, R> CheckedBiFunction<T, U, R> from(BiFunction<T, U, R> function) {
		Objects.requireNonNull(function);
		return function::apply;
	}

	/**
	 * 应用函数
	 *
	 * @param t 第一个输入参数
	 * @param u 第二个输入参数
	 * @return 函数结果
	 * @throws Exception 可能抛出的异常
	 */
	R apply(T t, U u) throws Exception;

	/**
	 * 函数组合：先应用当前函数，再应用 after 函数
	 */
	default <V> CheckedBiFunction<T, U, V> andThen(CheckedFunction<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (T t, U u) -> after.apply(apply(t, u));
	}

	/**
	 * 函数组合：先应用当前函数，再应用 after 函数
	 */
	default <V> CheckedBiFunction<T, U, V> andThen(BiFunction<? super T, ? super U, ? extends V> after) {
		Objects.requireNonNull(after);
		return (T t, U u) -> after.apply(t, u);
	}

	/**
	 * 柯里化：将双参数函数转换为单参数函数
	 */
	default CheckedFunction<T, CheckedFunction<U, R>> curried() {
		return (T t) -> (U u) -> apply(t, u);
	}

	/**
	 * 部分应用：固定第一个参数
	 */
	default CheckedFunction<U, R> applyFirst(T t) {
		return (U u) -> apply(t, u);
	}

	/**
	 * 部分应用：固定第二个参数
	 */
	default CheckedFunction<T, R> applySecond(U u) {
		return (T t) -> apply(t, u);
	}

	/**
	 * 转换为 JDK BiFunction（异常会被包装为 RuntimeException）
	 */
	default BiFunction<T, U, R> unchecked() {
		return (T t, U u) -> {
			try {
				return apply(t, u);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * 转换为 JDK BiFunction，使用自定义异常处理器 当发生异常时，调用异常处理器并返回 null
	 *
	 * @param handler 异常处理器，接收捕获的异常
	 * @return 标准 BiFunction
	 */
	default BiFunction<T, U, R> unchecked(Consumer<Throwable> handler) {
		Objects.requireNonNull(handler, "handler cannot be null");
		return (T t, U u) -> {
			try {
				return apply(t, u);
			} catch (Exception e) {
				handler.accept(e);
				return null;
			}
		};
	}

	/**
	 * 转换为 JDK BiFunction，使用自定义异常处理器和默认值 当发生异常时，调用异常处理器并返回默认值
	 *
	 * @param handler      异常处理器，接收捕获的异常
	 * @param defaultValue 异常时返回的默认值
	 * @return 标准 BiFunction
	 */
	default BiFunction<T, U, R> unchecked(Consumer<Throwable> handler, R defaultValue) {
		Objects.requireNonNull(handler, "handler cannot be null");
		return (T t, U u) -> {
			try {
				return apply(t, u);
			} catch (Exception e) {
				handler.accept(e);
				return defaultValue;
			}
		};
	}
}
