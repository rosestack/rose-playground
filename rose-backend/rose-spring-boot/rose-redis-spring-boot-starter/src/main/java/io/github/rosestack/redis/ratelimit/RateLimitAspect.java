package io.github.rosestack.redis.ratelimit;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.redis.config.RoseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 限流切面处理器
 * 
 * <p>拦截标注了 @RateLimited 注解的方法，执行限流检查。
 * 支持 SpEL 表达式动态生成限流 key，提供灵活的限流策略。
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Order(100) // 确保在事务切面之前执行
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final RateLimitManager rateLimitManager;
    private final RoseRedisProperties properties;
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * 拦截 @RateLimited 注解的方法
     */
    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        // 检查是否启用限流
        if (!rateLimited.enabled() || !properties.getRateLimit().isEnabled()) {
            return joinPoint.proceed();
        }
        
        try {
            // 构建限流请求
            SendRequest request = buildSendRequest(joinPoint, rateLimited);
            
            // 执行限流检查
            boolean allowed = rateLimitManager.allow(request, rateLimited);
            
            if (!allowed) {
                String message = rateLimited.message();
                String key = parseKey(joinPoint, rateLimited.key());
                String algorithm = rateLimited.algorithm().name();
                
                log.warn("Rate limit exceeded: key={}, algorithm={}, method={}", 
                    key, algorithm, joinPoint.getSignature().toShortString());
                
                throw new RateLimitException(message, key, algorithm);
            }
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功执行
            rateLimitManager.record(request, rateLimited);
            
            return result;
            
        } catch (RateLimitException e) {
            // 重新抛出限流异常
            throw e;
        } catch (Exception e) {
            // 其他异常根据配置决定是否允许通过
            if (rateLimited.failOpen()) {
                log.warn("Rate limit check failed, allowing request to proceed", e);
                return joinPoint.proceed();
            } else {
                log.error("Rate limit check failed, rejecting request", e);
                throw new RateLimitException("Rate limit check failed: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 构建 SendRequest 对象
     */
    private SendRequest buildSendRequest(ProceedingJoinPoint joinPoint, RateLimited rateLimited) {
        String key = parseKey(joinPoint, rateLimited.key());
        String requestId = generateRequestId(joinPoint);
        
        return SendRequest.builder()
            .requestId(requestId)
            .target(key)
            .build();
    }
    
    /**
     * 解析限流 key，支持 SpEL 表达式
     */
    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        if (keyExpression == null || keyExpression.trim().isEmpty()) {
            // 默认使用类名+方法名作为 key
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getDeclaringTypeName() + "." + signature.getName();
        }
        
        // 如果不包含 SpEL 表达式标记，直接返回
        if (!keyExpression.contains("#{")) {
            return keyExpression;
        }
        
        try {
            // 解析 SpEL 表达式
            Expression expression = parser.parseExpression(keyExpression);
            EvaluationContext context = createEvaluationContext(joinPoint);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : keyExpression;
        } catch (Exception e) {
            log.warn("Failed to parse SpEL expression: {}, using original key", keyExpression, e);
            return keyExpression;
        }
    }
    
    /**
     * 创建 SpEL 表达式求值上下文
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();
        
        // 设置方法参数
        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        
        // 设置内置变量
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getSimpleName());
        context.setVariable("target", joinPoint.getTarget());
        context.setVariable("args", args);
        
        return context;
    }
    
    /**
     * 生成请求唯一标识
     */
    private String generateRequestId(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getName() + ":" + System.nanoTime();
    }
}