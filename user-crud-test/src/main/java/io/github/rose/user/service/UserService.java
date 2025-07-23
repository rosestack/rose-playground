package io.github.rose.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rose.user.dto.UserCreateRequest;
import io.github.rose.user.dto.UserPageRequest;
import io.github.rose.user.dto.UserResponse;
import io.github.rose.user.dto.UserUpdateRequest;
import io.github.rose.user.entity.User;

/**
 * 用户服务接口
 *
 * @author Chen Soul
 * @since 1.0.0
 */
public interface UserService {
    
    /**
     * 根据ID获取用户
     */
    UserResponse getUserById(Long id);
    
    /**
     * 创建用户
     */
    UserResponse createUser(UserCreateRequest request);
    
    /**
     * 更新用户
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 分页查询用户
     */
    IPage<UserResponse> pageUsers(Page<User> page, UserPageRequest request);
}