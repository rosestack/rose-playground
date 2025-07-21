package com.example.ddddemo.user.interfaces.controller;

import com.example.ddddemo.shared.application.dto.ApiResponse;
import com.example.ddddemo.user.application.command.CreateUserCommand;
import com.example.ddddemo.user.application.command.UpdateUserCommand;
import com.example.ddddemo.user.application.command.UpdateUserStatusCommand;
import com.example.ddddemo.user.application.query.UserQuery;
import com.example.ddddemo.user.application.dto.UserDTO;
import com.example.ddddemo.user.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * <p>
 * 提供用户相关的REST API接口
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>用户CRUD操作</li>
 *   <li>RESTful API设计</li>
 *   <li>参数验证</li>
 *   <li>统一响应格式</li>
 * </ul>
 *
 * @author DDD Demo
 * @since 1.0.0
 */
@Tag(name = "用户管理", description = "用户相关的API接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * 创建用户
     *
     * @param command 创建用户命令
     * @return 创建结果
     */
    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public ApiResponse<UserDTO> createUser(@RequestBody CreateUserCommand command) {
        return userApplicationService.createUser(command);
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详细信息")
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return userApplicationService.getUserById(id);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Operation(summary = "根据用户名查询用户", description = "根据用户名查询用户详细信息")
    @GetMapping("/username/{username}")
    public ApiResponse<UserDTO> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable String username) {
        return userApplicationService.getUserByUsername(username);
    }

    /**
     * 分页查询用户列表
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param status 用户状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持按状态和关键词筛选")
    @GetMapping
    public ApiResponse<List<UserDTO>> getUserList(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户状态：0-禁用，1-正常") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        
        UserQuery query = new UserQuery(page, size);
        if (status != null) {
            query.setStatus(status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setKeyword(keyword);
        }
        
        return userApplicationService.getUserList(query);
    }

    /**
     * 更新用户基本信息
     *
     * @param id 用户ID
     * @param command 更新用户命令
     * @return 更新结果
     */
    @Operation(summary = "更新用户基本信息", description = "更新用户的个人信息")
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody UpdateUserCommand command) {
        return userApplicationService.updateUser(id, command);
    }

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param command 更新状态命令
     * @return 更新结果
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/{id}/status")
    public ApiResponse<UserDTO> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody UpdateUserStatusCommand command) {
        return userApplicationService.updateUserStatus(id, command);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return userApplicationService.deleteUser(id);
    }

    /**
     * 统计用户数量
     *
     * @param status 用户状态（可选）
     * @return 用户数量
     */
    @Operation(summary = "统计用户数量", description = "统计用户总数或按状态统计")
    @GetMapping("/count")
    public ApiResponse<Long> countUsers(
            @Parameter(description = "用户状态：0-禁用，1-正常") @RequestParam(required = false) Integer status) {
        return userApplicationService.countUsers(status);
    }
} 