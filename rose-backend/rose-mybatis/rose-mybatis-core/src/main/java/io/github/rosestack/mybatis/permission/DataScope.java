package io.github.rosestack.mybatis.permission;

/**
 * 数据权限范围枚举（核心层）。
 */
public enum DataScope {
	SELF,
	PARENT,
	PARENT_AND_CHILD,
	PARENT_PARENT,
	PARENT_PARENT_AND_CHILD,
	ALL,
	CUSTOM
}
