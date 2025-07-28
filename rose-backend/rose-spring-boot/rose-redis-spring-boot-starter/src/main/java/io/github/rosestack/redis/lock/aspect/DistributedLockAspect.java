package io.github.rosestack.redis.lock.aspect;

import io.github.rosestack.redis.annotation.DistributedLock;
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
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 * <p>
 * 处理 @DistributedLock 注解，实现方法级别的分布式锁控制。
 * 支持 SpEL 表达式动态生成锁名称。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Order(1) // 确保在事务切面之前执行
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final DistributedLockManager lockManager;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockName = resolveLockName(distributedLock, joinPoint);
        
        if (!StringUtils.hasText(lockName)) {
            log.warn("锁名称为空，跳过分布式锁控制");
            return joinPoint.proceed();
        }

        // 构建完整的锁名称
        String fullLockName = buildFullLockName(distributedLock, lockName);
        
        log.debug("尝试获取分布式锁: {}", fullLockName);

        io.github.rosestack.redis.lock.DistributedLock lock = lockManager.getLock(fullLockName);
        
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
                    releaseLock(lock, fullLockName);
                }
            }
            
        } catch (Exception e) {
            log.error("分布式锁执行过程中发生异常: {}", fullLockName, e);
            throw e;
        }
    }

    /**
     * 解析锁名称
     */
    private String resolveLockName(DistributedLock distributedLock, ProceedingJoinPoint joinPoint) {
        String lockName = StringUtils.hasText(distributedLock.name()) ? 
                distributedLock.name() : distributedLock.value();
        
        if (!StringUtils.hasText(lockName)) {
            return null;
        }

        // 如果包含 SpEL 表达式，进行解析
        if (lockName.contains("#") || lockName.contains("${")) {
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
            
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("解析 SpEL 表达式失败: {}", expression, e);
            return expression; // 返回原始表达式
        }
    }

    /**
     * 构建完整的锁名称
     */
    private String buildFullLockName(DistributedLock distributedLock, String lockName) {
        StringBuilder fullName = new StringBuilder();
        
        // 添加作用域前缀
        if (StringUtils.hasText(distributedLock.scope())) {
            fullName.append(distributedLock.scope()).append(":");
        }
        
        fullName.append(lockName);
        
        return fullName.toString();
    }

    /**
     * 获取锁
     */
    private boolean acquireLock(io.github.rosestack.redis.lock.DistributedLock lock, DistributedLock distributedLock) 
            throws InterruptedException {
        
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();

        if (waitTime == -1) {
            // 不等待，立即返回
            if (leaseTime == -1) {
                return lock.tryLock();
            } else {
                return lock.tryLock(leaseTime, timeUnit);
            }
        } else if (waitTime == 0) {
            // 无限等待
            if (leaseTime == -1) {
                lock.lock();
            } else {
                lock.lock(leaseTime, timeUnit);
            }
            return true;
        } else {
            // 等待指定时间
            if (leaseTime == -1) {
                // 使用默认租期时间，这里需要一个合理的默认值
                return lock.tryLock(waitTime, 30000L, timeUnit);
            } else {
                return lock.tryLock(waitTime, leaseTime, timeUnit);
            }
        }
    }

    /**
     * 释放锁
     */
    private void releaseLock(io.github.rosestack.redis.lock.DistributedLock lock, String lockName) {
        try {
            boolean released = lock.unlock();
            if (released) {
                log.debug("成功释放分布式锁: {}", lockName);
            } else {
                log.warn("释放分布式锁失败: {}", lockName);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常: {}", lockName, e);
        }
    }

    /**
     * 处理获取锁失败的情况
     */
    private Object handleLockFailure(DistributedLock distributedLock, String lockName, ProceedingJoinPoint joinPoint) 
            throws Throwable {
        
        String failMessage = distributedLock.failMessage() + ": " + lockName;
        
        switch (distributedLock.failStrategy()) {
            case RETURN_NULL:
                log.warn("获取分布式锁失败，返回 null: {}", lockName);
                return null;
                
            case SKIP:
                log.warn("获取分布式锁失败，跳过方法执行: {}", lockName);
                return getDefaultReturnValue(joinPoint);
                
            case CUSTOM_EXCEPTION:
                Class<? extends RuntimeException> exceptionClass = distributedLock.customException();
                if (exceptionClass != RuntimeException.class) {
                    try {
                        RuntimeException exception = exceptionClass.getConstructor(String.class).newInstance(failMessage);
                        throw exception;
                    } catch (Exception e) {
                        log.error("创建自定义异常失败: {}", exceptionClass.getName(), e);
                        throw new RuntimeException(failMessage);
                    }
                }
                // 如果没有指定自定义异常，则抛出默认异常
                
            case EXCEPTION:
            default:
                throw new DistributedLockManager.LockException(failMessage);
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
            if (returnType == boolean.class) return false;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0.0f;
            if (returnType == double.class) return 0.0d;
            if (returnType == char.class) return '\0';
        }
        
        return null;
    }
}