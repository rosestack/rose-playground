package io.github.rosestack.mybatis.datapermission;

import java.util.List;

/**
 * 数据权限提供者接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface DataPermissionProvider {
    boolean support(String filed);

    /**
     * 获取权限值列表
     * <p>
     * 根据数据权限注解配置，返回当前上下文下的权限值列表。
     * 返回的权限值将用于构建 SQL 的 WHERE 条件。
     * </p>
     */
    List<String> getPermissionValues();
}
