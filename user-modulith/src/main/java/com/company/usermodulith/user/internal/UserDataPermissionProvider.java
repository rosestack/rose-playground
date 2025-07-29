package com.company.usermodulith.user.internal;

import io.github.rosestack.mybatis.support.datapermission.AbstractDataPermissionProvider;
import io.github.rosestack.mybatis.support.datapermission.DataScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDataPermissionProvider extends AbstractDataPermissionProvider {
    @Override
    public String getSupportedField() {
        return "id";
    }

    @Override
    public String getDescription() {
        return "用户数据权限提供者";
    }

    @Override
    protected DataScope getDataScope() {
        return DataScope.SELF;
    }

    @Override
    protected List<String> getSelfPermissionValues() {
        return List.of("1");
    }

    @Override
    protected List<String> getParentPermissionValues() {
        return List.of();
    }

    @Override
    protected List<String> getParentChildPermissionValues() {
        return List.of();
    }

    @Override
    protected List<String> getParentParentChildPermissionValues() {
        return List.of();
    }

    @Override
    protected List<String> getParentParentPermissionValues() {
        return List.of();
    }

    @Override
    protected List<String> getCustomPermissionValues() {
        return List.of();
    }
}
