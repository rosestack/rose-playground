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
     * 操作类型
     * <p>
     * 例如：CREATE, UPDATE, DELETE, QUERY 等
     * </p>
     */
    String type() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 模块名称
     */
    String module() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;
    
    /**
     * 是否记录响应结果
     */
    boolean logResult() default false;
    
    /**
     * 是否记录执行时间
     */
    boolean logExecutionTime() default true;
    
    /**
     * 是否记录用户信息
     */
    boolean logUser() default true;
    
    /**
     * 是否记录 IP 地址
     */
    boolean logIp() default true;
    
    /**
     * 是否异步记录
     */
    boolean async() default true;
} 