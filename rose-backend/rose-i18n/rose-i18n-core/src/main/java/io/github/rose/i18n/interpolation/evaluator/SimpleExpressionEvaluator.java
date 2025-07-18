package io.github.rose.i18n.interpolation.evaluator;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 高性能简单表达式评估器
 *
 * <p>无依赖的简单表达式评估器，支持基本的属性访问和方法调用。
 * 包含缓存机制和优化的反射调用。</p>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class SimpleExpressionEvaluator implements ExpressionEvaluator {

    private static final Pattern ARRAY_ACCESS_PATTERN = Pattern.compile("(.+?)\\[(.+?)\\]");
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile("(.+?)\\(\\)");
    private static final Pattern NULL_CHECK_PATTERN = Pattern.compile("(.+?)\\s*(==|!=)\\s*null");

    // 方法缓存
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    // 表达式解析缓存
    private final Map<String, ExpressionNode> expressionCache = new ConcurrentHashMap<>();

    private boolean cacheEnabled = true;
    private final Map<String, Function<Object[], Object>> customFunctions = new ConcurrentHashMap<>();

    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            String trimmedExpression = expression.trim();

            // 检查缓存
            if (cacheEnabled) {
                ExpressionNode cachedNode = expressionCache.get(trimmedExpression);
                if (cachedNode != null) {
                    return cachedNode.evaluate(variables, locale);
                }
            }

            // 解析并评估表达式
            ExpressionNode node = parseExpression(trimmedExpression);
            
            // 调试信息
            System.out.println("表达式: " + trimmedExpression + " -> 节点类型: " + node.getClass().getSimpleName());

            // 缓存解析结果
            if (cacheEnabled) {
                expressionCache.put(trimmedExpression, node);
            }

            return node.evaluate(variables, locale);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supports(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        String trimmed = expression.trim();

        // 检查是否包含不支持的语法
        if (trimmed.contains("?") || trimmed.contains(":") ||
                trimmed.contains("+") || trimmed.contains("-") ||
                trimmed.contains("*") || trimmed.contains("/")) {
            return false;
        }

        return true;
    }

    @Override
    public void registerFunction(String name, Function<Object[], Object> function) {
        customFunctions.put(name, function);
    }

    @Override
    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        if (!enabled) {
            methodCache.clear();
            expressionCache.clear();
        }
    }

    /**
     * 解析表达式为语法树节点
     */
    private ExpressionNode parseExpression(String expression) {
        // 空值检查
        if (NULL_CHECK_PATTERN.matcher(expression).matches()) {
            return new NullCheckNode(expression);
        }

        // 方法调用（优先级高于复杂表达式）
        if (METHOD_CALL_PATTERN.matcher(expression).matches()) {
            return new MethodCallNode(expression);
        }

        // 复杂表达式（包含数组访问或嵌套属性访问）
        if (expression.contains("[") || expression.contains(".")) {
            return new ComplexPropertyAccessNode(expression);
        }

        // 简单属性访问
        return new PropertyAccessNode(expression);
    }

    /**
     * 解析复杂表达式，支持嵌套访问
     */
    private ExpressionNode parseComplexExpression(String expression) {
        // 检查是否包含数组访问
        if (expression.contains("[")) {
            return new ComplexPropertyAccessNode(expression);
        }
        
        // 检查是否包含方法调用
        if (expression.contains("(")) {
            return new MethodCallNode(expression);
        }
        
        // 简单属性访问
        return new PropertyAccessNode(expression);
    }

    /**
     * 表达式节点抽象类
     */
    private abstract static class ExpressionNode {
        protected final String expression;

        protected ExpressionNode(String expression) {
            this.expression = expression;
        }

        abstract Object evaluate(Map<String, Object> variables, Locale locale);
    }

    /**
     * 属性访问节点
     */
    private class PropertyAccessNode extends ExpressionNode {
        private final String[] propertyPath;

        public PropertyAccessNode(String expression) {
            super(expression);
            this.propertyPath = expression.split("\\.");
        }

        @Override
        Object evaluate(Map<String, Object> variables, Locale locale) {
            Object current = variables.get(propertyPath[0]);
            if (current == null) {
                return null;
            }

            for (int i = 1; i < propertyPath.length; i++) {
                current = getPropertyValue(current, propertyPath[i]);
                if (current == null) {
                    return null;
                }
            }

            return current;
        }
    }

    /**
     * 方法调用节点
     */
    private class MethodCallNode extends ExpressionNode {
        private final String objectExpression;
        private final String methodName;

        public MethodCallNode(String expression) {
            super(expression);
            int lastDotIndex = expression.lastIndexOf('.');
            this.objectExpression = expression.substring(0, lastDotIndex);
            this.methodName = expression.substring(lastDotIndex + 1, expression.length() - 2);
        }

        @Override
        Object evaluate(Map<String, Object> variables, Locale locale) {
            Object object = new PropertyAccessNode(objectExpression).evaluate(variables, locale);
            System.out.println("MethodCallNode: objectExpression=" + objectExpression + ", object=" + object + ", methodName=" + methodName);
            if (object == null) {
                return null;
            }

            Object result = invokeMethod(object, methodName);
            System.out.println("MethodCallNode: result=" + result);
            return result;
        }
    }

    /**
     * 数组访问节点
     */
    private class ArrayAccessNode extends ExpressionNode {
        private final String objectExpression;
        private final String indexExpression;

        public ArrayAccessNode(String expression) {
            super(expression);
            int bracketIndex = expression.indexOf('[');
            this.objectExpression = expression.substring(0, bracketIndex);
            this.indexExpression = expression.substring(bracketIndex + 1, expression.length() - 1);
        }

        @Override
        Object evaluate(Map<String, Object> variables, Locale locale) {
            Object object = new PropertyAccessNode(objectExpression).evaluate(variables, locale);
            if (object == null) {
                return null;
            }

            Object index = new PropertyAccessNode(indexExpression).evaluate(variables, locale);
            if (index == null) {
                return null;
            }

            return getIndexedValue(object, index);
        }
    }

    /**
     * 复杂属性访问节点，支持嵌套的数组访问和属性访问
     */
    private class ComplexPropertyAccessNode extends ExpressionNode {
        private final String[] parts;

        public ComplexPropertyAccessNode(String expression) {
            super(expression);
            // 分割表达式，处理数组访问、属性访问和方法调用
            this.parts = expression.split("(?=\\[)|(?<=\\])|(?=\\.)|(?=\\()|(?<=\\))");
        }

        @Override
        Object evaluate(Map<String, Object> variables, Locale locale) {
            Object current = variables.get(parts[0]);
            if (current == null) {
                return null;
            }

            for (int i = 1; i < parts.length; i++) {
                String part = parts[i].trim();
                
                if (part.startsWith("[")) {
                    // 数组访问
                    String indexStr = part.substring(1, part.length() - 1);
                    try {
                        int index = Integer.parseInt(indexStr);
                        current = getIndexedValue(current, index);
                    } catch (NumberFormatException e) {
                        // 如果不是数字，尝试作为变量名
                        Object index = variables.get(indexStr);
                        current = getIndexedValue(current, index);
                    }
                } else if (part.startsWith(".")) {
                    // 属性访问
                    String propertyName = part.substring(1);
                    current = getPropertyValue(current, propertyName);
                } else if (part.startsWith("(")) {
                    // 方法调用
                    String methodName = parts[i-1].substring(1); // 获取方法名（去掉前面的点）
                    current = invokeMethod(current, methodName);
                } else if (part.startsWith(".") && !part.equals(".")) {
                    // 属性访问（但不是单独的点）
                    String propertyName = part.substring(1);
                    current = getPropertyValue(current, propertyName);
                }
                
                if (current == null) {
                    return null;
                }
            }

            return current;
        }
    }

    /**
     * 空值检查节点
     */
    private class NullCheckNode extends ExpressionNode {
        private final String propertyPath;
        private final boolean isNullCheck;

        public NullCheckNode(String expression) {
            super(expression);
            if (expression.contains(" == null")) {
                this.propertyPath = expression.replace(" == null", "").trim();
                this.isNullCheck = true;
            } else {
                this.propertyPath = expression.replace(" != null", "").trim();
                this.isNullCheck = false;
            }
        }

        @Override
        Object evaluate(Map<String, Object> variables, Locale locale) {
            Object value = new PropertyAccessNode(propertyPath).evaluate(variables, locale);
            return isNullCheck ? (value == null) : (value != null);
        }
    }

    /**
     * 获取属性值（带缓存）
     */
    private Object getPropertyValue(Object object, String propertyName) {
        if (object == null) {
            return null;
        }

        try {
            // 如果是Map，直接使用get方法
            if (object instanceof Map) {
                return ((Map<?, ?>) object).get(propertyName);
            }

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
     * 调用方法（带缓存）
     */
    private Object invokeMethod(Object object, String methodName) {
        if (object == null) {
            return null;
        }

        try {
            String cacheKey = object.getClass().getName() + "#" + methodName;
            Method method = methodCache.get(cacheKey);

            if (method == null) {
                method = object.getClass().getMethod(methodName);
                methodCache.put(cacheKey, method);
            }

            Object result = method.invoke(object);
            System.out.println("invokeMethod: " + object.getClass().getSimpleName() + "." + methodName + "() = " + result);
            return result;
        } catch (Exception e) {
            System.out.println("invokeMethod: 异常 " + object.getClass().getSimpleName() + "." + methodName + "() - " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取索引值
     */
    private Object getIndexedValue(Object collection, Object index) {
        if (collection == null || index == null) {
            return null;
        }

        try {
            if (collection instanceof Map) {
                return ((Map<?, ?>) collection).get(index);
            }

            if (collection instanceof java.util.List) {
                int listIndex = Integer.parseInt(index.toString());
                java.util.List<?> list = (java.util.List<?>) collection;
                return listIndex >= 0 && listIndex < list.size() ? list.get(listIndex) : null;
            }

            if (collection.getClass().isArray()) {
                int arrayIndex = Integer.parseInt(index.toString());
                Object[] array = (Object[]) collection;
                return arrayIndex >= 0 && arrayIndex < array.length ? array[arrayIndex] : null;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}