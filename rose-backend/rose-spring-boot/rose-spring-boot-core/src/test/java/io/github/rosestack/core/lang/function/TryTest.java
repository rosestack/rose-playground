package io.github.rosestack.core.lang.function;

import io.github.rosestack.core.lang.function.checked.CheckedFunction;
import io.github.rosestack.core.lang.function.checked.CheckedPredicate;
import io.github.rosestack.core.lang.function.checked.CheckedRunnable;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Try 测试类 基于实际代码的全面测试覆盖
 */
class TryTest {

	// ========== 基本构造和状态测试 ==========

	@Test
	void testSuccessCreation() {
		Try<String> result = Try.success("hello");

		assertTrue(result.isSuccess());
		assertFalse(result.isFailure());
		assertEquals("hello", result.get());
		assertThrows(IllegalStateException.class, result::getCause);
	}

	@Test
	void testFailureCreation() {
		RuntimeException exception = new RuntimeException("test error");
		Try<String> result = Try.failure(exception);

		assertFalse(result.isSuccess());
		assertTrue(result.isFailure());
		assertEquals(exception, result.getCause());
		assertThrows(RuntimeException.class, result::get);
	}

	@Test
	void testFailureWithNullThrowable() {
		assertThrows(NullPointerException.class, () -> Try.failure(null));
	}

	// ========== of 方法测试 ==========
	@Test
	void testOfCheckedSupplierWithNull() {
		assertThrows(NullPointerException.class, () -> Try.ofCheckedSupplier(null));
	}

	// ========== ofSupplier (CheckedSupplier) 测试 ==========

	@Test
	void testOfCheckedSupplierSuccess() {
		Try<String> result = Try.ofCheckedSupplier(() -> "checked success");

		assertTrue(result.isSuccess());
		assertEquals("checked success", result.get());
	}

	@Test
	void testOfCheckedSupplierFailure() {
		Try<String> result = Try.ofCheckedSupplier(() -> {
			throw new Exception("checked supplier error");
		});

		assertTrue(result.isFailure());
		assertEquals("checked supplier error", result.getCause().getMessage());
	}

	// ========== ofFunction 测试 ==========

	@Test
	void testOfFunctionSuccess() {
		Try<String> result = Try.ofFunction("hello", String::toUpperCase);

		assertTrue(result.isSuccess());
		assertEquals("HELLO", result.get());
	}

	@Test
	void testOfFunctionFailure() {
		Try<String> result = Try.ofFunction((String) null, String::toUpperCase);

		assertTrue(result.isFailure());
		assertTrue(result.getCause() instanceof NullPointerException);
	}

	@Test
	void testOfFunctionWithNullFunction() {
		assertThrows(NullPointerException.class, () -> Try.ofFunction("input", null));
	}

	// ========== ofBiFunction 测试 ==========

	@Test
	void testOfBiFunctionSuccess() {
		Try<String> result = Try.ofBiFunction("hello", " world", (s1, s2) -> s1 + s2);

		assertTrue(result.isSuccess());
		assertEquals("hello world", result.get());
	}

	@Test
	void testOfBiFunctionFailure() {
		Try<String> result = Try.ofBiFunction("hello", (String) null, (s1, s2) -> s1 + s2.toUpperCase());

		assertTrue(result.isFailure());
		assertTrue(result.getCause() instanceof NullPointerException);
	}

	// ========== ofConsumer 测试 ==========

	@Test
	void testOfConsumerSuccess() {
		AtomicBoolean executed = new AtomicBoolean(false);
		Try<Void> result = Try.ofConsumer("input", input -> executed.set(true));

		assertTrue(result.isSuccess());
		assertTrue(executed.get());
		assertNull(result.get());
	}

	@Test
	void testOfConsumerFailure() {
		Try<Void> result = Try.ofConsumer("input", input -> {
			throw new RuntimeException("consumer error");
		});

		assertTrue(result.isFailure());
		assertEquals("consumer error", result.getCause().getMessage());
	}

	// ========== ofBiConsumer 测试 ==========

	@Test
	void testOfBiConsumerSuccess() {
		AtomicInteger sum = new AtomicInteger(0);
		Try<Void> result = Try.ofBiConsumer(5, 3, (a, b) -> sum.set(a + b));

		assertTrue(result.isSuccess());
		assertEquals(8, sum.get());
		assertNull(result.get());
	}

	@Test
	void testOfBiConsumerFailure() {
		Try<Void> result = Try.ofBiConsumer(1, 2, (a, b) -> {
			throw new RuntimeException("bi-consumer error");
		});

		assertTrue(result.isFailure());
		assertEquals("bi-consumer error", result.getCause().getMessage());
	}

	// ========== ofRunnable 测试 ==========

