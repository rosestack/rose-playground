package io.github.rose.core.lang.function.core;

import io.github.rose.core.lang.function.checked.CheckedFunction;
import io.github.rose.core.lang.function.checked.CheckedRunnable;
import io.github.rose.core.lang.function.checked.CheckedSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Try 测试类
 * 测试函数式异常处理容器的功能
 */
class TryTest {

    @Test
    void testOfFunctionSuccess() {
        // 测试成功的函数执行
        Try<String> result = Try.ofFunction("hello", (CheckedFunction<String, String>) String::toUpperCase);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("HELLO", result.get());

        // 成功时getCause应该抛出异常
        assertThrows(IllegalStateException.class, result::getCause);
    }

    @Test
    void testOfFunctionFailure() {
        // 测试失败的函数执行
        Try<String> result = Try.ofFunction(null, (CheckedFunction<String, String>) String::toUpperCase);

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertNotNull(result.getCause());
        assertTrue(result.getCause() instanceof NullPointerException);

        // get()应该抛出异常
        assertThrows(RuntimeException.class, result::get);
    }

    @Test
    void testOfSupplierSuccess() {
        // 测试成功的Supplier执行
        Try<String> result = Try.of(() -> "success");

        assertTrue(result.isSuccess());
        assertEquals("success", result.get());
    }

    @Test
    void testOfSupplierFailure() {
        // 测试失败的Supplier执行
        Try<String> result = Try.of(() -> {
            throw new RuntimeException("测试异常");
        });

        assertTrue(result.isFailure());
        assertEquals("测试异常", result.getCause().getMessage());
    }

    @Test
    void testOfRunnableSuccess() {
        // 测试成功的Runnable执行
        AtomicBoolean executed = new AtomicBoolean(false);
        Try<Void> result = Try.ofRunnable((CheckedRunnable) () -> executed.set(true));

        assertTrue(result.isSuccess());
        assertTrue(executed.get());
        assertNull(result.get());
    }

    @Test
    void testOfRunnableFailure() {
        // 测试失败的Runnable执行
        Try<Void> result = Try.ofRunnable((CheckedRunnable) () -> {
            throw new RuntimeException("Runnable异常");
        });

        assertTrue(result.isFailure());
        assertEquals("Runnable异常", result.getCause().getMessage());
    }

    @Test
    void testMap() {
        // 测试map操作
        Try<String> original = Try.of(() -> "hello");
        Try<String> mapped = original.map((Function<String, String>) String::toUpperCase);

        assertTrue(mapped.isSuccess());
        assertEquals("HELLO", mapped.get());

        // 测试失败情况下的map
        Try<String> failed = Try.ofFunction(null, (CheckedFunction<String, String>) String::toUpperCase);
        Try<String> mappedFailed = failed.map((Function<String, String>) String::toLowerCase);

        assertTrue(mappedFailed.isFailure());
        assertEquals(failed.getCause().getClass(), mappedFailed.getCause().getClass());
    }

    @Test
    void testFlatMap() {
        // 测试flatMap操作
        Try<String> original = Try.of(() -> "hello");
        Try<String> flatMapped = original.flatMap(s -> Try.ofFunction(s, (CheckedFunction<String, String>) String::toUpperCase));

        assertTrue(flatMapped.isSuccess());
        assertEquals("HELLO", flatMapped.get());

        // 测试flatMap返回失败的情况
        Try<String> flatMappedFailed = original.flatMap(s -> Try.of(() -> {
            throw new RuntimeException("flatMap异常");
        }));

        assertTrue(flatMappedFailed.isFailure());
        assertEquals("flatMap异常", flatMappedFailed.getCause().getMessage());
    }

    @Test
    void testRecover() {
        // 测试recover操作
        Try<String> failed = Try.ofFunction(null, (CheckedFunction<String, String>) String::toUpperCase);
        Try<String> recovered = failed.recover((Function<Throwable, String>) throwable -> "recovered");

        assertTrue(recovered.isSuccess());
        assertEquals("recovered", recovered.get());

        // 测试成功情况下的recover
        Try<String> success = Try.of(() -> "hello");
        Try<String> notRecovered = success.recover((Function<Throwable, String>) throwable -> "should not be called");

        assertTrue(notRecovered.isSuccess());
        assertEquals("hello", notRecovered.get());
    }

    @Test
    void testGetOrElse() {
        // 测试getOrElse操作
        Try<String> failed = Try.ofFunction(null, (CheckedFunction<String, String>) String::toUpperCase);
        String result = failed.getOrElse("default");
        assertEquals("default", result);

        // 测试成功情况下的getOrElse
        Try<String> success = Try.of(() -> "hello");
        String successResult = success.getOrElse("default");
        assertEquals("hello", successResult);
    }

    @Test
    void testGetOrElseGet() {
        // 测试getOrElseGet操作
        Try<String> failed = Try.ofFunction(null, (CheckedFunction<String, String>) String::toUpperCase);
        String result = failed.getOrElseGet(() -> "lazy default");
        assertEquals("lazy default", result);

        // 验证Supplier只在失败时被调用
        AtomicBoolean supplierCalled = new AtomicBoolean(false);
        Try<String> success = Try.of(() -> "hello");
        String successResult = success.getOrElseGet(() -> {
            supplierCalled.set(true);
            return "should not be called";
        });

        assertEquals("hello", successResult);
        assertFalse(supplierCalled.get());
    }

