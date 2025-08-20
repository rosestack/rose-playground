package io.github.rosestack.spring.boot.mybatis.permission.provider;

import io.github.rosestack.mybatis.permission.CurrentUserProvider;
import io.github.rosestack.spring.util.ServletUtils;

public class DefaultCurrentUserProvider implements CurrentUserProvider {
    @Override
    public String getCurrentUserId() {
        return ServletUtils.getUserId();
    }
}
