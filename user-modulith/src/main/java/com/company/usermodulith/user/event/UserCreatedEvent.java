package com.company.usermodulith.user.event;

import lombok.Value;

/**
 * 用户创建事件
 * <p>
 * 按照 Spring Modulith 官方文档的事件设计模式
 * 使用不可变对象表示事件
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Value
public class UserCreatedEvent {

	/**
	 * 用户ID
	 */
	Long userId;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 邮箱
	 */
	String email;

	/**
	 * 创建时间
	 */
	java.time.LocalDateTime createdAt;
}
