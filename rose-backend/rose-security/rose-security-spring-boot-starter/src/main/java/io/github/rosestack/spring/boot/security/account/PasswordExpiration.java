package io.github.rosestack.spring.boot.security.account;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码过期信息
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordExpiration {

    /** 用户名 */
    private String username;

    /** 最后一次密码修改时间 */
    private LocalDateTime lastPasswordChange;

    /** 密码过期时间 */
    private LocalDateTime expirationTime;

    public static PasswordExpiration of(String username, int expireDays) {
        LocalDateTime now = LocalDateTime.now();
        return new PasswordExpiration(username, now, now.plusDays(expireDays));
    }

    /**
     * 判断密码是否已过期
     *
     * @return true如果已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * 获取距离过期的天数
     *
     * @return 剩余天数，负数表示已过期
     */
    public long getDaysUntilExpiration() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expirationTime);
    }
}
