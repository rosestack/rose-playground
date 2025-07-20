package io.github.rose.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 安全用户模型
 * <p>
 * 实现 Spring Security 的 UserDetails 接口，提供完整的用户认证和授权信息。
 * 支持多租户架构、用户状态管理、安全审计等功能。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>多租户支持 - 通过 tenantId 和 customerId 实现租户隔离</li>
 *   <li>用户状态管理 - 支持账号锁定、过期、禁用等状态</li>
 *   <li>安全审计 - 记录登录时间、失败次数等安全信息</li>
 *   <li>权限控制 - 基于 Authority 枚举的细粒度权限控制</li>
 *   <li>会话管理 - 支持会话跟踪和失效</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * SecurityUser user = new SecurityUser();
 * user.setId(1L);
 * user.setUsername("admin");
 * user.setEmail("admin@example.com");
 * user.setAuthority(Authority.SYS_ADMIN);
 * user.setTenantId("ROOT");
 * user.setEnabled(true);
 * }</pre>
 *
 * @author chensoul
 * @since 1.0.0
 * @see UserDetails
 * @see Authority
 * @see UserPrincipal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    private static final long serialVersionUID = -797397440703066079L;

    // ==================== 基本信息 ====================
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 - 用于登录 */
    private String username;
    
    /** 密码 - 加密存储 */
    private String password;
    
    /** 邮箱 - 用于登录和通知 */
    private String email;
    
    /** 手机号 - 用于登录和通知 */
    private String phone;
    
    /** 真实姓名 */
    private String realName;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
    
    /** 用户描述 */
    private String description;

    // ==================== 权限信息 ====================
    
    /** 用户权限 */
    private Authority authority;
    
    /** 租户ID - 多租户支持 */
    private String tenantId;
    
    /** 客户ID - 多租户支持 */
    private String customerId;
    
    /** 用户组ID列表 */
    private Set<String> userGroupIds;
    
    /** 角色ID列表 */
    private Set<String> roleIds;
    
    /** 权限列表 - Spring Security 使用 */
    private Collection<GrantedAuthority> authorities;
    
    /** 用户主体信息 */
    private UserPrincipal userPrincipal;

    // ==================== 状态信息 ====================
    
    /** 是否启用 */
    private boolean enabled = true;
    
    /** 是否锁定 */
    private boolean locked = false;
    
    /** 是否过期 */
    private boolean expired = false;
    
    /** 密码是否过期 */
    private boolean passwordExpired = false;
    
    /** 是否删除 */
    private boolean deleted = false;

    // ==================== 安全信息 ====================
    
    /** 会话ID */
    private String sessionId = UUID.randomUUID().toString();
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    /** 最后登录设备 */
    private String lastLoginDevice;
    
    /** 登录失败次数 */
    private int loginFailureCount = 0;
    
    /** 账号锁定时间 */
    private LocalDateTime lockTime;
    
    /** 密码最后修改时间 */
    private LocalDateTime passwordLastModifiedTime;
    
    /** 密码过期时间 */
    private LocalDateTime passwordExpireTime;
    
    /** 账号过期时间 */
    private LocalDateTime accountExpireTime;
    
    /** 是否启用双因素认证 */
    private boolean mfaEnabled = false;
    
    /** 双因素认证密钥 */
    private String mfaSecret;
    
    /** 是否强制修改密码 */
    private boolean forcePasswordChange = false;

    // ==================== 审计信息 ====================
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 创建人 */
    private String createBy;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 更新人 */
    private String updateBy;
    
    /** 版本号 - 乐观锁 */
    private Long version = 0L;

    // ==================== 扩展信息 ====================
    
    /** 扩展属性 - 存储额外的用户信息 */
    private Map<String, Object> extraProperties = new HashMap<>();

    // ==================== Spring Security 接口实现 ====================

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = Stream.of(this.getAuthority())
                    .map(authority -> new SimpleGrantedAuthority(authority.name()))
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !expired && (accountExpireTime == null || accountExpireTime.isAfter(LocalDateTime.now()));
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked && (lockTime == null || lockTime.plusMinutes(30).isBefore(LocalDateTime.now()));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !passwordExpired && (passwordExpireTime == null || passwordExpireTime.isAfter(LocalDateTime.now()));
    }

    @Override
    public boolean isEnabled() {
        return enabled && !deleted;
    }

    // ==================== 业务方法 ====================

    /**
     * 检查用户是否有指定权限
     *
     * @param requiredAuthority 所需权限
     * @return 是否有权限
     */
    public boolean hasAuthority(Authority requiredAuthority) {
        return this.authority != null && this.authority.equals(requiredAuthority);
    }

    /**
     * 检查用户是否为系统管理员
     *
     * @return 是否为系统管理员
     */
    public boolean isSystemAdmin() {
        return hasAuthority(Authority.SYS_ADMIN);
    }

    /**
     * 检查用户是否为租户管理员
     *
     * @return 是否为租户管理员
     */
    public boolean isTenantAdmin() {
        return hasAuthority(Authority.TENANT_ADMIN);
    }

    /**
     * 检查用户是否为客户用户
     *
     * @return 是否为客户用户
     */
    public boolean isCustomerUser() {
        return hasAuthority(Authority.CUSTOMER_USER);
    }

    /**
     * 检查用户是否可以访问指定租户的资源
     *
     * @param targetTenantId 目标租户ID
     * @return 是否可以访问
     */
    public boolean canAccessTenant(String targetTenantId) {
        if (isSystemAdmin()) {
            return true; // 系统管理员可以访问所有租户
        }
        return Objects.equals(this.tenantId, targetTenantId);
    }

    /**
     * 检查用户是否可以访问指定客户的资源
     *
     * @param targetCustomerId 目标客户ID
     * @return 是否可以访问
     */
    public boolean canAccessCustomer(String targetCustomerId) {
        if (isSystemAdmin() || isTenantAdmin()) {
            return true; // 系统管理员和租户管理员可以访问所有客户
        }
        return Objects.equals(this.customerId, targetCustomerId);
    }

    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailureCount() {
        this.loginFailureCount++;
        if (this.loginFailureCount >= 5) {
            this.locked = true;
            this.lockTime = LocalDateTime.now();
        }
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailureCount() {
        this.loginFailureCount = 0;
        this.locked = false;
        this.lockTime = null;
    }

    /**
     * 更新登录信息
     *
     * @param loginIp 登录IP
     * @param loginDevice 登录设备
     */
    public void updateLoginInfo(String loginIp, String loginDevice) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = loginIp;
        this.lastLoginDevice = loginDevice;
        resetLoginFailureCount();
    }

    /**
     * 设置扩展属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void setExtraProperty(String key, Object value) {
        if (extraProperties == null) {
            extraProperties = new HashMap<>();
        }
        extraProperties.put(key, value);
    }

    /**
     * 获取扩展属性
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object getExtraProperty(String key) {
        return extraProperties != null ? extraProperties.get(key) : null;
    }

    /**
     * 检查密码是否需要修改
     *
     * @param passwordExpireDays 密码过期天数
     * @return 是否需要修改密码
     */
    public boolean isPasswordChangeRequired(int passwordExpireDays) {
        if (forcePasswordChange) {
            return true;
        }
        if (passwordLastModifiedTime == null) {
            return true;
        }
        return passwordLastModifiedTime.plusDays(passwordExpireDays).isBefore(LocalDateTime.now());
    }

    /**
     * 创建用户副本（用于安全上下文）
     *
     * @return 用户副本
     */
    public SecurityUser copy() {
        SecurityUser copy = new SecurityUser();
        copy.setId(this.id);
        copy.setUsername(this.username);
        copy.setEmail(this.email);
        copy.setPhone(this.phone);
        copy.setRealName(this.realName);
        copy.setNickname(this.nickname);
        copy.setAvatar(this.avatar);
        copy.setDescription(this.description);
        copy.setAuthority(this.authority);
        copy.setTenantId(this.tenantId);
        copy.setCustomerId(this.customerId);
        copy.setUserGroupIds(this.userGroupIds);
        copy.setRoleIds(this.roleIds);
        copy.setEnabled(this.enabled);
        copy.setLocked(this.locked);
        copy.setExpired(this.expired);
        copy.setPasswordExpired(this.passwordExpired);
        copy.setDeleted(this.deleted);
        copy.setSessionId(this.sessionId);
        copy.setLastLoginTime(this.lastLoginTime);
        copy.setLastLoginIp(this.lastLoginIp);
        copy.setLastLoginDevice(this.lastLoginDevice);
        copy.setLoginFailureCount(this.loginFailureCount);
        copy.setLockTime(this.lockTime);
        copy.setPasswordLastModifiedTime(this.passwordLastModifiedTime);
        copy.setPasswordExpireTime(this.passwordExpireTime);
        copy.setAccountExpireTime(this.accountExpireTime);
        copy.setMfaEnabled(this.mfaEnabled);
        copy.setMfaSecret(this.mfaSecret);
        copy.setForcePasswordChange(this.forcePasswordChange);
        copy.setCreateTime(this.createTime);
        copy.setCreateBy(this.createBy);
        copy.setUpdateTime(this.updateTime);
        copy.setUpdateBy(this.updateBy);
        copy.setVersion(this.version);
        copy.setUserPrincipal(this.userPrincipal);
        // 不复制密码和扩展属性，保证安全性
        return copy;
    }

    @Override
    public String toString() {
        return "SecurityUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", authority=" + authority +
                ", tenantId='" + tenantId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", enabled=" + enabled +
                ", locked=" + locked +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
