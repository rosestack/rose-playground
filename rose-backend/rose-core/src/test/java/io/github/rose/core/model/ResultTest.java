package io.github.rose.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 测试类
 * 测试API响应结果包装类的所有功能
 */
class ResultTest {

    // ========== 基本构造和状态测试 ==========

    @Test
    void testDefaultConstructor() {
        Result<String> result = new Result<>();
        assertNull(result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFullConstructor() {
        String data = "test data";
        Result<String> result = new Result<>(200, "success", data);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    void testSuccessWithoutData() {
        Result<String> result = Result.success();

        assertTrue(result.isSuccess());
        assertFalse(result.isFail());
        assertEquals(Result.SUCCESS, result.getCode());
        assertEquals(Result.SERVER_SUCCESS, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testSuccessWithData() {
        String data = "test data";
        Result<String> result = Result.success(data);

        assertTrue(result.isSuccess());
        assertFalse(result.isFail());
        assertEquals(Result.SUCCESS, result.getCode());
        assertEquals(Result.SERVER_SUCCESS, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    void testFailureDefault() {
        Result<String> result = Result.failure();

        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(Result.FAIL, result.getCode());
        assertEquals("error", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFailureWithCodeAndMessage() {
        Result<String> result = Result.failure(404, "Not Found");

        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(404, result.getCode());
        assertEquals("Not Found", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFailureWithMessage() {
        Result<String> result = Result.failure("Custom error");

        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(Result.FAIL, result.getCode());
        assertEquals("Custom error", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFailureWithMessageAndData() {
        String data = "error data";
        Result<String> result = Result.failure(data);

        assertFalse(result.isSuccess());
        assertTrue(result.isFail());
        assertEquals(Result.FAIL, result.getCode());
        assertEquals(data, result.getMessage());
    }

    // ========== 静态方法测试 ==========

    @Test
    void testStaticIsSuccess() {
        Result<String> successResult = Result.success("data");
        Result<String> failResult = Result.failure("error");

        assertTrue(Result.isSuccess(successResult));
        assertFalse(Result.isSuccess(failResult));
    }

    @Test
    void testStaticIsFailure() {
        Result<String> successResult = Result.success("data");
        Result<String> failResult = Result.failure("error");

        assertFalse(Result.isFailure(successResult));
        assertTrue(Result.isFailure(failResult));
    }

    // ========== 函数式方法测试 ==========

    @Test
    void testMapSuccess() {
        Result<String> result = Result.success("hello");
        Result<String> mapped = result.map(String::toUpperCase);

        assertTrue(mapped.isSuccess());
        assertEquals("HELLO", mapped.getData());
        assertEquals(Result.SERVER_SUCCESS, mapped.getMessage());
    }

    @Test
    void testMapFailure() {
        Result<String> result = Result.failure("error");
        Result<String> mapped = result.map(String::toUpperCase);

        assertFalse(mapped.isSuccess());
        assertEquals("error", mapped.getMessage());
        assertNull(mapped.getData());
    }

    @Test
    void testMapWithException() {
        Result<String> result = Result.success("hello");
        Result<Integer> mapped = result.map(s -> {
            throw new RuntimeException("Map error");
        });

        assertFalse(mapped.isSuccess());
        assertTrue(mapped.getMessage().contains("数据转换失败"));
        assertTrue(mapped.getMessage().contains("Map error"));
    }

    @Test
    void testFlatMapSuccess() {
        Result<String> result = Result.success("hello");
        Result<String> flatMapped = result.flatMap(s -> Result.success(s.toUpperCase()));

        assertTrue(flatMapped.isSuccess());
        assertEquals("HELLO", flatMapped.getData());
    }

    @Test
    void testFlatMapFailure() {
        Result<String> result = Result.failure("error");
        Result<String> flatMapped = result.flatMap(s -> Result.success(s.toUpperCase()));

        assertFalse(flatMapped.isSuccess());
        assertEquals("error", flatMapped.getMessage());
    }

    @Test
    void testFlatMapReturnsFailure() {
        Result<String> result = Result.success("hello");
        Result<String> flatMapped = result.flatMap(s -> Result.failure("FlatMap error"));

        assertFalse(flatMapped.isSuccess());
        assertEquals("FlatMap error", flatMapped.getMessage());
    }

    @Test
    void testFlatMapWithException() {
        Result<String> result = Result.success("hello");
        Result<String> flatMapped = result.flatMap(s -> {
            throw new RuntimeException("FlatMap exception");
        });

        assertFalse(flatMapped.isSuccess());
        assertTrue(flatMapped.getMessage().contains("数据转换失败"));
        assertTrue(flatMapped.getMessage().contains("FlatMap exception"));
    }

    @Test
    void testFilterSuccess() {
        Result<String> result = Result.success("hello");
        Result<String> filtered = result.filter(s -> s.length() > 3, "Too short");

        assertTrue(filtered.isSuccess());
        assertEquals("hello", filtered.getData());
    }

    @Test
    void testFilterFail() {
        Result<String> result = Result.success("hi");
        Result<String> filtered = result.filter(s -> s.length() > 3, "Too short");

        assertFalse(filtered.isSuccess());
        assertEquals("Too short", filtered.getMessage());
    }

    @Test
    void testFilterWithNullData() {
        Result<String> result = Result.success(null);
        Result<String> filtered = result.filter(s -> s.length() > 3, "Too short");

        assertTrue(filtered.isSuccess()); // 应该返回原结果
        assertNull(filtered.getData());
    }

    @Test
    void testFilterOnFailure() {
        Result<String> result = Result.failure("error");
        Result<String> filtered = result.filter(s -> s.length() > 3, "Too short");

        assertFalse(filtered.isSuccess());
        assertEquals("error", filtered.getMessage()); // 应该保持原错误消息
    }

    @Test
    void testFilterWithException() {
        Result<String> result = Result.success("hello");
        Result<String> filtered = result.filter(s -> {
            throw new RuntimeException("Filter error");
        }, "Too short");

        assertFalse(filtered.isSuccess());
        assertTrue(filtered.getMessage().contains("数据过滤失败"));
        assertTrue(filtered.getMessage().contains("Filter error"));
    }

    @Test
    void testOnSuccessCallback() {
        AtomicBoolean called = new AtomicBoolean(false);
        Result<String> result = Result.success("data");

        Result<String> returned = result.onSuccess(data -> called.set(true));

        assertTrue(called.get());
        assertSame(result, returned); // 应该返回同一个实例
    }

    @Test
    void testOnSuccessWithNullData() {
        AtomicBoolean called = new AtomicBoolean(false);
        Result<String> result = Result.success(null);

        result.onSuccess(data -> called.set(true));

        assertFalse(called.get()); // null数据不应该调用回调
    }

    @Test
    void testOnSuccessNotCalledOnFailure() {
        AtomicBoolean called = new AtomicBoolean(false);
        Result<String> result = Result.failure("error");

        result.onSuccess(data -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void testOnSuccessWithException() {
        Result<String> result = Result.success("data");

        // 异常应该被忽略，不改变结果状态
        Result<String> returned = result.onSuccess(data -> {
            throw new RuntimeException("Callback error");
        });

        assertTrue(returned.isSuccess());
        assertEquals("data", returned.getData());
    }

    @Test
    void testOnErrorCallback() {
        AtomicBoolean called = new AtomicBoolean(false);
        Result<String> result = Result.failure("error message");

        Result<String> returned = result.onError(message -> called.set(true));

        assertTrue(called.get());
        assertSame(result, returned); // 应该返回同一个实例
    }

    @Test
    void testOnErrorNotCalledOnSuccess() {
        AtomicBoolean called = new AtomicBoolean(false);
        Result<String> result = Result.success("data");

        result.onError(message -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void testOnErrorWithException() {
        Result<String> result = Result.failure("error");

        // 异常应该被忽略，不改变结果状态
        Result<String> returned = result.onError(message -> {
            throw new RuntimeException("Callback error");
        });

        assertFalse(returned.isSuccess());
        assertEquals("error", returned.getMessage());
    }

    @Test
    void testGetOrElseSuccess() {
        Result<String> result = Result.success("data");
        assertEquals("data", result.getOrElse("default"));
    }

    @Test
    void testGetOrElseFailure() {
        Result<String> result = Result.failure("error");
        assertEquals("default", result.getOrElse("default"));
    }

    @Test
    void testGetOrElseGetSuccess() {
        Result<String> result = Result.success("data");
        assertEquals("data", result.getOrElseGet(() -> "default"));
    }

    @Test
    void testGetOrElseGetFailure() {
        Result<String> result = Result.failure("error");
        assertEquals("default", result.getOrElseGet(() -> "default"));
    }

    // ========== 参数化测试 ==========

    @ParameterizedTest
    @MethodSource("provideSuccessTestCases")
    void testVariousSuccessCases(Object data, boolean expectedSuccess) {
        Result<Object> result = Result.success(data);

        assertEquals(expectedSuccess, result.isSuccess());
        assertEquals(data, result.getData());
    }

    static Stream<Arguments> provideSuccessTestCases() {
        return Stream.of(
                Arguments.of("string", true),
                Arguments.of(42, true),
                Arguments.of(null, true),
                Arguments.of(true, true)
        );
    }

    // ========== 链式调用测试 ==========

    @Test
    void testChainedOperationsSuccess() {
        Result<String> result = Result.success("hello")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3, "Too short")
                .onSuccess(data -> System.out.println("Success: " + data));

        assertTrue(result.isSuccess());
        assertEquals("HELLO", result.getData());
    }

    @Test
    void testChainedOperationsWithFailure() {
        AtomicBoolean successCalled = new AtomicBoolean(false);
        AtomicBoolean errorCalled = new AtomicBoolean(false);

        Result<String> result = Result.success("hi")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3, "Too short") // 这里会失败
                .onSuccess(data -> successCalled.set(true))
                .onError(message -> errorCalled.set(true));

        assertFalse(result.isSuccess());
        assertEquals("Too short", result.getMessage());
        assertFalse(successCalled.get());
        assertTrue(errorCalled.get());
    }
}
