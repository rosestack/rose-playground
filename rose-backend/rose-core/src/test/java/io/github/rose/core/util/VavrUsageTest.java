package io.github.rose.core.util;

import io.vavr.control.Try;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vavr 使用示例测试
 * 展示如何使用 Vavr 进行函数式编程
 */
class VavrUsageTest {
    
    private static final Logger log = LoggerFactory.getLogger(VavrUsageTest.class);
    
    // ==================== Try 使用示例 ====================
    
    @Test
    void testTryBasicUsage() {
        // 基本用法：处理可能抛出异常的操作
        Try<Integer> result = Try.of(() -> Integer.parseInt("123"));
        
        assertTrue(result.isSuccess());
        assertEquals(123, result.get());
    }
    
    @Test
    void testTryWithFailure() {
        // 处理失败的情况
        Try<Integer> result = Try.of(() -> Integer.parseInt("invalid"));
        
        assertTrue(result.isFailure());
        assertThrows(NumberFormatException.class, result::get);
    }
    
    @Test
    void testTryWithFallback() {
        // 使用默认值处理失败
        Integer result = Try.of(() -> Integer.parseInt("invalid"))
                .getOrElse(0);
        
        assertEquals(0, result);
    }
    
    @Test
    void testTryWithRecovery() {
        // 使用 recover 处理特定异常
        Try<Integer> result = Try.of(() -> Integer.parseInt("invalid"))
                .recover(NumberFormatException.class, 0);
        
        assertTrue(result.isSuccess());
        assertEquals(0, result.get());
    }
    
    @Test
    void testTryChainOperations() {
        // 链式操作
        String result = Try.of(() -> "123")
                .map(Integer::parseInt)
                .map(i -> i * 2)
                .map(String::valueOf)
                .getOrElse("0");
        
        assertEquals("246", result);
    }
    
    @Test
    void testTryWithSideEffects() {
        // 使用 onSuccess 和 onFailure 处理副作用
        Try<Integer> result = Try.of(() -> Integer.parseInt("123"))
                .onSuccess(value -> log.info("Successfully parsed: {}", value))
                .onFailure(error -> log.error("Failed to parse: {}", error.getMessage()));
        
        assertTrue(result.isSuccess());
        assertEquals(123, result.get());
    }
    
    @Test
    void testTryFlatMap() {
        // 使用 flatMap 组合多个操作
        Try<Integer> result = Try.of(() -> Integer.parseInt("10"))
                .flatMap(v1 -> Try.of(() -> Integer.parseInt("20"))
                        .map(v2 -> v1 + v2));
        
        assertTrue(result.isSuccess());
        assertEquals(30, result.get());
    }
    
    // ==================== Either 使用示例 ====================
    
    @Test
    void testEitherBasicUsage() {
        // Either 用于表示成功或失败
        Either<String, Integer> result = Try.of(() -> Integer.parseInt("123"))
                .toEither()
                .mapLeft(Throwable::getMessage);
        
        assertTrue(result.isRight());
        assertEquals(123, result.get());
    }
    
    @Test
    void testEitherWithFailure() {
        // 处理失败情况
        Either<String, Integer> result = Try.of(() -> Integer.parseInt("invalid"))
                .toEither()
                .mapLeft(Throwable::getMessage);
        
        assertTrue(result.isLeft());
        assertTrue(result.getLeft().contains("NumberFormatException"));
    }
    
    @Test
    void testEitherMap() {
        // 对成功值进行转换
        Either<String, String> result = Try.of(() -> Integer.parseInt("123"))
                .toEither()
                .mapLeft(Throwable::getMessage)
                .map(String::valueOf);
        
        assertTrue(result.isRight());
        assertEquals("123", result.get());
    }
    
    @Test
    void testEitherMapLeft() {
        // 对错误值进行转换
        Either<String, Integer> result = Try.of(() -> Integer.parseInt("invalid"))
                .toEither()
                .mapLeft(error -> "Parse error: " + error.getMessage());
        
        assertTrue(result.isLeft());
        assertTrue(result.getLeft().startsWith("Parse error:"));
    }
    
    // ==================== Option 使用示例 ====================
    
    @Test
    void testOptionBasicUsage() {
        // Option 用于处理可能为空的值
        Option<String> some = Option.of("hello");
        Option<String> none = Option.none();
        
        assertTrue(some.isDefined());
        assertFalse(none.isDefined());
        assertEquals("hello", some.get());
    }
    
    @Test
    void testOptionMap() {
        // 对 Option 中的值进行转换
        Option<Integer> result = Option.of("123")
                .map(Integer::parseInt);
        
        assertTrue(result.isDefined());
        assertEquals(123, result.get());
    }
    
    @Test
    void testOptionFlatMap() {
        // 使用 flatMap 处理嵌套的 Option
        Option<Integer> result = Option.of("123")
                .flatMap(s -> Try.of(() -> Integer.parseInt(s))
                        .toOption());
        
        assertTrue(result.isDefined());
        assertEquals(123, result.get());
    }
    
    @Test
    void testOptionGetOrElse() {
        // 使用默认值
        Integer result = Option.<Integer>none()
                .getOrElse(0);
        
        assertEquals(0, result);
    }
    
    // ==================== 实际应用示例 ====================
    
    @Test
    void testUserInputValidation() {
        // 模拟用户输入验证
        String userInput = "25";
        
        Either<String, Integer> result = validateAndParseAge(userInput);
        
        assertTrue(result.isRight());
        assertEquals(25, result.get());
    }
    
    @Test
    void testUserInputValidationFailure() {
        // 模拟无效用户输入
        String userInput = "invalid";
        
        Either<String, Integer> result = validateAndParseAge(userInput);
        
        assertTrue(result.isLeft());
        assertTrue(result.getLeft().contains("Invalid age"));
    }
    
    @Test
    void testComplexDataProcessing() {
        // 复杂数据处理示例
        String[] inputs = {"10", "20", "invalid", "30"};
        
        Try<Integer> result = Try.of(() -> inputs)
                .map(this::processArray)
                .onSuccess(sum -> log.info("Total sum: {}", sum))
                .onFailure(error -> log.error("Processing failed: {}", error.getMessage()));
        
        assertTrue(result.isSuccess());
        assertEquals(60, result.get()); // 10 + 20 + 0 + 30
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 验证并解析年龄
     */
    private Either<String, Integer> validateAndParseAge(String input) {
        return Try.of(() -> Integer.parseInt(input))
                .filter(age -> age >= 0 && age <= 150)
                .toEither()
                .mapLeft(error -> "Invalid age: " + input);
    }
    
    /**
     * 处理数组，忽略无效值
     */
    private Integer processArray(String[] inputs) {
        return Try.of(() -> inputs)
                .map(array -> {
                    int sum = 0;
                    for (String input : array) {
                        sum += Try.of(() -> Integer.parseInt(input))
                                .getOrElse(0);
                    }
                    return sum;
                })
                .getOrElse(0);
    }
    
    /**
     * 函数式编程示例：组合多个函数
     */
    @Test
    void testFunctionComposition() {
        Function<String, Try<Integer>> parse = s -> Try.of(() -> Integer.parseInt(s));
        Function<Integer, Try<String>> toString = i -> Try.of(() -> String.valueOf(i));
        
        // 组合函数
        Function<String, Try<String>> composed = parse.andThen(tryInt -> 
            tryInt.flatMap(toString));
        
        Try<String> result = composed.apply("123");
        
        assertTrue(result.isSuccess());
        assertEquals("123", result.get());
    }
} 