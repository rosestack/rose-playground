package io.github.rosestack.spring.boot.security.core.support.impl;

import io.github.rosestack.spring.boot.security.core.support.CaptchaService;

public class NoopCaptchaService implements CaptchaService {
    @Override
    public boolean validate(String scene, String identity, String code) {
        return true;
    }
}
