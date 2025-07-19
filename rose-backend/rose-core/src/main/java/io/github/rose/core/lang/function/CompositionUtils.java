package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedFunction;
import io.github.rose.core.lang.function.checked.CheckedBiFunction;
import io.github.rose.core.lang.function.checked.CheckedConsumer;
import io.github.rose.core.lang.function.checked.CheckedBiConsumer;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.BiPredicate;

/**
 * 函数组合工具类
 * 提供函数组合相关的功能方法
 * 
 * @author rose
 */
public final class CompositionUtils {
    
    private CompositionUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 标准函数组合 ====================
    
    /**
     * 函数组合：f ∘ g = f(g(x))
     * 
     * @param f 外层函数
     * @param g 内层函数
     * @param <T> 输入类型
     * @param <U> 中间类型
     * @param <R> 输出类型
     * @return 组合后的函数
     */
    public static <T, U, R> Function<T, R> compose(Function<U, R> f, Function<T, U> g) {
        Objects.requireNonNull(f, "f cannot be null");
        Objects.requireNonNull(g, "g cannot be null");
        return f.compose(g);
    }
    
    /**
     * 函数链式组合：f.andThen(g) = g(f(x))
     * 
     * @param f 内层函数
     * @param g 外层函数
     * @param <T> 输入类型
     * @param <U> 中间类型
     * @param <R> 输出类型
     * @return 组合后的函数
     */
    public static <T, U, R> Function<T, R> andThen(Function<T, U> f, Function<U, R> g) {
        Objects.requireNonNull(f, "f cannot be null");
        Objects.requireNonNull(g, "g cannot be null");
        return f.andThen(g);
    }
    
    // ==================== 受检函数组合 ====================
    
    /**
     * 受检函数组合：f ∘ g = f(g(x))
     * 
     * @param f 外层函数
     * @param g 内层函数
     * @param <T> 输入类型
     * @param <U> 中间类型
     * @param <R> 输出类型
     * @return 组合后的受检函数
     */
    public static <T, U, R> CheckedFunction<T, R> compose(CheckedFunction<U, R> f, CheckedFunction<T, U> g) {
        Objects.requireNonNull(f, "f cannot be null");
        Objects.requireNonNull(g, "g cannot be null");
        return t -> f.apply(g.apply(t));
    }
    
    /**
     * 受检函数链式组合：f.andThen(g) = g(f(x))
     * 
     * @param f 内层函数
     * @param g 外层函数
     * @param <T> 输入类型
     * @param <U> 中间类型
     * @param <R> 输出类型
     * @return 组合后的受检函数
     */
    public static <T, U, R> CheckedFunction<T, R> andThen(CheckedFunction<T, U> f, CheckedFunction<U, R> g) {
        Objects.requireNonNull(f, "f cannot be null");
        Objects.requireNonNull(g, "g cannot be null");
        return t -> g.apply(f.apply(t));
    }
    
    // ==================== 消费者组合 ====================
    
    /**
     * 消费者组合：先执行 first，再执行 second
     * 
     * @param first 第一个消费者
     * @param second 第二个消费者
     * @param <T> 输入类型
     * @return 组合后的消费者
     */
    public static <T> Consumer<T> andThen(Consumer<T> first, Consumer<T> second) {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
        return first.andThen(second);
    }
    
    /**
     * 受检消费者组合：先执行 first，再执行 second
     * 
     * @param first 第一个受检消费者
     * @param second 第二个受检消费者
     * @param <T> 输入类型
     * @return 组合后的受检消费者
     */
    public static <T> CheckedConsumer<T> andThen(CheckedConsumer<T> first, CheckedConsumer<T> second) {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
        return first.andThen(second);
    }
    
    /**
     * 双参数消费者组合：先执行 first，再执行 second
     * 
     * @param first 第一个双参数消费者
     * @param second 第二个双参数消费者
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @return 组合后的双参数消费者
     */
    public static <T, U> BiConsumer<T, U> andThen(BiConsumer<T, U> first, BiConsumer<T, U> second) {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
        return first.andThen(second);
    }
    
