package io.github.rosestack.spring.boot.security.core.account;

/**
 * 验证码 SPI 接口
 */
public interface CaptchaService {
    /**
     * 校验验证码是否有效
     * @param scene 场景标识，如 "login"
     * @param identity 身份标识，如用户名或手机号
     * @param code 提交的验证码
     * @return 是否通过
     */
    boolean validate(String scene, String identity, String code);
}
