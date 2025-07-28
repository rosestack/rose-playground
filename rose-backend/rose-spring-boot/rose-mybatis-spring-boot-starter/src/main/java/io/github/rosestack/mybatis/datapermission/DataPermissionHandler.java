package io.github.rosestack.mybatis.datapermission;

import io.github.rosestack.mybatis.annotation.DataPermission;

import java.util.List;

/**
 * 数据权限处理器接口
 * <p>
 * 定义数据权限处理的标准接口，支持不同的权限控制策略。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface DataPermissionHandler {

    /**
     * 获取当前用户的数据权限值
     *
     * @param dataPermission 数据权限注解
     * @return 权限值列表
     */
    List<String> getPermissionValues(DataPermission dataPermission);

    /**
     * 检查是否支持指定的数据权限类型
     *
     * @param type 数据权限类型
     * @return 是否支持
     */
    boolean supports(DataPermissionType type);

    /**
     * 检查是否需要进行数据权限控制
     *
     * @param dataPermission 数据权限注解
     * @return 是否需要权限控制
     */
    default boolean needPermissionControl(DataPermission dataPermission) {
        return dataPermission!=null &&
               dataPermission.scope() != DataScope.ALL;
    }
}
