package com.company.usermodulith.user.controller;

import com.company.usermodulith.user.*;
import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.core.model.PageRequest;
import io.github.rosestack.core.model.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("创建用户请求: {}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable @Min(1) Long id) {
        log.debug("查询用户，ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("更新用户，ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable @Min(1) Long id) {
        log.info("删除用户，ID: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> pageUsers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            UserQuery userQuery) {
        log.info("分页查询用户，page: {}, size: {}, userQuery: {}", page, size, userQuery);

        // 创建分页请求对象
        PageRequest pageRequest = PageRequest.of(page, size);

        PageResponse<UserResponse> response = userService.pageUsers(pageRequest, userQuery);
        return ApiResponse.success(response);
    }

    @GetMapping("/list")
    public ApiResponse<java.util.List<UserResponse>> getAllUsers() {
        log.info("获取所有用户列表");
        java.util.List<UserResponse> response = userService.getAllUsers();
        return ApiResponse.success(response);
    }
}