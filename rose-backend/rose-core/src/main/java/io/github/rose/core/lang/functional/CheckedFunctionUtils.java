package io.github.rose.core.lang.functional;

import io.github.rose.core.lang.functional.checked.CheckedFunction;
import io.vavr.control.Try;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.function.Function;

/**
 * CheckedFunction 工具类
 * 提供基于 CheckedFunction 的便捷方法
 * 
 * @author rose
 */
public final class CheckedFunctionUtils {
    
    private CheckedFunctionUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 创建方法 ====================
    
    /**
     * 从受检异常的函数创建 Try
     * 
     * @param function 受检异常的函数
     * @param input 输入值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, R> Try<R> of(CheckedFunction<T, R> function, T input) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> function.apply(input));
    }
    
    /**
     * 从受检异常的函数创建 Option
     * 
     * @param function 受检异常的函数
     * @param input 输入值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Option 结果
     */
    public static <T, R> Option<R> ofOption(CheckedFunction<T, R> function, T input) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            R result = function.apply(input);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            return Option.none();
        }
    }
    
    /**
     * 从受检异常的函数创建 Option（保留异常信息）
     * 
     * @param function 受检异常的函数
     * @param input 输入值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Option 结果
     */
    public static <T, R> Option<R> ofOptionWithException(CheckedFunction<T, R> function, T input) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            R result = function.apply(input);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            // 记录异常但不抛出
            e.printStackTrace();
            return Option.none();
        }
    }
    
    /**
     * 从受检异常的函数创建 Option（带异常处理）
     * 
     * @param function 受检异常的函数
     * @param input 输入值
     * @param exceptionHandler 异常处理函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Option 结果
     */
    public static <T, R> Option<R> ofOption(CheckedFunction<T, R> function, T input,
                                          java.util.function.Function<Exception, R> exceptionHandler) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        try {
            R result = function.apply(input);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            R fallback = exceptionHandler.apply(e);
            return fallback != null ? Option.some(fallback) : Option.none();
        }
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 CheckedFunction 转换为标准 Function
     * 
     * @param checkedFunction 受检异常的函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 标准 Function
     */
    public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> checkedFunction) {
        Objects.requireNonNull(checkedFunction, "checkedFunction cannot be null");
        return checkedFunction.unchecked();
    }
    
    /**
     * 将标准 Function 转换为 CheckedFunction
     * 
     * @param function 标准函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 受检异常的函数
     */
    public static <T, R> CheckedFunction<T, R> checked(Function<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return CheckedFunction.from(function);
    }
    
    /**
     * 链式组合 CheckedFunction
     * 
     * @param function1 第一个函数
     * @param function2 第二个函数
     * @param <T> 输入类型
     * @param <R1> 第一个函数返回类型
     * @param <R2> 第二个函数返回类型
     * @return 链式组合后的函数
     */
    public static <T, R1, R2> CheckedFunction<T, R2> compose(
            CheckedFunction<T, R1> function1,
            CheckedFunction<R1, R2> function2) {
        Objects.requireNonNull(function1, "function1 cannot be null");
        Objects.requireNonNull(function2, "function2 cannot be null");
        
        return input -> {
            R1 intermediate = function1.apply(input);
            return function2.apply(intermediate);
        };
    }
    
    // ==================== 映射方法 ====================
    
    /**
     * 映射集合中的每个元素
     * 
     * @param function 映射函数
     * @param collection 集合
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 映射后的集合
     */
    public static <T, R> java.util.List<R> map(CheckedFunction<T, R> function,
                                             java.util.Collection<T> collection) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");
        
        return collection.stream()
                .map(item -> {
                    try {
                        return function.apply(item);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 映射集合中的每个元素，收集所有结果（包括失败）
     * 
     * @param function 映射函数
     * @param collection 集合
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Try 结果列表
     */
    public static <T, R> java.util.List<Try<R>> mapCollect(CheckedFunction<T, R> function,
                                                          java.util.Collection<T> collection) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");
        
        return collection.stream()
                .map(item -> of(function, item))
                .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== 过滤方法 ====================
    
    /**
     * 过滤并映射集合
     * 
     * @param function 映射函数
     * @param filter 过滤条件
     * @param collection 集合
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 过滤并映射后的集合
     */
    public static <T, R> java.util.List<R> filterMap(CheckedFunction<T, R> function,
                                                   java.util.function.Predicate<T> filter,
                                                   java.util.Collection<T> collection) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(filter, "filter cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");
        
        return collection.stream()
                .filter(filter)
                .map(item -> {
                    try {
                        return function.apply(item);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建身份函数
     * 
     * @param <T> 类型
     * @return 身份函数
     */
    public static <T> CheckedFunction<T, T> identity() {
        return input -> input;
    }
    
    /**
     * 创建常量函数
     * 
     * @param value 常量值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 常量函数
     */
    public static <T, R> CheckedFunction<T, R> constant(R value) {
        return input -> value;
    }
    
    /**
     * 创建异常函数
     * 
     * @param exception 要抛出的异常
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 异常函数
     */
    public static <T, R> CheckedFunction<T, R> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return input -> {
            throw exception;
        };
    }
    
    /**
     * 创建条件函数
     * 
     * @param condition 条件函数
     * @param ifTrue 条件为真时的函数
     * @param ifFalse 条件为假时的函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 条件函数
     */
    public static <T, R> CheckedFunction<T, R> conditional(
            java.util.function.Predicate<T> condition,
            CheckedFunction<T, R> ifTrue,
            CheckedFunction<T, R> ifFalse) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(ifTrue, "ifTrue cannot be null");
        Objects.requireNonNull(ifFalse, "ifFalse cannot be null");
        
        return input -> {
            if (condition.test(input)) {
                return ifTrue.apply(input);
            } else {
                return ifFalse.apply(input);
            }
        };
    }
} 