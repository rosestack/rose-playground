package com.company.usermodulith.user;

import com.company.usermodulith.shared.model.PageRequest;
import com.company.usermodulith.shared.model.PageResponse;

import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 用户模块的公开 API，对外提供服务
 * 参考 Spring Modulith 设计模式
 * 不暴露任何技术实现细节
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 用户响应
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户响应
     */
    UserResponse getUserById(Long id);

    /**
     * 更新用户
     *
     * @param id      用户ID
     * @param request 更新用户请求
     * @return 用户响应
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 分页查询用户
     *
     * @param pageRequest 分页请求
     * @param userQuery   查询请求
     * @return 用户分页响应
     */
    PageResponse<UserResponse> pageUsers(PageRequest pageRequest, UserQuery userQuery);

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    List<UserResponse> getAllUsers();
}