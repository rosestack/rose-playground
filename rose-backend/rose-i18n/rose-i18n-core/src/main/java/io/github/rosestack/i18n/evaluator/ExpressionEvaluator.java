package io.github.rosestack.i18n.evaluator;

import java.util.Locale;
import java.util.Map;

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

	/**
	 * 检查是否支持指定的表达式
	 *
	 * @param expression 表达式
	 * @return 是否支持
	 */
	default boolean supports(String expression) {
		return true;
	}
}
