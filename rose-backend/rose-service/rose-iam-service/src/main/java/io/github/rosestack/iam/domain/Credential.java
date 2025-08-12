package io.github.rosestack.iam.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Credential {
    private Long id;

    private String userId;

    private String password;

    private String activateToken;

    private String resetToken;

    /** 用户密码安全强度等级 */
    private Integer passwordSecurityLevel;

    /** 下次登录要求重置密码 */
    private Boolean resetPasswordOnNextLogin;

    private Boolean resetPasswordOnFirstLogin;

    private LocalDateTime userPasswordExpireTime;

    /** 用户上次密码修改时间 */
    private String passwordLastSetAt;

    /** 状态 */
    private Boolean status;
}
