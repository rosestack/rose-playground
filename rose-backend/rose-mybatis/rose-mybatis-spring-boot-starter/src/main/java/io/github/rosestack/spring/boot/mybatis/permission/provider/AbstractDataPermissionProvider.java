package io.github.rosestack.spring.boot.mybatis.permission.provider;

import io.github.rosestack.mybatis.permission.DataScope;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户数据权限提供者（核心抽象，无 Spring 依赖）。
 */
public abstract class AbstractDataPermissionProvider implements DataPermissionProvider {
    private static final Logger log = LoggerFactory.getLogger(AbstractDataPermissionProvider.class);

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
