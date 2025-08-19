package io.github.rosestack.core.lang.function.checked;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * 可能抛出受检异常的双参数断言接口
 *
 * @param <T> 第一个参数类型
 * @param <U> 第二个参数类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedBiPredicate<T, U> {

	/**
	 * 从标准 BiPredicate 创建 CheckedBiPredicate
	 *
	 * @param predicate 标准双参数断言
	 * @param <T>       第一个参数类型
	 * @param <U>       第二个参数类型
	 * @return CheckedBiPredicate
	 */
	static <T, U> CheckedBiPredicate<T, U> from(BiPredicate<T, U> predicate) {
		Objects.requireNonNull(predicate, "predicate cannot be null");
		return predicate::test;
	}

	/**
	 * 返回一个总是返回 true 的双参数断言
	 *
	 * @param <T> 第一个参数类型
	 * @param <U> 第二个参数类型
	 * @return 断言
	 */
	static <T, U> CheckedBiPredicate<T, U> alwaysTrue() {
		return (t, u) -> true;
	}

	/**
	 * 返回一个总是返回 false 的双参数断言
	 *
	 * @param <T> 第一个参数类型
	 * @param <U> 第二个参数类型
	 * @return 断言
	 */
	static <T, U> CheckedBiPredicate<T, U> alwaysFalse() {
		return (t, u) -> false;
	}

	/**
	 * 对给定的两个参数进行断言测试
	 *
	 * @param t 第一个输入参数
	 * @param u 第二个输入参数
	 * @return 如果输入参数匹配断言则返回 true，否则返回 false
	 * @throws Exception 如果发生异常
	 */
	boolean test(T t, U u) throws Exception;

	/**
	 * 转换为标准 BiPredicate，将受检异常包装为运行时异常
	 *
	 * @return 标准 BiPredicate
	 */
	default BiPredicate<T, U> unchecked() {
		return (t, u) -> {
			try {
				return test(t, u);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * 转换为标准 BiPredicate，使用自定义异常处理器 当发生异常时，调用异常处理器并返回 false
	 *
	 * @param handler 异常处理器，接收捕获的异常
	 * @return 标准 BiPredicate
	 */
	default BiPredicate<T, U> unchecked(java.util.function.Consumer<Throwable> handler) {
		Objects.requireNonNull(handler, "handler cannot be null");
		return (t, u) -> {
			try {
				return test(t, u);
			} catch (Exception e) {
				handler.accept(e);
				return false;
			}
		};
	}

	/**
	 * 转换为标准 BiPredicate，使用自定义异常处理器和默认值 当发生异常时，调用异常处理器并返回默认值
	 *
	 * @param handler      异常处理器，接收捕获的异常
	 * @param defaultValue 异常时返回的默认值
	 * @return 标准 BiPredicate
	 */
	default BiPredicate<T, U> unchecked(java.util.function.Consumer<Throwable> handler, boolean defaultValue) {
		Objects.requireNonNull(handler, "handler cannot be null");
		return (t, u) -> {
			try {
				return test(t, u);
			} catch (Exception e) {
				handler.accept(e);
				return defaultValue;
			}
		};
	}

	/**
	 * 返回一个组合断言，表示该断言与另一个断言的逻辑与
	 *
	 * @param other 要与该断言进行逻辑与操作的断言
	 * @return 组合断言
	 */
	default CheckedBiPredicate<T, U> and(CheckedBiPredicate<? super T, ? super U> other) {
		Objects.requireNonNull(other, "other cannot be null");
		return (t, u) -> test(t, u) && other.test(t, u);
	}

	/**
	 * 返回该断言的逻辑非
	 *
	 * @return 逻辑非断言
	 */
	default CheckedBiPredicate<T, U> negate() {
		return (t, u) -> !test(t, u);
	}

	/**
	 * 返回一个组合断言，表示该断言与另一个断言的逻辑或
	 *
	 * @param other 要与该断言进行逻辑或操作的断言
	 * @return 组合断言
	 */
	default CheckedBiPredicate<T, U> or(CheckedBiPredicate<? super T, ? super U> other) {
		Objects.requireNonNull(other, "other cannot be null");
		return (t, u) -> test(t, u) || other.test(t, u);
	}
}
