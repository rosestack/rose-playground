package io.github.rosestack.spring.boot.redis.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 *
 * <p>用于方法级别的分布式锁控制。支持动态锁名称、超时时间、等待时间等配置。 可以通过 SpEL 表达式动态生成锁名称。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {

	/**
	 * 锁名称
	 *
	 * <p>支持 SpEL 表达式，可以使用方法参数、返回值等。 例如：'user:' + #userId 或 'order:' + #order.id
	 *
	 * @return 锁名称
	 */
	String value();

	/**
	 * 锁名称（别名）
	 *
	 * <p>与 value() 作用相同，提供更语义化的属性名。
	 *
	 * @return 锁名称
	 */
	String name() default "";

	/**
	 * 等待获取锁的时间，单位毫秒
	 *
	 * <p>如果在指定时间内无法获取锁，则抛出异常。 设置为 -1 表示不等待，立即返回。 设置为 0 表示无限等待。
	 *
	 * @return 等待时间
	 */
	long waitTime() default 5000L;

	/**
	 * 锁的持有时间，单位毫秒
	 *
	 * <p>锁的最大持有时间，超过此时间锁会自动释放。 设置为 -1 表示使用默认配置。
	 *
	 * @return 持有时间
	 */
	long leaseTime() default -1L;

	/**
	 * 是否自动续期
	 *
	 * <p>如果启用自动续期，锁会在即将过期时自动延长持有时间。
	 *
	 * @return 是否自动续期
	 */
	boolean autoRenewal() default true;

	/**
	 * 获取锁失败时的处理策略
	 *
	 * @return 失败策略
	 */
	FailStrategy failStrategy() default FailStrategy.EXCEPTION;

	/**
	 * 获取锁失败时的自定义异常类
	 *
	 * <p>当 failStrategy 为 CUSTOM_EXCEPTION 时使用。 异常类必须有一个接受 String 参数的构造函数。
	 *
	 * @return 异常类
	 */
	Class<? extends RuntimeException> customException() default RuntimeException.class;

	/**
	 * 获取锁失败时的错误消息
	 *
	 * @return 错误消息
	 */
	String failMessage() default "获取分布式锁失败";

	/**
	 * 是否在方法执行完成后自动释放锁
	 *
	 * <p>通常应该保持为 true，除非有特殊需求。
	 *
	 * @return 是否自动释放
	 */
	boolean autoUnlock() default true;

	/**
	 * 锁的作用域
	 *
	 * <p>用于区分不同业务场景的锁。
	 *
	 * @return 锁作用域
	 */
	String scope() default "";

	/**
	 * 获取锁失败时的处理策略枚举
	 */
	enum FailStrategy {
		/**
		 * 抛出异常（默认）
		 */
		EXCEPTION,

		/**
		 * 返回 null
		 */
		RETURN_NULL,

		/**
		 * 跳过方法执行，返回默认值
		 */
		SKIP,

		/**
		 * 抛出自定义异常
		 */
		CUSTOM_EXCEPTION
	}
}
