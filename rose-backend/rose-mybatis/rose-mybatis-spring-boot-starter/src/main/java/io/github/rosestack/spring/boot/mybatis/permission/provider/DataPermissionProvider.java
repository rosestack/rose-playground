package io.github.rosestack.spring.boot.mybatis.permission.provider;

import java.util.List;

/**
 * 数据权限提供者接口（核心层）。
 */
public interface DataPermissionProvider {

	String getSupportedField();

	/**
	 * 根据当前上下文返回权限值列表，空列表表示无权限限制。
	 */
	List<String> getPermissionValues(String field);

	/**
	 * 优先级（越小越高），默认 100
	 */
	default int getPriority() {
		return 100;
	}

	/**
	 * 描述信息
	 */
	default String getDescription() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 是否启用
	 */
	default boolean isEnabled() {
		return true;
	}
}
