package io.github.rosestack.core.lang.function;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Option 测试类 测试可选值容器的所有功能
 */
class OptionTest {

    // ========== 基本构造和状态测试 ==========

    static Stream<Arguments> provideSomeTestCases() {
        return Stream.of(
                Arguments.of("string", "string"),
                Arguments.of(42, 42),
                Arguments.of(true, true),
                Arguments.of(3.14, 3.14));
    }

    @Test
    void testSomeCreation() {
        Option<String> option = Option.some("value");

        assertTrue(option.isPresent());
        assertFalse(option.isEmpty());
        assertEquals("value", option.get());
    }

    @Test
    void testSomeWithNullThrowsException() {
        assertThrows(NullPointerException.class, () -> Option.some(null));
    }

    @Test
    void testNoneCreation() {
        Option<String> option = Option.none();

        assertFalse(option.isPresent());
        assertTrue(option.isEmpty());
        assertThrows(IllegalStateException.class, option::get);
    }

    @Test
    void testOfWithValue() {
        Option<String> option = Option.of("value");

        assertTrue(option.isPresent());
        assertEquals("value", option.get());
    }

    @Test
    void testOfWithNull() {
        Option<String> option = Option.of(null);

        assertFalse(option.isPresent());
        assertTrue(option.isEmpty());
    }

    @Test
    void testFromOptionalPresent() {
        Optional<String> javaOptional = Optional.of("value");
        Option<String> option = Option.from(javaOptional);

        assertTrue(option.isPresent());
        assertEquals("value", option.get());
    }

    // ========== 值获取方法测试 ==========

    @Test
    void testFromOptionalEmpty() {
        Optional<String> javaOptional = Optional.empty();
        Option<String> option = Option.from(javaOptional);

        assertFalse(option.isPresent());
    }

    @Test
    void testGetOrElsePresent() {
        Option<String> option = Option.some("value");
        assertEquals("value", option.getOrElse("default"));
    }

    @Test
    void testGetOrElseEmpty() {
        Option<String> option = Option.none();
        assertEquals("default", option.getOrElse("default"));
    }

    @Test
    void testGetOrElseGetPresent() {
        Option<String> option = Option.some("value");
        assertEquals("value", option.getOrElseGet(() -> "default"));
    }

    @Test
    void testGetOrElseGetEmpty() {
        Option<String> option = Option.none();
        assertEquals("default", option.getOrElseGet(() -> "default"));
    }

    @Test
    void testGetOrElseGetWithNullSupplier() {
        Option<String> option = Option.none();
        assertThrows(NullPointerException.class, () -> option.getOrElseGet(null));
    }

    @Test
    void testGetOrElseThrowPresent() {
        Option<String> option = Option.some("value");
        assertEquals("value", option.getOrElseThrow(() -> new RuntimeException("Should not throw")));
    }

