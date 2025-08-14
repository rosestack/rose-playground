package io.github.rosestack.spring.boot.security.account.impl;

import io.github.rosestack.spring.boot.security.account.CaptchaService;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class NoopCaptchaService implements CaptchaService {
    @Override
    public boolean validate(String scene, String identity, String code) {
        return true;
    }
}
