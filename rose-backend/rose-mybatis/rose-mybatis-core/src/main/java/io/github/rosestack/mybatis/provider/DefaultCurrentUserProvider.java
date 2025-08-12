package io.github.rosestack.mybatis.provider;

import io.github.rosestack.core.util.ServletUtils;

public class DefaultCurrentUserProvider implements CurrentUserProvider {
    @Override
    public String getCurrentUserId() {
        return ServletUtils.getCurrentUserId();
    }
}