	@Test
	void testOfRunnableSuccess() {
		AtomicBoolean executed = new AtomicBoolean(false);
		Try<Void> result = Try.ofRunnable(() -> executed.set(true));

		assertTrue(result.isSuccess());
		assertTrue(executed.get());
		assertNull(result.get());
	}

	@Test
	void testOfRunnableFailure() {
		Try<Void> result = Try.ofRunnable(() -> {
			throw new RuntimeException("runnable error");
		});

		assertTrue(result.isFailure());
		assertEquals("runnable error", result.getCause().getMessage());
	}

	@Test
	void testOfRunnableWithJavaRunnable() {
		AtomicBoolean executed = new AtomicBoolean(false);
		Runnable runnable = () -> executed.set(true);
		Try<Void> result = Try.ofRunnable(CheckedRunnable.from(runnable));

		assertTrue(result.isSuccess());
		assertTrue(executed.get());
	}

	// ========== ofCallable 测试 ==========

	@Test
	void testOfCallableSuccess() {
		Callable<String> callable = () -> "callable result";
		Try<String> result = Try.ofCallable(callable);

		assertTrue(result.isSuccess());
		assertEquals("callable result", result.get());
	}

	@Test
	void testOfCallableFailure() {
		Callable<String> callable = () -> {
			throw new Exception("callable error");
		};
		Try<String> result = Try.ofCallable(callable);

		assertTrue(result.isFailure());
		assertEquals("callable error", result.getCause().getMessage());
	}

	// ========== ofPredicate 测试 ==========

	@Test
	void testOfPredicateTrue() {
		Try<Boolean> result = Try.ofPredicate("hello", s -> s.length() > 3);

		assertTrue(result.isSuccess());
		assertTrue(result.get());
	}

	@Test
	void testOfPredicateFalse() {
		Try<Boolean> result = Try.ofPredicate("hi", s -> s.length() > 3);

		assertTrue(result.isSuccess());
		assertFalse(result.get());
	}

	@Test
	void testOfPredicateFailure() {
		Try<Boolean> result = Try.ofPredicate(null, (CheckedPredicate<String>) s -> s.length() > 3);

		assertTrue(result.isFailure());
		assertTrue(result.getCause() instanceof NullPointerException);
	}

	// ========== ofBiPredicate 测试 ==========

	@Test
	void testOfBiPredicateTrue() {
		Try<Boolean> result = Try.ofBiPredicate("hello", "world", (s1, s2) -> s1.length() + s2.length() > 8);

		assertTrue(result.isSuccess());
		assertTrue(result.get());
	}

	@Test
	void testOfBiPredicateFalse() {
		Try<Boolean> result = Try.ofBiPredicate("hi", "bye", (s1, s2) -> s1.length() + s2.length() > 10);

		assertTrue(result.isSuccess());
		assertFalse(result.get());
	}

	// ========== getOrElse 和 getOrElseGet 测试 ==========

	@Test
	void testGetOrElseSuccess() {
		Try<String> result = Try.success("value");
		assertEquals("value", result.getOrElse("default"));
	}

