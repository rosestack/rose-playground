package io.github.rosestack.spring.boot.security.core.account.impl;

import io.github.rosestack.spring.boot.security.core.account.CaptchaService;

public class NoopCaptchaService implements CaptchaService {
    @Override
    public boolean validate(String scene, String identity, String code) {
        return true;
    }
}
