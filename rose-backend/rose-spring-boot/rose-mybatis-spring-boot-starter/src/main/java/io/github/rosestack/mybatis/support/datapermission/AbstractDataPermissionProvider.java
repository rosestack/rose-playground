package io.github.rosestack.mybatis.support.datapermission;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 用户数据权限提供者
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractDataPermissionProvider implements DataPermissionProvider {
    @Override
    public List<String> getPermissionValues(String field) {
        DataScope dataScope = getDataScope();
        List<String> permissionValues = getPermissionValues(dataScope);
        log.info("获取用户数据权限范围 {} 对应的数据权限值: {}", dataScope, permissionValues);
        return permissionValues;
    }

    protected abstract DataScope getDataScope();

    public List<String> getPermissionValues(DataScope scope) {
        switch (scope) {
            case SELF:
                return getSelfPermissionValues();
            case PARENT:
                return getParentPermissionValues();
            case PARENT_AND_CHILD:
                return getParentChildPermissionValues();
            case PARENT_PARENT:
                return getParentParentPermissionValues();
            case PARENT_PARENT_AND_CHILD:
                return getParentParentChildPermissionValues();
            case CUSTOM:
                return getCustomPermissionValues();
            case ALL:
                return Collections.emptyList(); // 全部数据，不限制
            default:
                log.warn("不支持的数据权限范围: {}", scope);
                return Collections.emptyList();
        }
    }

    protected abstract List<String> getSelfPermissionValues();

    protected abstract List<String> getParentPermissionValues();

    protected abstract List<String> getParentChildPermissionValues();

    protected abstract List<String> getParentParentChildPermissionValues();

    protected abstract List<String> getParentParentPermissionValues();

    protected abstract List<String> getCustomPermissionValues();
}