    @Test
    void testGetOrElseThrowEmpty() {
        Option<String> option = Option.none();
        RuntimeException exception = new RuntimeException("Custom exception");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> option.getOrElseThrow(() -> exception));
        assertEquals(exception, thrown);
    }

    // ========== 回调方法测试 ==========

    @Test
    void testGetOrElseThrowWithNullSupplier() {
        Option<String> option = Option.none();
        assertThrows(NullPointerException.class, () -> option.getOrElseThrow(null));
    }

    @Test
    void testOnPresentWithValue() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option<String> option = Option.some("value");

        Option<String> returned = option.onPresent(value -> called.set(true));

        assertTrue(called.get());
        assertSame(option, returned);
    }

    @Test
    void testOnPresentEmpty() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option<String> option = Option.none();

        option.onPresent(value -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void testOnPresentWithNullConsumer() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.onPresent(null));
    }

    @Test
    void testOnEmptyWithValue() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option<String> option = Option.some("value");

        option.onEmpty(() -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void testOnEmptyEmpty() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option<String> option = Option.none();

        Option<String> returned = option.onEmpty(() -> called.set(true));

        assertTrue(called.get());
        assertSame(option, returned);
    }

    // ========== 转换方法测试 ==========

    @Test
    void testOnEmptyWithNullRunnable() {
        Option<String> option = Option.none();
        assertThrows(NullPointerException.class, () -> option.onEmpty(null));
    }

    @Test
    void testMapPresent() {
        Option<String> option = Option.some("hello");
        Option<String> mapped = option.map(String::toUpperCase);

        assertTrue(mapped.isPresent());
        assertEquals("HELLO", mapped.get());
    }

    @Test
    void testMapEmpty() {
        Option<String> option = Option.none();
        Option<String> mapped = option.map(String::toUpperCase);

        assertFalse(mapped.isPresent());
    }

    @Test
    void testMapReturnsNull() {
        Option<String> option = Option.some("value");
        Option<String> mapped = option.map(s -> null);

        assertFalse(mapped.isPresent());
    }

    @Test
    void testMapWithException() {
        Option<String> option = Option.some("value");
        Option<String> mapped = option.map(s -> {
            throw new RuntimeException("Map error");
        });

        assertFalse(mapped.isPresent());
    }

    @Test
    void testMapWithNullFunction() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.map((java.util.function.Function<String, String>) null));
    }

    @Test
    void testMapWithCheckedFunction() {
        Option<String> option = Option.some("hello");
        Option<String> mapped = option.mapChecked(String::toUpperCase);

        assertTrue(mapped.isPresent());
        assertEquals("HELLO", mapped.get());
    }

    @Test
    void testMapWithCheckedFunctionThrowsException() {
        Option<String> option = Option.some("hello");
        Option<String> mapped = option.mapChecked(s -> {
            throw new Exception("Checked function error");
        });

        assertFalse(mapped.isPresent());
    }

    @Test
    void testFlatMapPresent() {
        Option<String> option = Option.some("hello");
        Option<String> flatMapped = option.flatMap(s -> Option.some(s.toUpperCase()));

        assertTrue(flatMapped.isPresent());
        assertEquals("HELLO", flatMapped.get());
    }

    @Test
    void testFlatMapEmpty() {
        Option<String> option = Option.none();
        Option<String> flatMapped = option.flatMap(s -> Option.some(s.toUpperCase()));

        assertFalse(flatMapped.isPresent());
    }

    @Test
    void testFlatMapReturnsNone() {
        Option<String> option = Option.some("hello");
        Option<String> flatMapped = option.flatMap(s -> Option.none());

        assertFalse(flatMapped.isPresent());
    }

    @Test
    void testFlatMapWithException() {
        Option<String> option = Option.some("hello");
        Option<String> flatMapped = option.flatMap(s -> {
            throw new RuntimeException("FlatMap error");
        });

        assertFalse(flatMapped.isPresent());
    }

    @Test
    void testFlatMapWithNullFunction() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.flatMap(null));
    }

    @Test
    void testFilterPresentMatches() {
        Option<String> option = Option.some("hello");
        Option<String> filtered = option.filter(s -> s.length() > 3);

        assertTrue(filtered.isPresent());
        assertEquals("hello", filtered.get());
    }

    @Test
    void testFilterPresentNotMatches() {
        Option<String> option = Option.some("hi");
        Option<String> filtered = option.filter(s -> s.length() > 3);

        assertFalse(filtered.isPresent());
    }

    @Test
    void testFilterEmpty() {
        Option<String> option = Option.none();
        Option<String> filtered = option.filter(s -> s.length() > 3);

        assertFalse(filtered.isPresent());
    }

    // ========== orElse 方法测试 ==========

    @Test
    void testFilterWithNullPredicate() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.filter(null));
    }

    @Test
    void testOrElsePresent() {
        Option<String> option = Option.some("value");
        Option<String> other = Option.some("other");

        Option<String> result = option.orElse(other);

        assertSame(option, result);
        assertEquals("value", result.get());
    }

    @Test
    void testOrElseEmpty() {
        Option<String> option = Option.none();
        Option<String> other = Option.some("other");

        Option<String> result = option.orElse(other);

        assertSame(other, result);
        assertEquals("other", result.get());
    }

    @Test
    void testOrElseWithNull() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.orElse(null));
    }

    @Test
    void testOrElseGetPresent() {
        Option<String> option = Option.some("value");

        Option<String> result = option.orElseGet(() -> Option.some("other"));

        assertSame(option, result);
        assertEquals("value", result.get());
    }

    @Test
    void testOrElseGetEmpty() {
        Option<String> option = Option.none();
        Option<String> other = Option.some("other");

        Option<String> result = option.orElseGet(() -> other);

        assertSame(other, result);
        assertEquals("other", result.get());
    }

    // ========== 转换方法测试 ==========

    @Test
    void testOrElseGetWithNullSupplier() {
        Option<String> option = Option.none();
        assertThrows(NullPointerException.class, () -> option.orElseGet(null));
    }

    @Test
    void testToTryPresent() {
        Option<String> option = Option.some("value");
        Try<String> tryResult = option.toTry();

        assertTrue(tryResult.isSuccess());
        assertEquals("value", tryResult.get());
    }

    // ========== 匹配方法测试 ==========

    @Test
    void testToTryEmpty() {
        Option<String> option = Option.none();
        Try<String> tryResult = option.toTry();

        assertTrue(tryResult.isFailure());
        assertTrue(tryResult.getCause() instanceof IllegalStateException);
    }

    @Test
    void testMatchPresent() {
        Option<String> option = Option.some("hello");

        String result = option.match(value -> value.toUpperCase(), () -> "empty");

        assertEquals("HELLO", result);
    }

    @Test
    void testMatchEmpty() {
        Option<String> option = Option.none();

        String result = option.match(value -> value.toUpperCase(), () -> "empty");

        assertEquals("empty", result);
    }

    @Test
    void testMatchWithNullPresentMapper() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.match(null, () -> "empty"));
    }

    @Test
    void testMatchWithNullEmptySupplier() {
        Option<String> option = Option.some("value");
        assertThrows(NullPointerException.class, () -> option.match(value -> value, null));
    }

    @Test
    void testMatchConsumerPresent() {
        AtomicBoolean presentCalled = new AtomicBoolean(false);
        AtomicBoolean emptyCalled = new AtomicBoolean(false);
        Option<String> option = Option.some("value");

        option.match(value -> presentCalled.set(true), () -> emptyCalled.set(true));

        assertTrue(presentCalled.get());
        assertFalse(emptyCalled.get());
    }

    // ========== equals 和 hashCode 测试 ==========

    @Test
    void testMatchConsumerEmpty() {
        AtomicBoolean presentCalled = new AtomicBoolean(false);
        AtomicBoolean emptyCalled = new AtomicBoolean(false);
        Option<String> option = Option.none();

        option.match(value -> presentCalled.set(true), () -> emptyCalled.set(true));

        assertFalse(presentCalled.get());
        assertTrue(emptyCalled.get());
    }

    @Test
    void testEqualsAndHashCodeSome() {
        Option<String> option1 = Option.some("value");
        Option<String> option2 = Option.some("value");
        Option<String> option3 = Option.some("other");

        assertEquals(option1, option2);
        assertNotEquals(option1, option3);
        assertEquals(option1.hashCode(), option2.hashCode());
    }

    @Test
    void testEqualsAndHashCodeNone() {
        Option<String> option1 = Option.none();
        Option<String> option2 = Option.none();

        assertEquals(option1, option2);
        assertEquals(option1.hashCode(), option2.hashCode());
    }

    @Test
    void testEqualsSomeAndNone() {
        Option<String> some = Option.some("value");
        Option<String> none = Option.none();

        assertNotEquals(some, none);
        assertNotEquals(none, some);
    }

    @Test
    void testEqualsWithNull() {
        Option<String> option = Option.some("value");

        assertNotEquals(option, null);
    }

    // ========== toString 测试 ==========

    @Test
    void testEqualsWithDifferentType() {
        Option<String> option = Option.some("value");

        assertNotEquals(option, "value");
        assertNotEquals(option, Optional.of("value"));
    }

    @Test
    void testToStringSome() {
        Option<String> option = Option.some("value");
        String toString = option.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Some"));
        assertTrue(toString.contains("value"));
    }

    // ========== 参数化测试 ==========

    @Test
    void testToStringNone() {
        Option<String> option = Option.none();
        String toString = option.toString();

        assertNotNull(toString);
        assertEquals("None", toString);
    }

    @ParameterizedTest
    @MethodSource("provideSomeTestCases")
    void testVariousSomeCases(Object input, Object expected) {
        Option<Object> option = Option.some(input);

        assertTrue(option.isPresent());
        assertEquals(expected, option.get());
    }

    // ========== 链式调用测试 ==========

    @Test
    void testChainedOperationsPresent() {
        Option<String> result = Option.some("hello")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3)
                .flatMap(s -> Option.some(s + " WORLD"));

        assertTrue(result.isPresent());
        assertEquals("HELLO WORLD", result.get());
    }

    @Test
    void testChainedOperationsWithFilter() {
        Option<String> result = Option.some("hi")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3) // 这里会过滤掉
                .flatMap(s -> Option.some(s + " WORLD"));

        assertFalse(result.isPresent());
    }

    @Test
    void testChainedOperationsEmpty() {
        Option<String> result = Option.<String>none()
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3)
                .flatMap(s -> Option.some(s + " WORLD"));

        assertFalse(result.isPresent());
    }

    // ========== 边界情况测试 ==========

    @Test
    void testNoneIsSingleton() {
        Option<String> none1 = Option.none();
        Option<Integer> none2 = Option.none();

        // 虽然泛型不同，但底层应该是同一个实例
        assertSame(none1, none2);
    }
}
