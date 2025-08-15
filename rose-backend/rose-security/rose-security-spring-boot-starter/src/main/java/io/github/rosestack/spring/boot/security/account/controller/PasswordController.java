package io.github.rosestack.spring.boot.security.account.controller;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.account.PasswordChangeResult;
import io.github.rosestack.spring.boot.security.account.PasswordChangeService;
import io.github.rosestack.spring.boot.security.account.PasswordPolicyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 密码管理控制器
 *
 * <p>提供密码修改、密码策略查询等功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "rose.security.account.password",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PasswordController {

    private final PasswordChangeService passwordChangeService;
    private final PasswordPolicyService passwordPolicyService;

    /**
     * 修改密码
     */
    @PostMapping("/change")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ApiResponse.error("用户未登录");
        }

        String username = userDetails.getUsername();
        PasswordChangeResult result =
                passwordChangeService.changePassword(username, request.getOldPassword(), request.getNewPassword());

        if (result.isSuccess()) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(result.getErrorMessage());
        }
    }

    /**
     * 重置密码（管理员功能）
     */
    @PostMapping("/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordChangeResult result =
                passwordChangeService.resetPassword(request.getUsername(), request.getNewPassword());

        if (result.isSuccess()) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(result.getErrorMessage());
        }
    }

    /**
     * 检查密码是否过期
     */
    @GetMapping("/expiration")
    public ApiResponse<Map<String, Object>> checkPasswordExpiration(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ApiResponse.error("用户未登录");
        }

        String username = userDetails.getUsername();
        boolean expired = passwordPolicyService.isPasswordExpired(username);
        long daysUntilExpiration = passwordPolicyService.getPasswordExpirationDays(username);

        Map<String, Object> data = Map.of(
                "expired", expired,
                "daysUntilExpiration", daysUntilExpiration);

        return ApiResponse.success(data);
    }

    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "当前密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }

    /**
     * 重置密码请求
     */
    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
