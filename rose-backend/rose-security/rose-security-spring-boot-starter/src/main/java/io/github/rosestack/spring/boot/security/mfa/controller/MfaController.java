package io.github.rosestack.spring.boot.security.mfa.controller;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.mfa.MfaChallenge;
import io.github.rosestack.spring.boot.security.mfa.MfaContext;
import io.github.rosestack.spring.boot.security.mfa.MfaResult;
import io.github.rosestack.spring.boot.security.mfa.MfaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * MFA管理控制器
 * <p>
 * 提供多因子认证的REST API接口，包括MFA设置、验证、管理等功能。
 * 仅在MFA功能启用时生效。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rose.security.mfa.enabled", havingValue = "true")
public class MfaController {

    private final MfaService mfaService;

    /**
     * 获取用户MFA状态
     *
     * @param authentication 认证信息
     * @return MFA状态信息
     */
    @GetMapping("/status")
    public ApiResponse<MfaStatusResponse> getMfaStatus(Authentication authentication) {
        String userId = authentication.getName();
        log.debug("获取用户MFA状态，用户ID: {}", userId);

        boolean isSetup = mfaService.isUserMfaSetup(userId);
        List<String> setupProviders = mfaService.getUserSetupProviders(userId);
        List<String> availableProviders = mfaService.getAvailableProviders();

        MfaStatusResponse response = MfaStatusResponse.builder()
                .isSetup(isSetup)
                .setupProviders(setupProviders)
                .availableProviders(availableProviders)
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 初始化MFA设置
     *
     * @param providerType 提供商类型
     * @param authentication 认证信息
     * @param request HTTP请求
     * @return MFA挑战信息
     */
    @PostMapping("/setup/{providerType}/init")
    public ApiResponse<MfaChallenge> initMfaSetup(
            @PathVariable String providerType, Authentication authentication, HttpServletRequest request) {
        String userId = authentication.getName();
        log.info("初始化用户MFA设置，用户ID: {}, 提供商: {}", userId, providerType);

        MfaContext context = buildMfaContext(userId, authentication, request);
        MfaChallenge challenge = mfaService.initMfaSetup(userId, providerType, context);

        return ApiResponse.success(challenge);
    }

    /**
     * 完成MFA设置
     *
     * @param providerType 提供商类型
     * @param request 完成设置请求
     * @param authentication 认证信息
     * @param httpRequest HTTP请求
     * @return 设置结果
     */
    @PostMapping("/setup/{providerType}/complete")
    public ApiResponse<MfaResult> completeMfaSetup(
            @PathVariable String providerType,
            @RequestBody @Valid CompleteMfaSetupRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String userId = authentication.getName();
        log.info("完成用户MFA设置，用户ID: {}, 提供商: {}", userId, providerType);

        MfaContext context = buildMfaContext(userId, authentication, httpRequest);

        // 构建挑战信息（简化版本，生产环境应从缓存或数据库获取）
        MfaChallenge challenge = MfaChallenge.builder()
                .challengeId(request.getChallengeId())
                .providerType(providerType)
                .userId(userId)
                .challengeType("setup")
                .build();

        MfaResult result =
                mfaService.completeMfaSetup(userId, providerType, challenge, request.getVerificationCode(), context);

        return ApiResponse.success(result);
    }

    /**
     * 验证MFA
     *
     * @param providerType 提供商类型
     * @param request 验证请求
     * @param authentication 认证信息
     * @param httpRequest HTTP请求
     * @return 验证结果
     */
    @PostMapping("/verify/{providerType}")
    public ApiResponse<MfaResult> verifyMfa(
            @PathVariable String providerType,
            @RequestBody @Valid VerifyMfaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String userId = authentication.getName();
        log.info("验证用户MFA，用户ID: {}, 提供商: {}", userId, providerType);

        MfaContext context = buildMfaContext(userId, authentication, httpRequest);
        MfaResult result = mfaService.verifyMfa(userId, providerType, request.getVerificationCode(), context);

        return ApiResponse.success(result);
    }

    /**
     * 移除MFA设置
     *
     * @param providerType 提供商类型
     * @param authentication 认证信息
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/setup/{providerType}")
    public ApiResponse<MfaResult> removeMfaSetup(
            @PathVariable String providerType, Authentication authentication, HttpServletRequest httpRequest) {
        String userId = authentication.getName();
        log.info("移除用户MFA设置，用户ID: {}, 提供商: {}", userId, providerType);

        MfaContext context = buildMfaContext(userId, authentication, httpRequest);
        MfaResult result = mfaService.removeMfaSetup(userId, providerType, context);

        return ApiResponse.success(result);
    }

    /**
     * 生成备用恢复码
     *
     * @param providerType 提供商类型
     * @param authentication 认证信息
     * @param httpRequest HTTP请求
     * @return 备用恢复码信息
     */
    @PostMapping("/backup-codes/{providerType}")
    public ApiResponse<MfaChallenge> generateBackupCodes(
            @PathVariable String providerType, Authentication authentication, HttpServletRequest httpRequest) {
        String userId = authentication.getName();
        log.info("生成用户备用恢复码，用户ID: {}, 提供商: {}", userId, providerType);

        MfaContext context = buildMfaContext(userId, authentication, httpRequest);
        MfaChallenge challenge = mfaService.generateBackupCodes(userId, providerType, context);

        return ApiResponse.success(challenge);
    }

    /**
     * 构建MFA上下文
     */
    private MfaContext buildMfaContext(String userId, Authentication authentication, HttpServletRequest request) {
        return MfaContext.builder()
                .userId(userId)
                .username(authentication.getName())
                .clientIp(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .sessionId(
                        request.getSession(false) != null ? request.getSession().getId() : null)
                .build();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    // ========== 请求/响应DTO ==========

    /**
     * MFA状态响应
     */
    @Data
    @lombok.Builder
    public static class MfaStatusResponse {
        /** 是否已设置MFA */
        private boolean isSetup;

        /** 已设置的提供商列表 */
        private List<String> setupProviders;

        /** 可用的提供商列表 */
        private List<String> availableProviders;
    }

    /**
     * 完成MFA设置请求
     */
    @Data
    public static class CompleteMfaSetupRequest {
        /** 挑战ID */
        @NotBlank(message = "挑战ID不能为空")
        private String challengeId;

        /** 验证码 */
        @NotBlank(message = "验证码不能为空")
        private String verificationCode;
    }

    /**
     * MFA验证请求
     */
    @Data
    public static class VerifyMfaRequest {
        /** 验证码 */
        @NotBlank(message = "验证码不能为空")
        private String verificationCode;
    }
}