    /**
     * 受检双参数消费者组合：先执行 first，再执行 second
     * 
     * @param first 第一个受检双参数消费者
     * @param second 第二个受检双参数消费者
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @return 组合后的受检双参数消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> andThen(CheckedBiConsumer<T, U> first, CheckedBiConsumer<T, U> second) {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
        return first.andThen(second);
    }
    
    // ==================== 谓词组合 ====================
    
    /**
     * 谓词与组合：p1 && p2
     * 
     * @param p1 第一个谓词
     * @param p2 第二个谓词
     * @param <T> 输入类型
     * @return 组合后的谓词
     */
    public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2) {
        Objects.requireNonNull(p1, "p1 cannot be null");
        Objects.requireNonNull(p2, "p2 cannot be null");
        return p1.and(p2);
    }
    
    /**
     * 谓词或组合：p1 || p2
     * 
     * @param p1 第一个谓词
     * @param p2 第二个谓词
     * @param <T> 输入类型
     * @return 组合后的谓词
     */
    public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2) {
        Objects.requireNonNull(p1, "p1 cannot be null");
        Objects.requireNonNull(p2, "p2 cannot be null");
        return p1.or(p2);
    }
    
    /**
     * 谓词非组合：!p
     * 
     * @param p 谓词
     * @param <T> 输入类型
     * @return 组合后的谓词
     */
    public static <T> Predicate<T> not(Predicate<T> p) {
        Objects.requireNonNull(p, "p cannot be null");
        return p.negate();
    }
    
    /**
     * 双参数谓词与组合：p1 && p2
     * 
     * @param p1 第一个双参数谓词
     * @param p2 第二个双参数谓词
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @return 组合后的双参数谓词
     */
    public static <T, U> BiPredicate<T, U> and(BiPredicate<T, U> p1, BiPredicate<T, U> p2) {
        Objects.requireNonNull(p1, "p1 cannot be null");
        Objects.requireNonNull(p2, "p2 cannot be null");
        return p1.and(p2);
    }
    
    /**
     * 双参数谓词或组合：p1 || p2
     * 
     * @param p1 第一个双参数谓词
     * @param p2 第二个双参数谓词
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @return 组合后的双参数谓词
     */
    public static <T, U> BiPredicate<T, U> or(BiPredicate<T, U> p1, BiPredicate<T, U> p2) {
        Objects.requireNonNull(p1, "p1 cannot be null");
        Objects.requireNonNull(p2, "p2 cannot be null");
        return p1.or(p2);
    }
    
    /**
     * 双参数谓词非组合：!p
     * 
     * @param p 双参数谓词
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @return 组合后的双参数谓词
     */
    public static <T, U> BiPredicate<T, U> not(BiPredicate<T, U> p) {
        Objects.requireNonNull(p, "p cannot be null");
        return p.negate();
    }
    
    // ==================== 柯里化 ====================
    
    /**
     * 柯里化双参数函数
     * 
     * @param function 双参数函数
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @param <R> 输出类型
     * @return 柯里化后的函数
     */
    public static <T, U, R> Function<T, Function<U, R>> curry(BiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return t -> u -> function.apply(t, u);
    }
    
    /**
     * 柯里化受检双参数函数
     * 
     * @param function 受检双参数函数
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @param <R> 输出类型
     * @return 柯里化后的受检函数
     */
    public static <T, U, R> CheckedFunction<T, CheckedFunction<U, R>> curry(CheckedBiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return function.curried();
    }
    
    /**
     * 反柯里化函数
     * 
     * @param function 柯里化的函数
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @param <R> 输出类型
     * @return 双参数函数
     */
    public static <T, U, R> BiFunction<T, U, R> uncurry(Function<T, Function<U, R>> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return (t, u) -> function.apply(t).apply(u);
    }
    
    // ==================== 部分应用 ====================
    
    /**
     * 部分应用双参数函数的第一个参数
     * 
     * @param function 双参数函数
     * @param t 第一个参数值
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @param <R> 输出类型
     * @return 单参数函数
     */
    public static <T, U, R> Function<U, R> partial(BiFunction<T, U, R> function, T t) {
        Objects.requireNonNull(function, "function cannot be null");
        return u -> function.apply(t, u);
    }
    
    /**
     * 部分应用受检双参数函数的第一个参数
     * 
     * @param function 受检双参数函数
     * @param t 第一个参数值
     * @param <T> 第一个输入类型
     * @param <U> 第二个输入类型
     * @param <R> 输出类型
     * @return 受检单参数函数
     */
    public static <T, U, R> CheckedFunction<U, R> partial(CheckedBiFunction<T, U, R> function, T t) {
        Objects.requireNonNull(function, "function cannot be null");
        return function.applyFirst(t);
    }
}
