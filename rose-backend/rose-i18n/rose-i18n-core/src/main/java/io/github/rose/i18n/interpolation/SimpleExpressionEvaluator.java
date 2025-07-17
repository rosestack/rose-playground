package io.github.rose.i18n.interpolation;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 简单表达式评估器
 *
 * <p>无依赖的简单表达式评估器，支持基本的属性访问和方法调用。</p>
 *
 * <p>支持的语法：</p>
 * <ul>
 *   <li>属性访问：user.name, user.profile.email</li>
 *   <li>方法调用：user.getName(), list.size()</li>
 *   <li>数组/集合访问：users[0], map['key']</li>
 *   <li>简单的空值检查：user.name != null</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class SimpleExpressionEvaluator implements ExpressionEvaluator {

    private static final Pattern SIMPLE_PROPERTY_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.\\[\\]'\"()]*$");
    private static final Pattern COMPLEX_EXPRESSION_PATTERN = Pattern.compile(".*[+\\-*/>=<&|!?:].*");

    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            return evaluateExpression(expression.trim(), variables);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 评估表达式
     *
     * @param expression 表达式
     * @param variables  变量映射
     * @return 评估结果
     */
    private Object evaluateExpression(String expression, Map<String, Object> variables) {
        // 处理简单的空值检查
        if (expression.contains(" != null")) {
            String propertyPath = expression.replace(" != null", "").trim();
            Object value = evaluatePropertyPath(propertyPath, variables);
            return value != null;
        }

        if (expression.contains(" == null")) {
            String propertyPath = expression.replace(" == null", "").trim();
            Object value = evaluatePropertyPath(propertyPath, variables);
            return value == null;
        }

        // 处理属性路径
        return evaluatePropertyPath(expression, variables);
    }

    /**
     * 评估属性路径
     *
     * @param propertyPath 属性路径，如 user.name, user.getName(), users[0]
     * @param variables    变量映射
     * @return 属性值
     */
    private Object evaluatePropertyPath(String propertyPath, Map<String, Object> variables) {
        String[] parts = propertyPath.split("\\.");
        Object current = variables.get(parts[0]);

        if (current == null) {
            return null;
        }

        for (int i = 1; i < parts.length; i++) {
            current = evaluatePropertyPart(current, parts[i]);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * 评估属性部分
     *
     * @param object 对象
     * @param part   属性部分，如 name, getName(), [0], ['key']
     * @return 属性值
     */
    private Object evaluatePropertyPart(Object object, String part) {
        if (object == null) {
            return null;
        }

        try {
            // 处理方法调用
            if (part.endsWith("()")) {
                String methodName = part.substring(0, part.length() - 2);
                return invokeMethod(object, methodName);
            }

            // 处理数组/集合访问
            if (part.contains("[") && part.endsWith("]")) {
                String propertyName = part.substring(0, part.indexOf('['));
                String indexStr = part.substring(part.indexOf('[') + 1, part.length() - 1);

                Object collection = propertyName.isEmpty() ? object : getProperty(object, propertyName);
                return getIndexedValue(collection, indexStr);
            }

            // 处理普通属性
            return getProperty(object, part);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取属性值
     *
     * @param object       对象
     * @param propertyName 属性名
     * @return 属性值
     */
    private Object getProperty(Object object, String propertyName) {
        if (object == null) {
            return null;
        }

        try {
            // 尝试getter方法
            String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            Object result = invokeMethod(object, getterName);
            if (result != null) {
                return result;
            }

            // 尝试is方法（布尔类型）
            String isMethodName = "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            return invokeMethod(object, isMethodName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 调用方法
     *
     * @param object     对象
     * @param methodName 方法名
     * @return 方法返回值
     */
    private Object invokeMethod(Object object, String methodName) {
        if (object == null) {
            return null;
        }

        try {
            Method method = object.getClass().getMethod(methodName);
            return method.invoke(object);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取索引值
     *
     * @param collection 集合或数组
     * @param indexStr   索引字符串
     * @return 索引对应的值
     */
    private Object getIndexedValue(Object collection, String indexStr) {
        if (collection == null) {
            return null;
        }

        try {
            // 处理字符串键（Map）
            if (indexStr.startsWith("'") && indexStr.endsWith("'")) {
                String key = indexStr.substring(1, indexStr.length() - 1);
                if (collection instanceof Map) {
                    return ((Map<?, ?>) collection).get(key);
                }
                return null;
            }

            // 处理数字索引
            int index = Integer.parseInt(indexStr);

            if (collection instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) collection;
                return index >= 0 && index < list.size() ? list.get(index) : null;
            }

            if (collection.getClass().isArray()) {
                Object[] array = (Object[]) collection;
                return index >= 0 && index < array.length ? array[index] : null;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
