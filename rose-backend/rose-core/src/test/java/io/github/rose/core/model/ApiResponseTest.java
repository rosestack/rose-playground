package io.github.rose.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 测试类
 * 测试API响应结果包装类的所有功能
 */
class ApiResponseTest {

    // ========== 基本构造和状态测试 ==========

    @Test
    void testDefaultConstructor() {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        assertNull(apiResponse.getCode());
        assertNull(apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testFullConstructor() {
        String data = "test data";
        ApiResponse<String> apiResponse = new ApiResponse<>(200, "success", data);

        assertEquals(200, apiResponse.getCode());
        assertEquals("success", apiResponse.getMessage());
        assertEquals(data, apiResponse.getData());
    }

    @Test
    void testSuccessWithoutData() {
        ApiResponse<String> apiResponse = ApiResponse.success();

        assertTrue(apiResponse.isSuccess());
        assertFalse(apiResponse.isFail());
        assertEquals(ApiResponse.SUCCESS, apiResponse.getCode());
        assertEquals(ApiResponse.SERVER_SUCCESS, apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testSuccessWithData() {
        String data = "test data";
        ApiResponse<String> apiResponse = ApiResponse.success(data);

        assertTrue(apiResponse.isSuccess());
        assertFalse(apiResponse.isFail());
        assertEquals(ApiResponse.SUCCESS, apiResponse.getCode());
        assertEquals(ApiResponse.SERVER_SUCCESS, apiResponse.getMessage());
        assertEquals(data, apiResponse.getData());
    }

    @Test
    void testFailureDefault() {
        ApiResponse<String> apiResponse = ApiResponse.error();

        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.isFail());
        assertEquals(ApiResponse.FAIL, apiResponse.getCode());
        assertEquals("error", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testFailureWithCodeAndMessage() {
        ApiResponse<String> apiResponse = ApiResponse.error(404, "Not Found");

        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.isFail());
        assertEquals(404, apiResponse.getCode());
        assertEquals("Not Found", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testFailureWithMessage() {
        ApiResponse<String> apiResponse = ApiResponse.error("Custom error");

        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.isFail());
        assertEquals(ApiResponse.FAIL, apiResponse.getCode());
        assertEquals("Custom error", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testFailureWithMessageAndData() {
        String data = "error data";
        ApiResponse<String> apiResponse = ApiResponse.error(data);

        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.isFail());
        assertEquals(ApiResponse.FAIL, apiResponse.getCode());
        assertEquals(data, apiResponse.getMessage());
    }

    // ========== 静态方法测试 ==========

    @Test
    void testStaticIsSuccess() {
        ApiResponse<String> successApiResponse = ApiResponse.success("data");
        ApiResponse<String> failApiResponse = ApiResponse.error("error");

        assertTrue(ApiResponse.isSuccess(successApiResponse));
        assertFalse(ApiResponse.isSuccess(failApiResponse));
    }

    @Test
    void testStaticIsFailure() {
        ApiResponse<String> successApiResponse = ApiResponse.success("data");
        ApiResponse<String> failApiResponse = ApiResponse.error("error");

        assertFalse(ApiResponse.isFailure(successApiResponse));
        assertTrue(ApiResponse.isFailure(failApiResponse));
    }

    // ========== 函数式方法测试 ==========

    @Test
    void testMapSuccess() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<String> mapped = apiResponse.map(String::toUpperCase);

        assertTrue(mapped.isSuccess());
        assertEquals("HELLO", mapped.getData());
        assertEquals(ApiResponse.SERVER_SUCCESS, mapped.getMessage());
    }

    @Test
    void testMapFailure() {
        ApiResponse<String> apiResponse = ApiResponse.error("error");
        ApiResponse<String> mapped = apiResponse.map(String::toUpperCase);

        assertFalse(mapped.isSuccess());
        assertEquals("error", mapped.getMessage());
        assertNull(mapped.getData());
    }

    @Test
    void testMapWithException() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<Integer> mapped = apiResponse.map(s -> {
            throw new RuntimeException("Map error");
        });

        assertFalse(mapped.isSuccess());
        assertTrue(mapped.getMessage().contains("数据转换失败"));
        assertTrue(mapped.getMessage().contains("Map error"));
    }

    @Test
    void testFlatMapSuccess() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<String> flatMapped = apiResponse.flatMap(s -> ApiResponse.success(s.toUpperCase()));

        assertTrue(flatMapped.isSuccess());
        assertEquals("HELLO", flatMapped.getData());
    }

    @Test
    void testFlatMapFailure() {
        ApiResponse<String> apiResponse = ApiResponse.error("error");
        ApiResponse<String> flatMapped = apiResponse.flatMap(s -> ApiResponse.success(s.toUpperCase()));

        assertFalse(flatMapped.isSuccess());
        assertEquals("error", flatMapped.getMessage());
    }

    @Test
    void testFlatMapReturnsFailure() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<String> flatMapped = apiResponse.flatMap(s -> ApiResponse.error("FlatMap error"));

        assertFalse(flatMapped.isSuccess());
        assertEquals("FlatMap error", flatMapped.getMessage());
    }

    @Test
    void testFlatMapWithException() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<String> flatMapped = apiResponse.flatMap(s -> {
            throw new RuntimeException("FlatMap exception");
        });

        assertFalse(flatMapped.isSuccess());
        assertTrue(flatMapped.getMessage().contains("数据转换失败"));
        assertTrue(flatMapped.getMessage().contains("FlatMap exception"));
    }

    @Test
    void testFilterSuccess() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<String> filtered = apiResponse.filter(s -> s.length() > 3, "Too short");

        assertTrue(filtered.isSuccess());
        assertEquals("hello", filtered.getData());
    }

