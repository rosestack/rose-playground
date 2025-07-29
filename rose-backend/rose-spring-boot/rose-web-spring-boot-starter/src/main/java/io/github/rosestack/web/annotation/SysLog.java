package io.github.rosestack.web.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 用于标记需要记录操作日志的方法
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {
    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作描述
     */
    String name() default "";

    /**
     * 是否记录响应结果
     */
    boolean logResult() default false;
}