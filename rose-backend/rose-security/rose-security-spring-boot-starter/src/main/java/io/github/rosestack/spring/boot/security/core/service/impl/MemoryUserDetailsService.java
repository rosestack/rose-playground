package io.github.rosestack.spring.boot.security.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的内存用户详情服务实现
 * 
 * <p>提供基于内存的用户存储，主要用于开发和测试环境。
 * 生产环境建议提供自定义的 UserDetailsService 实现。</p>
 * 
 * <h3>预置测试用户：</h3>
 * <ul>
 *   <li><strong>admin/admin123</strong> - 管理员账户，拥有 ADMIN 和 USER 权限</li>
 *   <li><strong>user/user123</strong> - 普通用户账户，拥有 USER 权限</li>
 *   <li><strong>test/test123</strong> - 测试账户，拥有 USER 权限</li>
 * </ul>
 * 
 * <h3>使用方式：</h3>
 * <pre>{@code
 * // 登录请求示例
 * POST /api/auth/login
 * {
 *   "username": "admin",
 *   "password": "admin123"
 * }
 * }</pre>
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
public class MemoryUserDetailsService implements UserDetailsService {

    /** 内存用户存储 */
    private final Map<String, UserDetails> users = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 初始化默认测试用户
     * 
     * @param passwordEncoder 密码编码器
     */
    public MemoryUserDetailsService(PasswordEncoder passwordEncoder) {
        initializeDefaultUsers(passwordEncoder);
        log.info("InMemoryUserDetailsService 已初始化，包含 {} 个用户", users.size());
        log.info("测试用户列表：admin/admin123 (ADMIN,USER), user/user123 (USER), test/test123 (USER)");
    }

    /**
     * 根据用户名加载用户详情
     * 
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("查找用户: {}", username);
        
        UserDetails user = users.get(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        log.debug("找到用户: {}, 权限: {}", username, user.getAuthorities());
        return user;
    }

    /**
     * 添加新用户
     * 
     * @param username 用户名
     * @param password 原始密码
     * @param authorities 权限列表
     * @param passwordEncoder 密码编码器
     * @return 添加的用户详情
     */
    public UserDetails addUser(String username, String password, List<String> authorities, PasswordEncoder passwordEncoder) {
        if (users.containsKey(username)) {
            log.warn("用户已存在: {}", username);
            return users.get(username);
        }

        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .authorities(authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toArray(SimpleGrantedAuthority[]::new))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        users.put(username, user);
        log.info("添加新用户: {}, 权限: {}", username, authorities);
        return user;
    }

    /**
     * 移除用户
     * 
     * @param username 用户名
     * @return 是否成功移除
     */
    public boolean removeUser(String username) {
        UserDetails removed = users.remove(username);
        if (removed != null) {
            log.info("移除用户: {}", username);
            return true;
        }
        log.warn("要移除的用户不存在: {}", username);
        return false;
    }

    /**
     * 检查用户是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * 获取所有用户名列表
     * 
     * @return 用户名列表
     */
    public List<String> getAllUsernames() {
        return List.copyOf(users.keySet());
    }

    /**
     * 获取用户总数
     * 
     * @return 用户总数
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * 初始化默认测试用户
     * 
     * @param passwordEncoder 密码编码器
     */
    private void initializeDefaultUsers(PasswordEncoder passwordEncoder) {
        // 管理员用户
        users.put("admin", User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .authorities("ROLE_ADMIN", "ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build());

        // 普通用户
        users.put("user", User.withUsername("user")
                .password(passwordEncoder.encode("user123"))
                .authorities("ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build());

        // 测试用户
        users.put("test", User.withUsername("test")
                .password(passwordEncoder.encode("test123"))
                .authorities("ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build());

        log.debug("默认用户初始化完成");
    }

    /**
     * 重置所有用户（仅用于测试）
     * 
     * @param passwordEncoder 密码编码器
     */
    public void resetUsers(PasswordEncoder passwordEncoder) {
        users.clear();
        initializeDefaultUsers(passwordEncoder);
        log.info("用户数据已重置");
    }

    /**
     * 更新用户密码
     * 
     * @param username 用户名
     * @param newPassword 新密码
     * @param passwordEncoder 密码编码器
     * @return 是否更新成功
     */
    public boolean updatePassword(String username, String newPassword, PasswordEncoder passwordEncoder) {
        UserDetails existingUser = users.get(username);
        if (existingUser == null) {
            log.warn("要更新密码的用户不存在: {}", username);
            return false;
        }

        UserDetails updatedUser = User.withUsername(username)
                .password(passwordEncoder.encode(newPassword))
                .authorities(existingUser.getAuthorities())
                .accountExpired(!existingUser.isAccountNonExpired())
                .accountLocked(!existingUser.isAccountNonLocked())
                .credentialsExpired(!existingUser.isCredentialsNonExpired())
                .disabled(!existingUser.isEnabled())
                .build();

        users.put(username, updatedUser);
        log.info("用户 {} 的密码已更新", username);
        return true;
    }
}
