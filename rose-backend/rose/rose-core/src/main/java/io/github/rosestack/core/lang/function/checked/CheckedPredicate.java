package io.github.rosestack.core.lang.function.checked;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 可能抛出受检异常的单参数断言接口
 *
 * @param <T> 输入类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedPredicate<T> {

	/**
	 * 从标准 Predicate 创建 CheckedPredicate
	 *
	 * @param predicate 标准断言
	 * @param <T>       输入类型
	 * @return CheckedPredicate
	 */
	static <T> CheckedPredicate<T> from(Predicate<T> predicate) {
		Objects.requireNonNull(predicate, "predicate cannot be null");
		return predicate::test;
	}

	/**
	 * 返回一个断言，用于测试两个参数是否相等
	 *
	 * @param targetRef 要比较相等性的对象引用
	 * @param <T>       要比较相等性的对象类型
	 * @return 断言
	 */
	static <T> CheckedPredicate<T> isEqual(Object targetRef) {
		return (null == targetRef) ? Objects::isNull : object -> targetRef.equals(object);
	}

	/**
	 * 返回一个断言，用于测试参数是否为 null
	 *
	 * @param <T> 输入类型
	 * @return 断言
	 */
	static <T> CheckedPredicate<T> isNull() {
		return Objects::isNull;
	}

	/**
	 * 返回一个断言，用于测试参数是否不为 null
	 *
	 * @param <T> 输入类型
	 * @return 断言
	 */
	static <T> CheckedPredicate<T> nonNull() {
		return Objects::nonNull;
	}

	/**
	 * 返回一个总是返回 true 的断言
	 *
	 * @param <T> 输入类型
	 * @return 断言
	 */
	static <T> CheckedPredicate<T> alwaysTrue() {
		return t -> true;
	}

	/**
	 * 返回一个总是返回 false 的断言
	 *
	 * @param <T> 输入类型
	 * @return 断言
	 */
	static <T> CheckedPredicate<T> alwaysFalse() {
		return t -> false;
	}

	/**
	 * 对给定参数进行断言测试
	 *
	 * @param t 输入参数
	 * @return 如果输入参数匹配断言则返回 true，否则返回 false
	 * @throws Exception 如果发生异常
	 */
	boolean test(T t) throws Exception;

	/**
	 * 转换为标准 Predicate，将受检异常包装为运行时异常
	 *
	 * @return 标准 Predicate
	 */
	default Predicate<T> unchecked() {
		return t -> {
			try {
				return test(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * 转换为标准 Predicate，使用自定义异常处理器 当发生异常时，调用异常处理器并返回 false
	 *
	 * @param handler 异常处理器，接收捕获的异常
	 * @return 标准 Predicate
	 */
	default Predicate<T> unchecked(java.util.function.Consumer<Throwable> handler) {
		Objects.requireNonNull(handler, "handler cannot be null");
		return t -> {
			try {
				return test(t);
			} catch (Exception e) {
				handler.accept(e);
				return false;
			}
		};
	}

	/**
	 * 转换为标准 Predicate，使用自定义异常处理器和默认值 当发生异常时，调用异常处理器并返回默认值
	 *
	 * @param handler      异常处理器，接收捕获的异常
	 * @param defaultValue 异常时返回的默认值
	 * @return 标准 Predicate
	 */
	default Predicate<T> unchecked(java.util.function.Consumer<Throwable> handler, boolean defaultValue) {
		Objects.requireNonNull(handler, "handler cannot be null");
		return t -> {
			try {
				return test(t);
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
	default CheckedPredicate<T> and(CheckedPredicate<? super T> other) {
		Objects.requireNonNull(other, "other cannot be null");
		return t -> test(t) && other.test(t);
	}

	/**
	 * 返回该断言的逻辑非
	 *
	 * @return 逻辑非断言
	 */
	default CheckedPredicate<T> negate() {
		return t -> !test(t);
	}

	/**
	 * 返回一个组合断言，表示该断言与另一个断言的逻辑或
	 *
	 * @param other 要与该断言进行逻辑或操作的断言
	 * @return 组合断言
	 */
	default CheckedPredicate<T> or(CheckedPredicate<? super T> other) {
		Objects.requireNonNull(other, "other cannot be null");
		return t -> test(t) || other.test(t);
	}
}
