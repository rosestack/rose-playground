package com.company.usermodulith.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
} 