package io.github.rose.i18n.interpolation.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Spring Expression Language (SpEL) 表达式评估器
 * 
 * 基于Spring的SpEL引擎，提供强大的表达式评估能力。
 * 
 * @author rose
 * @since 1.0.0
 */
@Slf4j
public class SpelExpressionEvaluator implements ExpressionEvaluator {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final Pattern spelPattern = Pattern.compile("\\$\\{([^}]+)\\}");
    
    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // 设置变量
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // 注入locale
            if (locale != null) {
                context.setVariable("locale", locale);
                context.setVariable("language", locale.getLanguage());
                context.setVariable("country", locale.getCountry());
                context.setVariable("displayLanguage", locale.getDisplayLanguage(locale));
                context.setVariable("displayCountry", locale.getDisplayCountry(locale));
            }
            
            // 注入一些常用的工具类
            context.setVariable("T", java.lang.Math.class);
            context.setVariable("Arrays", java.util.Arrays.class);
            context.setVariable("Collections", java.util.Collections.class);
            
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue(context);
            
            if (log.isDebugEnabled()) {
                log.debug("SpEL evaluation: {} -> {}", expression, result);
            }
            
            return result;
            
        } catch (Exception e) {
            log.debug("SpEL evaluation failed for expression: {}", expression, e);
            return null;
        }
    }
    
    @Override
    public boolean supports(String template) {
        return template != null && spelPattern.matcher(template).find();
    }
    
    /**
     * 注册自定义函数
     * 
     * @param name 函数名
     * @param function 函数实现
     */
    public void registerFunction(String name, java.lang.reflect.Method function) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.registerFunction(name, function);
            log.debug("Registered SpEL function: {}", name);
        } catch (Exception e) {
            log.warn("Failed to register SpEL function: {}", name, e);
        }
    }
    
    /**
     * 设置表达式缓存
     * 
     * @param enabled 是否启用缓存
     */
    @Override
    public void setCacheEnabled(boolean enabled) {
        // SpEL解析器本身不支持缓存配置，这里只是记录日志
        log.info("SpEL cache setting requested: {} (not supported by SpEL parser)", enabled);
    }
    
    /**
     * 验证表达式语法
     * 
     * @param expression 表达式
     * @return 是否有效
     */
    public boolean isValidExpression(String expression) {
        try {
            parser.parseExpression(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取表达式的类型信息
     * 
     * @param expression 表达式
     * @return 类型信息
     */
    public Class<?> getExpressionType(String expression) {
        try {
            Expression exp = parser.parseExpression(expression);
            return exp.getValueType();
        } catch (Exception e) {
            log.debug("Failed to get expression type: {}", expression, e);
            return Object.class;
        }
    }
    
    /**
     * 设置表达式解析器配置
     * 
     * @param configurer 配置器
     */
    public void configureParser(java.util.function.Consumer<StandardEvaluationContext> configurer) {
        if (configurer != null) {
            try {
                StandardEvaluationContext context = new StandardEvaluationContext();
                configurer.accept(context);
                log.debug("SpEL parser configured");
            } catch (Exception e) {
                log.warn("Failed to configure SpEL parser", e);
            }
        }
    }
}