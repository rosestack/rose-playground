package io.github.rosestack.spring.annotation;

import java.lang.annotation.*;

/**
 * 忽略响应包装注解
 * <p>
 * 用于标记不需要统一响应包装的方法
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseIgnore {
    
    /**
     * 忽略原因
     */
    String reason() default "";
} 