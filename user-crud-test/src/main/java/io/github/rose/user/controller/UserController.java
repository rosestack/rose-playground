package io.github.rose.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rose.user.dto.ApiResponse;
import io.github.rose.user.dto.UserCreateRequest;
import io.github.rose.user.dto.UserPageRequest;
import io.github.rose.user.dto.UserResponse;
import io.github.rose.user.dto.UserUpdateRequest;
import io.github.rose.user.entity.User;
import io.github.rose.user.service.UserService;
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
        log.info("更新用户请求，ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable @Min(1) Long id) {
        log.info("删除用户请求，ID: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success();
    }
    
    @GetMapping
    public ApiResponse<IPage<UserResponse>> pageUsers(Page<User> page, @Valid UserPageRequest request) {
        log.debug("分页查询用户请求，页码：{}，大小：{}", page.getCurrent(), page.getSize());
        IPage<UserResponse> response = userService.pageUsers(page, request);
        return ApiResponse.success(response);
    }
}