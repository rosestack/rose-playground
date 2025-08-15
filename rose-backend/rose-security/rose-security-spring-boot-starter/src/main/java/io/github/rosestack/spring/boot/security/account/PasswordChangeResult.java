package io.github.rosestack.spring.boot.security.account;

import lombok.Data;

/**
 * 密码修改结果
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
public class PasswordChangeResult {

    /** 是否成功 */
    private boolean success;

    /** 错误消息 */
    private String errorMessage;

    /** 新的加密密码（成功时返回） */
    private String encodedPassword;

    public static PasswordChangeResult success(String encodedPassword) {
        PasswordChangeResult result = new PasswordChangeResult();
        result.setSuccess(true);
        result.setEncodedPassword(encodedPassword);
        return result;
    }

    public static PasswordChangeResult failure(String errorMessage) {
        PasswordChangeResult result = new PasswordChangeResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
