package io.github.rosestack.spring.boot.security.account;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 密码验证结果
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
public class PasswordValidationResult {

    /** 验证是否通过 */
    private boolean valid;

    /** 错误消息列表 */
    private List<String> errorMessages = new ArrayList<>();

    public static PasswordValidationResult success() {
        PasswordValidationResult result = new PasswordValidationResult();
        result.setValid(true);
        return result;
    }

    public static PasswordValidationResult failure(String... errorMessages) {
        PasswordValidationResult result = new PasswordValidationResult();
        result.setValid(false);
        for (String errorMessage : errorMessages) {
            result.getErrorMessages().add(errorMessage);
        }
        return result;
    }

    public static PasswordValidationResult failure(List<String> errorMessages) {
        PasswordValidationResult result = new PasswordValidationResult();
        result.setValid(false);
        result.setErrorMessages(new ArrayList<>(errorMessages));
        return result;
    }

    /**
     * 添加错误消息
     *
     * @param message 错误消息
     */
    public void addErrorMessage(String message) {
        this.valid = false;
        this.errorMessages.add(message);
    }

    /**
     * 是否有错误
     *
     * @return true如果有错误
     */
    public boolean hasErrors() {
        return !valid || !errorMessages.isEmpty();
    }
}
