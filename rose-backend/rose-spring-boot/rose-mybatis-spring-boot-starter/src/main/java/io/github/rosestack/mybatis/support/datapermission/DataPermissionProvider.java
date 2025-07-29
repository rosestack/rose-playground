package io.github.rosestack.mybatis.support.datapermission;

import java.util.List;

/**
 * 数据权限提供者接口
 * <p>
 * 定义数据权限值的获取规范，每个提供者负责特定字段的权限控制：
 * - 用户权限：user_id、creator_id、owner_id 等
 * - 门店权限：store_id、shop_id、branch_id 等
 * - 部门权限：dept_id、department_id 等
 * - 组织权限：org_id、company_id 等
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface DataPermissionProvider {

    String getSupportedField();

    /**
     * 获取权限值列表
     * <p>
     * 根据当前上下文（用户、角色、权限等）返回权限值列表。
     * 权限范围由提供者内部根据业务逻辑决定，如：
     * - 用户权限：返回当前用户ID
     * - 门店权限：返回当前用户可访问的门店ID列表
     * - 部门权限：返回当前用户部门及下级部门ID列表
     * </p>
     *
     * @param field 权限字段名
     * @return 权限值列表，空列表表示无权限限制
     */
    List<String> getPermissionValues(String field);

    /**
     * 获取权限提供者的优先级
     * <p>
     * 当多个权限提供者都支持同一字段时，优先级高的提供者将被使用。
     * 数值越小优先级越高。
     * </p>
     *
     * @return 优先级，默认为 100
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取权限提供者的描述信息
     *
     * @return 描述信息
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * 权限提供者是否启用
     *
     * @return true 表示启用，false 表示禁用
     */
    default boolean isEnabled() {
        return true;
    }
}
