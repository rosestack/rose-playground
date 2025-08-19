package com.company.usermodulith.user.internal;

/**
 * 用户状态枚举
 *
 * @author Chen Soul
 * @since 1.0.0
 */
public enum UserStatus {

	/**
	 * 激活状态
	 */
	ACTIVE("ACTIVE", "激活"),

	/**
	 * 未激活状态
	 */
	INACTIVE("INACTIVE", "未激活"),

	/**
	 * 锁定状态
	 */
	LOCKED("LOCKED", "锁定"),

	/**
	 * 禁用状态
	 */
	DISABLED("DISABLED", "禁用");

	private final String code;
	private final String description;

	UserStatus(String code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * 根据代码获取状态枚举
	 *
	 * @param code 状态代码
	 * @return 状态枚举，如果不存在返回 null
	 */
	public static UserStatus fromCode(String code) {
		if (code == null) {
			return null;
		}
		for (UserStatus status : values()) {
			if (status.code.equals(code)) {
				return status;
			}
		}
		return null;
	}

	/**
	 * 检查是否为有效状态
	 *
	 * @param code 状态代码
	 * @return 是否为有效状态
	 */
	public static boolean isValid(String code) {
		return fromCode(code) != null;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
