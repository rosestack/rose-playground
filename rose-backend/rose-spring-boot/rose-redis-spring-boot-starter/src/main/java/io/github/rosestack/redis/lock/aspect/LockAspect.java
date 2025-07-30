package io.github.rosestack.redis.lock.aspect;

import io.github.rosestack.redis.annotation.Lock;
import io.github.rosestack.redis.exception.LockAcquisitionException;
import io.github.rosestack.redis.exception.LockTimeoutException;
import io.github.rosestack.redis.lock.DistributedLock;
import io.github.rosestack.redis.lock.DistributedLockManager;
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
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 * <p>
 * 处理 @Lock 注解，实现方法级别的分布式锁控制。
 * 支持 SpEL 表达式动态生成锁名称。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class LockAspect {

    private final DistributedLockManager lockManager;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, Lock distributedLock) throws Throwable {
        // 解析锁名称
        String lockName = parseLockName(distributedLock, joinPoint);
        if (!StringUtils.hasText(lockName)) {
            throw new IllegalArgumentException("锁名称不能为空");
        }

        // 构建完整的锁名称
        String fullLockName = buildFullLockName(distributedLock, lockName);

        log.debug("尝试获取分布式锁: {}", fullLockName);

        DistributedLock lock = lockManager.getLock(fullLockName);

        try {
            boolean acquired = acquireLock(lock, distributedLock);

            if (!acquired) {
                return handleLockFailure(distributedLock, fullLockName, joinPoint);
            }

            log.debug("成功获取分布式锁: {}", fullLockName);

            try {
                return joinPoint.proceed();
            } finally {
                if (distributedLock.autoUnlock()) {
                    boolean released = lock.unlock();
                    if (released) {
                        log.debug("成功释放分布式锁: {}", fullLockName);
                    } else {
                        log.warn("释放分布式锁失败: {}", fullLockName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("分布式锁操作异常: {}", fullLockName, e);
            throw e;
        }
    }

    /**
     * 解析锁名称
     */
    private String parseLockName(Lock distributedLock, ProceedingJoinPoint joinPoint) {
        String lockName = StringUtils.hasText(distributedLock.name()) ? 
                distributedLock.name() : distributedLock.value();

        if (!StringUtils.hasText(lockName)) {
            return null;
        }

        // 如果包含 SpEL 表达式，则解析
        if (lockName.contains("#")) {
            return parseSpelExpression(lockName, joinPoint);
        }

        return lockName;
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
     * 构建完整的锁名称
     */
    private String buildFullLockName(Lock distributedLock, String lockName) {
        String scope = distributedLock.scope();
        if (StringUtils.hasText(scope)) {
            return scope + ":" + lockName;
        }
        return lockName;
    }

    /**
     * 获取锁
     */
    private boolean acquireLock(DistributedLock lock, Lock distributedLock) throws InterruptedException {
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        if (leaseTime == -1) {
            // 使用默认租期时间
            if (waitTime <= 0) {
                return lock.tryLock();
            } else {
                return lock.tryLock(waitTime, TimeUnit.MILLISECONDS);
            }
        } else {
            // 使用指定的租期时间
            if (waitTime <= 0) {
                return lock.tryLock(leaseTime, TimeUnit.MILLISECONDS);
            } else {
                return lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * 处理获取锁失败的情况
     */
    private Object handleLockFailure(Lock distributedLock, String lockName, ProceedingJoinPoint joinPoint) throws Throwable {
        String failMessage = distributedLock.failMessage();
        Lock.FailStrategy strategy = distributedLock.failStrategy();

        log.warn("获取分布式锁失败: {}, 策略: {}", lockName, strategy);

        switch (strategy) {
            case EXCEPTION:
                throw new LockTimeoutException(failMessage + ": " + lockName);
                
            case RETURN_NULL:
                return null;
                
            case SKIP:
                return getDefaultReturnValue(joinPoint);
                
            case CUSTOM_EXCEPTION:
                Class<? extends RuntimeException> exceptionClass = distributedLock.customException();
                try {
                    RuntimeException exception = exceptionClass.getConstructor(String.class)
                            .newInstance(failMessage + ": " + lockName);
                    throw exception;
                } catch (Exception e) {
                    log.error("创建自定义异常失败", e);
                    throw new LockAcquisitionException(failMessage + ": " + lockName);
                }
                
            default:
                throw new LockAcquisitionException(failMessage + ": " + lockName);
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
            } else if (returnType == int.class || returnType == long.class || 
                      returnType == short.class || returnType == byte.class) {
                return 0;
            } else if (returnType == float.class || returnType == double.class) {
                return 0.0;
            } else if (returnType == char.class) {
                return '\0';
            }
        }

        return null;
    }
}