    @Test
    void testFilterFail() {
        ApiResponse<String> apiResponse = ApiResponse.success("hi");
        ApiResponse<String> filtered = apiResponse.filter(s -> s.length() > 3, "Too short");

        assertFalse(filtered.isSuccess());
        assertEquals("Too short", filtered.getMessage());
    }

    @Test
    void testFilterWithNullData() {
        ApiResponse<String> apiResponse = ApiResponse.success(null);
        ApiResponse<String> filtered = apiResponse.filter(s -> s.length() > 3, "Too short");

        assertTrue(filtered.isSuccess()); // 应该返回原结果
        assertNull(filtered.getData());
    }

    @Test
    void testFilterOnFailure() {
        ApiResponse<String> apiResponse = ApiResponse.error("error");
        ApiResponse<String> filtered = apiResponse.filter(s -> s.length() > 3, "Too short");

        assertFalse(filtered.isSuccess());
        assertEquals("error", filtered.getMessage()); // 应该保持原错误消息
    }

    @Test
    void testFilterWithException() {
        ApiResponse<String> apiResponse = ApiResponse.success("hello");
        ApiResponse<String> filtered = apiResponse.filter(s -> {
            throw new RuntimeException("Filter error");
        }, "Too short");

        assertFalse(filtered.isSuccess());
        assertTrue(filtered.getMessage().contains("数据过滤失败"));
        assertTrue(filtered.getMessage().contains("Filter error"));
    }

    @Test
    void testOnSuccessCallback() {
        AtomicBoolean called = new AtomicBoolean(false);
        ApiResponse<String> apiResponse = ApiResponse.success("data");

        ApiResponse<String> returned = apiResponse.onSuccess(data -> called.set(true));

        assertTrue(called.get());
        assertSame(apiResponse, returned); // 应该返回同一个实例
    }

    @Test
    void testOnSuccessWithNullData() {
        AtomicBoolean called = new AtomicBoolean(false);
        ApiResponse<String> apiResponse = ApiResponse.success(null);

        apiResponse.onSuccess(data -> called.set(true));

        assertFalse(called.get()); // null数据不应该调用回调
    }

    @Test
    void testOnSuccessNotCalledOnFailure() {
        AtomicBoolean called = new AtomicBoolean(false);
        ApiResponse<String> apiResponse = ApiResponse.error("error");

        apiResponse.onSuccess(data -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void testOnSuccessWithException() {
        ApiResponse<String> apiResponse = ApiResponse.success("data");

        // 异常应该被忽略，不改变结果状态
        ApiResponse<String> returned = apiResponse.onSuccess(data -> {
            throw new RuntimeException("Callback error");
        });

        assertTrue(returned.isSuccess());
        assertEquals("data", returned.getData());
    }

    @Test
    void testOnErrorCallback() {
        AtomicBoolean called = new AtomicBoolean(false);
        ApiResponse<String> apiResponse = ApiResponse.error("error message");

        ApiResponse<String> returned = apiResponse.onError(message -> called.set(true));

        assertTrue(called.get());
        assertSame(apiResponse, returned); // 应该返回同一个实例
    }

    @Test
    void testOnErrorNotCalledOnSuccess() {
        AtomicBoolean called = new AtomicBoolean(false);
        ApiResponse<String> apiResponse = ApiResponse.success("data");

        apiResponse.onError(message -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void testOnErrorWithException() {
        ApiResponse<String> apiResponse = ApiResponse.error("error");

        // 异常应该被忽略，不改变结果状态
        ApiResponse<String> returned = apiResponse.onError(message -> {
            throw new RuntimeException("Callback error");
        });

        assertFalse(returned.isSuccess());
        assertEquals("error", returned.getMessage());
    }

    @Test
    void testGetOrElseSuccess() {
        ApiResponse<String> apiResponse = ApiResponse.success("data");
        assertEquals("data", apiResponse.getOrElse("default"));
    }

    @Test
    void testGetOrElseFailure() {
        ApiResponse<String> apiResponse = ApiResponse.error("error");
        assertEquals("default", apiResponse.getOrElse("default"));
    }

    @Test
    void testGetOrElseGetSuccess() {
        ApiResponse<String> apiResponse = ApiResponse.success("data");
        assertEquals("data", apiResponse.getOrElseGet(() -> "default"));
    }

    @Test
    void testGetOrElseGetFailure() {
        ApiResponse<String> apiResponse = ApiResponse.error("error");
        assertEquals("default", apiResponse.getOrElseGet(() -> "default"));
    }

    // ========== 参数化测试 ==========

    @ParameterizedTest
    @MethodSource("provideSuccessTestCases")
    void testVariousSuccessCases(Object data, boolean expectedSuccess) {
        ApiResponse<Object> apiResponse = ApiResponse.success(data);

        assertEquals(expectedSuccess, apiResponse.isSuccess());
        assertEquals(data, apiResponse.getData());
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
        ApiResponse<String> apiResponse = ApiResponse.success("hello")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3, "Too short")
                .onSuccess(data -> System.out.println("Success: " + data));

        assertTrue(apiResponse.isSuccess());
        assertEquals("HELLO", apiResponse.getData());
    }

    @Test
    void testChainedOperationsWithFailure() {
        AtomicBoolean successCalled = new AtomicBoolean(false);
        AtomicBoolean errorCalled = new AtomicBoolean(false);

        ApiResponse<String> apiResponse = ApiResponse.success("hi")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3, "Too short") // 这里会失败
                .onSuccess(data -> successCalled.set(true))
                .onError(message -> errorCalled.set(true));

        assertFalse(apiResponse.isSuccess());
        assertEquals("Too short", apiResponse.getMessage());
        assertFalse(successCalled.get());
        assertTrue(errorCalled.get());
    }
}
