package io.github.rosestack.spring.boot.security;

/**
 * 审计事件类型枚举
 *
 * <p>定义系统中所有可审计的安全事件类型，包括：
 * <ul>
 *   <li>认证事件：登录、登出、密码相关</li>
 *   <li>授权事件：权限检查、角色变更</li>
 *   <li>会话事件：会话创建、过期、踢出</li>
 *   <li>Token事件：Token创建、刷新、撤销</li>
 *   <li>账户事件：锁定、解锁、状态变更</li>
 *   <li>多因子认证事件：启用、禁用、验证</li>
 * </ul>
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
public enum AuditEventType {

    // ========== 认证事件 ==========

    /**
     * 登录成功
     */
    LOGIN_SUCCESS("认证", "用户登录成功"),

    /**
     * 登录失败
     */
    LOGIN_FAILURE("认证", "用户登录失败"),

    /**
     * 用户登出
     */
    LOGOUT("认证", "用户主动登出"),

    /**
     * 全部设备登出
     */
    LOGOUT_ALL("认证", "用户全部设备登出"),

    /**
     * 密码修改
     */
    PASSWORD_CHANGED("认证", "用户密码已修改"),

    /**
     * 密码重置
     */
    PASSWORD_RESET("认证", "用户密码已重置"),

    // ========== 会话事件 ==========

    /**
     * 会话创建
     */
    SESSION_CREATED("会话", "用户会话已创建"),

    /**
     * 会话过期
     */
    SESSION_EXPIRED("会话", "用户会话已过期"),

    /**
     * 会话被踢出
     */
    SESSION_KICKED_OUT("会话", "用户会话被管理员踢出"),

    /**
     * 超过最大会话数，最早会话被回收
     */
    SESSION_EVICTED("会话", "最早会话因达到上限被回收"),

    // ========== Token事件 ==========

    /**
     * Token创建
     */
    TOKEN_CREATED("Token", "访问令牌已创建"),

    /**
     * Token刷新
     */
    TOKEN_REFRESHED("Token", "访问令牌已刷新"),

    /**
     * Token撤销
     */
    TOKEN_REVOKED("Token", "访问令牌已撤销"),

    /**
     * Token过期
     */
    TOKEN_EXPIRED("Token", "访问令牌已过期"),

    /**
     * Token验证失败
     */
    TOKEN_VALIDATION_FAILED("Token", "访问令牌验证失败"),

    // ========== 账户事件 ==========

    /**
     * 账户锁定
     */
    ACCOUNT_LOCKED("账户", "用户账户已锁定"),

    /**
     * 账户解锁
     */
    ACCOUNT_UNLOCKED("账户", "用户账户已解锁"),

    /**
     * 账户禁用
     */
    ACCOUNT_DISABLED("账户", "用户账户已禁用"),

    /**
     * 账户启用
     */
    ACCOUNT_ENABLED("账户", "用户账户已启用"),

    /**
     * 登录尝试过多
     */
    LOGIN_ATTEMPTS_EXCEEDED("账户", "登录失败次数超过限制"),

    // ========== 多因子认证事件 ==========

    /**
     * MFA启用
     */
    MFA_ENABLED("多因子认证", "多因子认证已启用"),

    /**
     * MFA禁用
     */
    MFA_DISABLED("多因子认证", "多因子认证已禁用"),

    /**
     * MFA验证成功
     */
    MFA_VERIFICATION_SUCCESS("多因子认证", "多因子认证验证成功"),

    /**
     * MFA验证失败
     */
    MFA_VERIFICATION_FAILED("多因子认证", "多因子认证验证失败"),

    /**
     * MFA挑战生成
     */
    MFA_CHALLENGE_GENERATED("多因子认证", "多因子认证挑战已生成"),

    // ========== 授权事件 ==========

    /**
     * 权限检查通过
     */
    AUTHORIZATION_SUCCESS("授权", "权限检查通过"),

    /**
     * 权限检查失败
     */
    AUTHORIZATION_FAILED("授权", "权限检查失败"),

    /**
     * 角色变更
     */
    ROLE_CHANGED("授权", "用户角色已变更"),

    // ========== 安全事件 ==========

    /**
     * 可疑活动检测
     */
    SUSPICIOUS_ACTIVITY("安全", "检测到可疑活动"),

    /**
     * 验证码验证失败
     */
    CAPTCHA_FAILED("安全", "验证码验证失败"),

    /**
     * 异常登录位置
     */
    UNUSUAL_LOCATION("安全", "检测到异常登录位置"),

    /**
     * 安全策略变更
     */
    SECURITY_POLICY_CHANGED("安全", "安全策略已变更");

    /**
     * 事件分类
     */
    private final String category;

    /**
     * 事件描述
     */
    private final String description;

    AuditEventType(String category, String description) {
        this.category = category;
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取事件代码（枚举名称）
     */
    public String getCode() {
        return this.name();
    }

    /**
     * 判断是否为成功事件
     */
    public boolean isSuccessEvent() {
        return this == LOGIN_SUCCESS
                || this == LOGOUT
                || this == LOGOUT_ALL
                || this == PASSWORD_CHANGED
                || this == PASSWORD_RESET
                || this == SESSION_CREATED
                || this == TOKEN_CREATED
                || this == TOKEN_REFRESHED
                || this == ACCOUNT_UNLOCKED
                || this == ACCOUNT_ENABLED
                || this == MFA_ENABLED
                || this == MFA_VERIFICATION_SUCCESS
                || this == AUTHORIZATION_SUCCESS
                || this == ROLE_CHANGED;
    }

    /**
     * 判断是否为失败事件
     */
    public boolean isFailureEvent() {
        return this == LOGIN_FAILURE
                || this == TOKEN_VALIDATION_FAILED
                || this == AUTHORIZATION_FAILED
                || this == MFA_VERIFICATION_FAILED
                || this == CAPTCHA_FAILED
                || this == LOGIN_ATTEMPTS_EXCEEDED;
    }

    /**
     * 判断是否为安全相关事件
     */
    public boolean isSecurityEvent() {
        return this == SUSPICIOUS_ACTIVITY
                || this == UNUSUAL_LOCATION
                || this == LOGIN_ATTEMPTS_EXCEEDED
                || this == ACCOUNT_LOCKED
                || this == SESSION_KICKED_OUT
                || this == TOKEN_VALIDATION_FAILED;
    }
}
