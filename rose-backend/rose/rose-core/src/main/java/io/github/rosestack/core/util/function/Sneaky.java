package io.github.rosestack.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 非检查异常工具类
 *
 * <p>提供将检查异常转换为非检查异常的功能
 *
 * @author rose
 * @since 1.0.0
 */
public final class Sneaky {

	private Sneaky() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * 将检查异常转换为非检查异常
	 *
	 * <p>这个方法使用泛型欺骗编译器，将检查异常作为非检查异常抛出。
	 * 对于特定类型的异常，会进行特殊处理：
	 * <ul>
	 *   <li>RuntimeException - 直接抛出</li>
	 *   <li>IOException - 包装为 UncheckedIOException</li>
	 *   <li>InterruptedException - 设置中断标志并包装为 RuntimeException</li>
	 *   <li>其他异常 - 使用泛型技巧直接抛出</li>
	 * </ul>
	 *
	 * @param throwable 要转换的异常
	 * @param <E>       异常类型
	 * @throws E 转换后的异常
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		}

		if (throwable instanceof IOException) {
			throw new UncheckedIOException((IOException) throwable);
		}

		if (throwable instanceof InterruptedException) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(throwable);
		}

		throw (E) throwable;
	}
}
