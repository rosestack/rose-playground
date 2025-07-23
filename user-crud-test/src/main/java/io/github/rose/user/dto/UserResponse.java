package io.github.rose.user.dto;

import io.github.rose.user.entity.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应对象
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class UserResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户状态
     */
    private UserStatus status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}