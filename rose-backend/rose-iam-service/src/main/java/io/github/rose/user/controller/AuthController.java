package io.github.rose.user.controller;

import io.github.rose.common.Result;
import io.github.rose.user.api.AuthApi;
import io.github.rose.user.dto.UserLoginDTO;
import io.github.rose.user.dto.UserRegisterDTO;
import io.github.rose.user.service.AuthService;
import io.github.rose.user.vo.LoginVO;
import io.github.rose.user.vo.RegisterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@Tag(name = "认证模块", description = "用户注册、登录、登出等接口")
@RestController
public class AuthController implements AuthApi {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @Operation(summary = "用户注册", description = "注册新用户，用户名、邮箱、手机号唯一，密码需8-20位字母数字组合")
    public Result<RegisterVO> register(
            @Parameter(description = "注册参数", required = true) @Validated @RequestBody UserRegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @Override
    @Operation(summary = "用户登录", description = "用户登录，返回JWT Token")
    public Result<LoginVO> login(
            @Parameter(description = "登录参数", required = true) @Validated @RequestBody UserLoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Override
    @Operation(summary = "用户登出", description = "用户登出，Token失效")
    public Result<Void> logout() {
        // 实际应获取当前登录用户ID
        authService.logout(null);
        return Result.success();
    }
}
