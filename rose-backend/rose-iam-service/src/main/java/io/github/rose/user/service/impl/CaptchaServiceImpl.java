package io.github.rose.user.service.impl;

import io.github.rose.user.service.CaptchaService;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    @Override
    public boolean validate(String captcha) {
        // mock: 只要不为空都通过
        return captcha != null && !captcha.trim().isEmpty();
    }
}
