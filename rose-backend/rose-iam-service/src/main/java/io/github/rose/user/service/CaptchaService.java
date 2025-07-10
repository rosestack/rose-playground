package io.github.rose.user.service;

public interface CaptchaService {
    boolean validate(String captcha);
}