	@Test
	void testGetOrElseFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		assertEquals("default", result.getOrElse("default"));
	}

	@Test
	void testGetOrElseGetSuccess() {
		Try<String> result = Try.success("value");
		assertEquals("value", result.getOrElseGet(() -> "default"));
	}

	@Test
	void testGetOrElseGetFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		assertEquals("default", result.getOrElseGet(() -> "default"));
	}

	// ========== onSuccess 和 onFailure 测试 ==========

	@Test
	void testOnSuccessCallback() {
		AtomicBoolean called = new AtomicBoolean(false);
		Try<String> result = Try.success("value");

		Try<String> returned = result.onSuccess(value -> called.set(true));

		assertTrue(called.get());
		assertSame(result, returned); // 应该返回同一个实例
	}

	@Test
	void testOnSuccessNotCalledOnFailure() {
		AtomicBoolean called = new AtomicBoolean(false);
		Try<String> result = Try.failure(new RuntimeException("error"));

		result.onSuccess(value -> called.set(true));

		assertFalse(called.get());
	}

	@Test
	void testOnFailureCallback() {
		AtomicBoolean called = new AtomicBoolean(false);
		Try<String> result = Try.failure(new RuntimeException("error"));

		Try<String> returned = result.onFailure(throwable -> called.set(true));

		assertTrue(called.get());
		assertSame(result, returned); // 应该返回同一个实例
	}

	@Test
	void testOnFailureNotCalledOnSuccess() {
		AtomicBoolean called = new AtomicBoolean(false);
		Try<String> result = Try.success("value");

		result.onFailure(throwable -> called.set(true));

		assertFalse(called.get());
	}

	// ========== map 测试 ==========

	@Test
	void testMapSuccess() {
		Try<String> result = Try.success("hello");
		Try<String> mapped = result.map(String::toUpperCase);

		assertTrue(mapped.isSuccess());
		assertEquals("HELLO", mapped.get());
	}

	@Test
	void testMapFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Try<String> mapped = result.map(String::toUpperCase);

		assertTrue(mapped.isFailure());
		assertEquals("error", mapped.getCause().getMessage());
	}

	@Test
	void testMapThrowsException() {
		Try<String> result = Try.success("hello");
		Try<String> mapped = result.map(s -> {
			throw new RuntimeException("map error");
		});

		assertTrue(mapped.isFailure());
		assertEquals("map error", mapped.getCause().getMessage());
	}

	@Test
	void testMapWithCheckedFunction() {
		Try<String> result = Try.success("hello");
		Try<String> mapped = result.map((CheckedFunction<String, String>) String::toUpperCase);

		assertTrue(mapped.isSuccess());
		assertEquals("HELLO", mapped.get());
	}

	@Test
	void testMapWithCheckedFunctionThrowsException() {
		Try<String> result = Try.success("hello");
		Try<String> mapped = result.map((CheckedFunction<String, String>) s -> {
			throw new Exception("checked map error");
		});

		assertTrue(mapped.isFailure());
		assertEquals("checked map error", mapped.getCause().getMessage());
	}

	// ========== flatMap 测试 ==========

	@Test
	void testFlatMapSuccess() {
		Try<String> result = Try.success("hello");
		Try<String> flatMapped = result.flatMap(s -> Try.success(s.toUpperCase()));

		assertTrue(flatMapped.isSuccess());
		assertEquals("HELLO", flatMapped.get());
	}

	@Test
	void testFlatMapFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Try<String> flatMapped = result.flatMap(s -> Try.success(s.toUpperCase()));

		assertTrue(flatMapped.isFailure());
		assertEquals("error", flatMapped.getCause().getMessage());
	}

	@Test
	void testFlatMapReturnsFailure() {
		Try<String> result = Try.success("hello");
		Try<String> flatMapped = result.flatMap(s -> Try.failure(new RuntimeException("flatMap error")));

		assertTrue(flatMapped.isFailure());
		assertEquals("flatMap error", flatMapped.getCause().getMessage());
	}

	@Test
	void testFlatMapThrowsException() {
		Try<String> result = Try.success("hello");
		Try<String> flatMapped = result.flatMap(s -> {
			throw new RuntimeException("flatMap exception");
		});

		assertTrue(flatMapped.isFailure());
		assertEquals("flatMap exception", flatMapped.getCause().getMessage());
	}

	// ========== recover 测试 ==========

	@Test
	void testRecoverSuccess() {
		Try<String> result = Try.success("value");
		Try<String> recovered = result.recover((CheckedFunction<Throwable, String>) throwable -> "recovered");

		assertTrue(recovered.isSuccess());
		assertEquals("value", recovered.get()); // 应该保持原值
		assertSame(result, recovered); // 应该返回同一个实例
	}

	@Test
	void testRecoverFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Try<String> recovered = result.recover((CheckedFunction<Throwable, String>) throwable -> "recovered");

		assertTrue(recovered.isSuccess());
		assertEquals("recovered", recovered.get());
	}

	@Test
	void testRecoverThrowsException() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Try<String> recovered = result.recover((CheckedFunction<Throwable, String>) throwable -> {
			throw new RuntimeException("recover error");
		});

		assertTrue(recovered.isFailure());
		assertEquals("recover error", recovered.getCause().getMessage());
	}

	@Test
	void testRecoverWithCheckedFunction() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Try<String> recovered = result.recover((CheckedFunction<Throwable, String>) throwable -> "checked recovered");

		assertTrue(recovered.isSuccess());
		assertEquals("checked recovered", recovered.get());
	}

	@Test
	void testRecoverWithCheckedFunctionThrowsException() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Try<String> recovered = result.recover((CheckedFunction<Throwable, String>) throwable -> {
			throw new Exception("checked recover error");
		});

		assertTrue(recovered.isFailure());
		assertEquals("checked recover error", recovered.getCause().getMessage());
	}

	// ========== 转换方法测试 ==========

	@Test
	void testToOptionalSuccess() {
		Try<String> result = Try.success("value");
		Optional<String> optional = result.toOptional();

		assertTrue(optional.isPresent());
		assertEquals("value", optional.get());
	}

	@Test
	void testToOptionalFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Optional<String> optional = result.toOptional();

		assertFalse(optional.isPresent());
	}

	@Test
	void testToOptionSuccess() {
		Try<String> result = Try.success("value");
		Option<String> option = result.toOption();

		assertTrue(option.isPresent());
		assertEquals("value", option.get());
	}

	@Test
	void testToOptionFailure() {
		Try<String> result = Try.failure(new RuntimeException("error"));
		Option<String> option = result.toOption();

		assertFalse(option.isPresent());
	}
}
