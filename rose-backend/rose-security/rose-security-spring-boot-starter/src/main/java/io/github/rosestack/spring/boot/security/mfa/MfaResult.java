package io.github.rosestack.spring.boot.security.mfa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MFA验证结果
 * <p>
 * 封装MFA认证操作的结果信息，包括成功状态、错误信息、建议等。
 * 提供丰富的反馈信息以支持前端显示和后续处理逻辑。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MfaResult {

    /** 操作是否成功 */
    private boolean success;

    /** 错误代码 */
    private String errorCode;

    /** 错误消息 */
    private String errorMessage;

    /** 详细错误信息 */
    private List<String> errorDetails;

    /** 用户ID */
    private String userId;

    /** MFA提供商类型 */
    private String providerType;

    /** 操作类型（setup、verify、remove等） */
    private String operationType;

    /** 验证令牌（成功时可能包含） */
    private String verificationToken;

    /** 剩余重试次数 */
    private Integer remainingAttempts;

    /** 锁定到期时间（如果被锁定） */
    private LocalDateTime lockoutExpiresAt;

    /** 下次允许验证时间 */
    private LocalDateTime nextAttemptAllowedAt;

    /** 建议的下一步操作 */
    private String suggestedAction;

    /** 成功消息 */
    private String successMessage;

    /** 扩展数据 */
    private Map<String, Object> data;

    /** 操作时间戳 */
    private LocalDateTime timestamp;

    /**
     * 创建成功结果
     *
     * @param userId 用户ID
     * @param providerType MFA提供商类型
     * @param operationType 操作类型
     * @return 成功结果
     */
    public static MfaResult success(String userId, String providerType, String operationType) {
        return MfaResult.builder()
                .success(true)
                .userId(userId)
                .providerType(providerType)
                .operationType(operationType)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param userId 用户ID
     * @param providerType MFA提供商类型
     * @param operationType 操作类型
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @return 失败结果
     */
    public static MfaResult failure(
            String userId, String providerType, String operationType, String errorCode, String errorMessage) {
        return MfaResult.builder()
                .success(false)
                .userId(userId)
                .providerType(providerType)
                .operationType(operationType)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 获取扩展数据
     *
     * @param key 数据键
     * @param <T> 数据类型
     * @return 数据值
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return data != null ? (T) data.get(key) : null;
    }

    /**
     * 设置扩展数据
     *
     * @param key 数据键
     * @param value 数据值
     */
    public void setData(String key, Object value) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put(key, value);
    }

    /**
     * 检查是否被锁定
     *
     * @return 如果被锁定返回true
     */
    public boolean isLockedOut() {
        return lockoutExpiresAt != null && LocalDateTime.now().isBefore(lockoutExpiresAt);
    }
}