    @Test
    void testToOptional() {
        // 测试转换为Optional
        Try<String> success = Try.of(() -> "hello");
        java.util.Optional<String> optional = success.toOptional();

        assertTrue(optional.isPresent());
        assertEquals("hello", optional.get());

        // 测试失败情况
        Try<String> failed = Try.ofFunction(null, (CheckedFunction<String, String>) String::toUpperCase);
        java.util.Optional<String> emptyOptional = failed.toOptional();

        assertFalse(emptyOptional.isPresent());
    }

    @Test
    void testChaining() {
        // 测试链式操作
        Try<String> result = Try.of(() -> "  hello world  ")
            .map((Function<String, String>) String::trim)
            .map((Function<String, String>) String::toUpperCase)
            .map((Function<String, String>) s -> s.replace("WORLD", "JAVA"));

        assertTrue(result.isSuccess());
        assertEquals("HELLO JAVA", result.get());
    }

    @Test
    void testComplexChaining() {
        // 测试复杂的链式操作
        Try<Integer> result = Try.of(() -> "123")
            .map((Function<String, String>) String::trim)
            .flatMap(s -> Try.ofFunction(s, (CheckedFunction<String, Integer>) Integer::parseInt))
            .map((Function<Integer, Integer>) i -> i * 2);

        assertTrue(result.isSuccess());
        assertEquals(246, result.get().intValue());
    }

    @Test
    void testFailureInChain() {
        // 测试链式操作中的失败传播
        Try<String> result = Try.of(() -> "hello")
            .map((Function<String, String>) String::toUpperCase)
            .flatMap(s -> Try.ofFunction(null, (CheckedFunction<String, String>) String::toLowerCase)) // 这里会失败
            .map((Function<String, String>) s -> s + " world"); // 这个不会执行

        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof NullPointerException);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testVariousScenarios(String input, boolean shouldSucceed, String expectedResult) {
        Try<String> result = Try.ofFunction(input, (CheckedFunction<String, String>) s -> s == null ? null : s.toUpperCase());

        assertEquals(shouldSucceed, result.isSuccess());
        if (shouldSucceed) {
            assertEquals(expectedResult, result.get());
        }
    }

    static Stream<Arguments> provideTestCases() {
        return Stream.of(
            Arguments.of("hello", true, "HELLO"),
            Arguments.of("world", true, "WORLD"),
            Arguments.of("", true, ""),
            Arguments.of(null, true, null) // 这个不会抛出异常，因为我们的函数处理了null
        );
    }

    @Test
    void testPerformance() {
        // 性能测试 - 确保Try包装不会有显著的性能开销
        int iterations = 10000;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            Try<String> result = Try.ofFunction("test" + i, (CheckedFunction<String, String>) String::toUpperCase);
            result.map((Function<String, String>) s -> s + "_processed");
        }
        long endTime = System.currentTimeMillis();

        // 10000次操作应该在合理时间内完成
        assertTrue(endTime - startTime < 1000, "Try操作耗时过长: " + (endTime - startTime) + "ms");
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // 线程安全测试
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    final int operationId = j;
                    Try<String> result = Try.ofFunction("test" + threadId + "_" + j, (CheckedFunction<String, String>) s -> {
                        if (operationId % 10 == 0) {
                            throw new RuntimeException("测试异常");
                        }
                        return s.toUpperCase();
                    });

                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证结果
        assertEquals(threadCount * operationsPerThread, successCount.get() + failureCount.get());
        assertEquals(threadCount * 10, failureCount.get()); // 每个线程有10个失败操作
    }

    @Test
    void testNestedTry() {
        // 测试嵌套Try操作
        Try<Try<String>> nested = Try.of(() -> Try.ofFunction("hello", (CheckedFunction<String, String>) String::toUpperCase));

        assertTrue(nested.isSuccess());
        Try<String> inner = nested.get();
        assertTrue(inner.isSuccess());
        assertEquals("HELLO", inner.get());

        // 扁平化嵌套Try
        Try<String> flattened = nested.flatMap(Function.identity());
        assertTrue(flattened.isSuccess());
        assertEquals("HELLO", flattened.get());
    }

    @Test
    void testOnSuccessAndOnFailure() {
        // 测试onSuccess和onFailure回调
        AtomicBoolean successCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);

        Try<String> success = Try.of(() -> "success");
        success.onSuccess(value -> successCalled.set(true))
               .onFailure(throwable -> failureCalled.set(true));

        assertTrue(successCalled.get());
        assertFalse(failureCalled.get());

        // 重置
        successCalled.set(false);
        failureCalled.set(false);

        Try<String> failure = Try.of(() -> {
            throw new RuntimeException("failure");
        });
        failure.onSuccess(value -> successCalled.set(true))
               .onFailure(throwable -> failureCalled.set(true));

        assertFalse(successCalled.get());
        assertTrue(failureCalled.get());
    }
}
