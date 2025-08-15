package io.github.rosestack.spring.boot.security.account;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码历史记录
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistory {

    /** 用户名 */
    private String username;

    /** 加密后的密码 */
    private String encodedPassword;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public static PasswordHistory of(String username, String encodedPassword) {
        return new PasswordHistory(username, encodedPassword, LocalDateTime.now());
    }
}
