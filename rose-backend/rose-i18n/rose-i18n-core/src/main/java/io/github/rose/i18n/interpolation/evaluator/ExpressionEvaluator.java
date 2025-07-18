package io.github.rose.i18n.interpolation.evaluator;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * 表达式评估器接口
 *
 * <p>用于评估表达式语法中的表达式，仅关注核心功能。</p>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface ExpressionEvaluator {
    /**
     * 评估表达式
     *
     * @param expression 表达式内容（不包含${}包装）
     * @param variables  变量映射
     * @param locale     语言环境
     * @return 评估结果，如果评估失败返回null
     */
    Object evaluate(String expression, Map<String, Object> variables, Locale locale);

    // 新增方法
    default boolean supports(String expression) {
        return true;
    }

    default void registerFunction(String name, Function<Object[], Object> function) {
        // 注册自定义函数
    }

    default void setCacheEnabled(boolean enabled) {
        // 启用表达式缓存
    }
}
