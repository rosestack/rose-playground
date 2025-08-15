package io.github.rosestack.spring.boot.security.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存用户详情服务
 * 
 * <p>提供基于内存的用户认证，用于测试和开发环境</p>
 */
@Slf4j
public class InMemoryUserDetailsServiceImpl implements UserDetailsService {
    private final Map<String, UserDetails> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public InMemoryUserDetailsServiceImpl() {
        initializeDefaultUsers();
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        log.debug("成功加载用户: {}", username);
        return user;
    }
    
    /**
     * 初始化默认用户
     */
    private void initializeDefaultUsers() {
        // 管理员用户
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .build();
        users.put("admin", admin);
        
        // 普通用户
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        users.put("user", user);
        
        // 测试用户
        UserDetails test = User.builder()
                .username("test")
                .password(passwordEncoder.encode("test123"))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        users.put("test", test);
        
        log.info("初始化默认用户完成: {}", users.keySet());
    }
    
    /**
     * 添加用户
     */
    public void addUser(UserDetails user) {
        users.put(user.getUsername(), user);
        log.info("添加用户: {}", user.getUsername());
    }
    
    /**
     * 删除用户
     */
    public void removeUser(String username) {
        users.remove(username);
        log.info("删除用户: {}", username);
    }
    
    /**
     * 更新密码
     */
    public void updatePassword(String username, String newPassword) {
        UserDetails user = users.get(username);
        if (user != null) {
            UserDetails updatedUser = User.builder()
                    .username(user.getUsername())
                    .password(passwordEncoder.encode(newPassword))
                    .authorities(user.getAuthorities())
                    .build();
            users.put(username, updatedUser);
            log.info("更新用户密码: {}", username);
        }
    }
    
    /**
     * 检查用户是否存在
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    /**
     * 获取所有用户名
     */
    public List<String> getAllUsernames() {
        return List.copyOf(users.keySet());
    }
    
    /**
     * 重置为默认用户
     */
    public void resetUsers() {
        users.clear();
        initializeDefaultUsers();
    }
}
