package io.github.rosestack.spring.boot.redis.ratelimit.aspect;

import io.github.rosestack.spring.boot.redis.annotation.RateLimited;
import io.github.rosestack.spring.boot.redis.config.RoseRedisProperties;
import io.github.rosestack.spring.boot.redis.exception.RateLimitExceededException;
import io.github.rosestack.spring.boot.redis.ratelimit.RateLimitManager;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 限流切面
 *
 * <p>处理 @RateLimited 注解，实现方法级别的限流控制。 支持 SpEL 表达式动态生成限流键名称。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Order(2) // 在分布式锁之后执行
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitManager rateLimitManager;
    private final RoseRedisProperties properties;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        // 检查是否启用限流
        if (!rateLimited.enabled() || !properties.getRateLimit().isEnabled()) {
            return joinPoint.proceed();
        }

        // 解析限流键
        String rateLimitKey = parseRateLimitKey(rateLimited, joinPoint);
        if (!StringUtils.hasText(rateLimitKey)) {
            throw new IllegalArgumentException("限流键不能为空");
        }

        // 构建完整的限流键
        String fullRateLimitKey = buildFullRateLimitKey(rateLimited, rateLimitKey);

        log.debug("尝试获取限流许可: {}", fullRateLimitKey);

        try {
            // 获取限流配置
            RoseRedisProperties.RateLimit.Algorithm algorithm = rateLimited.algorithm();
            int rate = rateLimited.rate() > 0
                    ? rateLimited.rate()
                    : properties.getRateLimit().getDefaultRate();
            int timeWindow = rateLimited.timeWindow() > 0
                    ? rateLimited.timeWindow()
                    : properties.getRateLimit().getDefaultTimeWindow();

            // 尝试获取许可
            boolean acquired = rateLimitManager.tryAcquire(fullRateLimitKey, algorithm, rate, timeWindow);

            if (!acquired) {
                return handleRateLimitExceeded(rateLimited, fullRateLimitKey, joinPoint);
            }

            log.debug("成功获取限流许可: {}", fullRateLimitKey);

            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("限流操作异常: {}", fullRateLimitKey, e);
            // 发生异常时根据配置决定是否继续执行
            if (shouldContinueOnError()) {
                return joinPoint.proceed();
            } else {
                throw e;
            }
        }
    }

    /**
     * 解析限流键名称
     */
    private String parseRateLimitKey(RateLimited rateLimited, ProceedingJoinPoint joinPoint) {
        String rateLimitKey = StringUtils.hasText(rateLimited.key()) ? rateLimited.key() : rateLimited.value();

        if (!StringUtils.hasText(rateLimitKey)) {
            return null;
        }

        // 如果包含 SpEL 表达式，则解析
        if (rateLimitKey.contains("#")) {
            return parseSpelExpression(rateLimitKey, joinPoint);
        }

        return rateLimitKey;
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseSpelExpression(String expression, ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            String[] paramNames = signature.getParameterNames();

            EvaluationContext context = new StandardEvaluationContext();

            // 设置方法参数
            if (paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            // 设置方法信息
            context.setVariable("method", method);
            context.setVariable("target", joinPoint.getTarget());

            Expression expr = parser.parseExpression(expression);
            Object value = expr.getValue(context);

            return value != null ? value.toString() : "";
        } catch (Exception e) {
            log.error("解析 SpEL 表达式失败: {}", expression, e);
            return expression;
        }
    }

    /**
     * 构建完整的限流键名称
     */
    private String buildFullRateLimitKey(RateLimited rateLimited, String rateLimitKey) {
        String scope = rateLimited.scope();
        if (StringUtils.hasText(scope)) {
            return scope + ":" + rateLimitKey;
        }
        return rateLimitKey;
    }

    /**
     * 处理限流超出的情况
     */
    private Object handleRateLimitExceeded(RateLimited rateLimited, String rateLimitKey, ProceedingJoinPoint joinPoint)
            throws Throwable {
        String failMessage = rateLimited.failMessage();
        RateLimited.FailStrategy strategy = rateLimited.failStrategy();

        log.warn("限流超出: {}, 策略: {}", rateLimitKey, strategy);

        switch (strategy) {
            case EXCEPTION:
                throw new RateLimitExceededException(failMessage + ": " + rateLimitKey);

            case RETURN_NULL:
                return null;

            case SKIP:
                return getDefaultReturnValue(joinPoint);

            case CUSTOM_EXCEPTION:
                Class<? extends RuntimeException> exceptionClass = rateLimited.customException();
                try {
                    RuntimeException exception =
                            exceptionClass.getConstructor(String.class).newInstance(failMessage + ": " + rateLimitKey);
                    throw exception;
                } catch (Exception e) {
                    log.error("创建自定义异常失败", e);
                    throw new RateLimitExceededException(failMessage + ": " + rateLimitKey);
                }

            case LOG_AND_CONTINUE:
                log.warn("限流超出但继续执行: {}", rateLimitKey);
                return joinPoint.proceed();

            default:
                throw new RateLimitExceededException(failMessage + ": " + rateLimitKey);
        }
    }

    /**
     * 获取方法的默认返回值
     */
    private Object getDefaultReturnValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (returnType == void.class || returnType == Void.class) {
            return null;
        }

        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) {
                return false;
            } else if (returnType == int.class
                    || returnType == long.class
                    || returnType == short.class
                    || returnType == byte.class) {
                return 0;
            } else if (returnType == float.class || returnType == double.class) {
                return 0.0;
            } else if (returnType == char.class) {
                return '\0';
            }
        }

        return null;
    }

    /**
     * 是否在发生错误时继续执行
     */
    private boolean shouldContinueOnError() {
        // 可以通过配置控制，这里默认为 true，避免限流功能影响业务
        return true;
    }
}
