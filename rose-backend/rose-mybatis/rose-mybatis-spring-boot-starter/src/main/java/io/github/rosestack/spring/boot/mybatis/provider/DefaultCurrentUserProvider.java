package io.github.rosestack.spring.boot.mybatis.provider;


import io.github.rosestack.mybatis.provider.CurrentUserProvider;
import io.github.rosestack.spring.util.ServletUtils;

public class DefaultCurrentUserProvider implements CurrentUserProvider {
    @Override
    public String getCurrentUserId() {
        return ServletUtils.getCurrentUserId();
    }
}


