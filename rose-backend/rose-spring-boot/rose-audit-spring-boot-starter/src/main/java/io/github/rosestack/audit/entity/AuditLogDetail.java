package io.github.rosestack.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditDetailType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 审计日志详情表实体类
 * <p>
 * 存储审计日志的详细信息，采用 JSON 格式存储复杂数据结构。
 * 支持敏感数据标记、加密存储、按类型分类等特性。
 * 详情数据按类型分为：HTTP请求相关、操作对象相关、数据变更相关、系统技术相关、安全相关。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_log_detail")
public class AuditLogDetail {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 审计日志ID（外键）
     */
    @NotNull(message = "审计日志ID不能为空")
    @TableField("audit_log_id")
    private Long auditLogId;

    /**
     * 详情类型
     */
    @NotBlank(message = "详情类型不能为空")
    @Size(max = 50, message = "详情类型长度不能超过50个字符")
    @TableField("detail_type")
    private String detailType;

    /**
     * 详情键
     */
    @NotBlank(message = "详情键不能为空")
    @Size(max = 50, message = "详情键长度不能超过50个字符")
    @TableField("detail_key")
    private String detailKey;

    /**
     * 详情值（JSON格式，可能加密脱敏）
     */
    @TableField("detail_value")
    private String detailValue;

    /**
     * 是否包含敏感数据
     */
    @TableField("is_sensitive")
    private Boolean isSensitive;

    /**
     * 是否已加密存储
     */
    @TableField("is_encrypted")
    private Boolean isEncrypted;

    /**
     * 租户ID（多租户支持）
     */
    @Size(max = 50, message = "租户ID长度不能超过50个字符")
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ==================== 业务方法 ====================

    /**
     * 设置详情类型（从枚举）
     *
     * @param detailType 详情类型枚举
     */
    public void setDetailType(AuditDetailType detailType) {
        if (detailType != null) {
            this.detailType = detailType.getCode();
        }
    }

    /**
     * 设置详情键（从枚举）
     *
     * @param detailKey 详情键枚举
     */
    public void setDetailKey(AuditDetailKey detailKey) {
        if (detailKey != null) {
            this.detailKey = detailKey.getCode();
            // 自动设置敏感数据标记
            this.isSensitive = detailKey.isSensitive();
        }
    }

    /**
     * 获取详情类型枚举
     *
     * @return 详情类型枚举
     */
    public AuditDetailType getDetailTypeEnum() {
        return AuditDetailType.fromCode(this.detailType);
    }

    /**
     * 获取详情键枚举
     *
     * @return 详情键枚举
     */
    public AuditDetailKey getDetailKeyEnum() {
        return AuditDetailKey.fromCode(this.detailKey);
    }

    /**
     * 判断是否需要加密
     *
     * @return 是否需要加密
     */
    public boolean needsEncryption() {
        AuditDetailKey keyEnum = getDetailKeyEnum();
        return keyEnum != null && keyEnum.needsEncryption();
    }

    /**
     * 判断是否需要脱敏
     *
     * @return 是否需要脱敏
     */
    public boolean needsMasking() {
        AuditDetailKey keyEnum = getDetailKeyEnum();
        return keyEnum != null && keyEnum.needsMasking();
    }

    /**
     * 判断是否为HTTP相关详情
     *
     * @return 是否为HTTP相关
     */
    public boolean isHttpRelated() {
        AuditDetailType typeEnum = getDetailTypeEnum();
        return typeEnum == AuditDetailType.HTTP_REQUEST;
    }

    /**
     * 判断是否为数据变更相关详情
     *
     * @return 是否为数据变更相关
     */
    public boolean isDataChangeRelated() {
        AuditDetailType typeEnum = getDetailTypeEnum();
        return typeEnum == AuditDetailType.DATA_CHANGE;
    }

    /**
     * 判断是否为安全相关详情
     *
     * @return 是否为安全相关
     */
    public boolean isSecurityRelated() {
        AuditDetailType typeEnum = getDetailTypeEnum();
        return typeEnum == AuditDetailType.SECURITY;
    }

    /**
     * 创建HTTP请求详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @param detailValue 详情值
     * @return 详情实体
     */
    public static AuditLogDetail createHttpDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue) {
        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailType(AuditDetailType.HTTP_REQUEST.getCode())
                .detailKey(detailKey.getCode())
                .detailValue(detailValue)
                .isSensitive(detailKey.isSensitive())
                .isEncrypted(false)
                .build();
    }

    /**
     * 创建数据变更详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @param detailValue 详情值
     * @return 详情实体
     */
    public static AuditLogDetail createDataChangeDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue) {
        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailType(AuditDetailType.DATA_CHANGE.getCode())
                .detailKey(detailKey.getCode())
                .detailValue(detailValue)
                .isSensitive(detailKey.isSensitive())
                .isEncrypted(false)
                .build();
    }

    /**
     * 创建安全相关详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @param detailValue 详情值
     * @return 详情实体
     */
    public static AuditLogDetail createSecurityDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue) {
        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailType(AuditDetailType.SECURITY.getCode())
                .detailKey(detailKey.getCode())
                .detailValue(detailValue)
                .isSensitive(detailKey.isSensitive())
                .isEncrypted(detailKey.needsEncryption())
                .build();
    }

    /**
     * 创建系统技术详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @param detailValue 详情值
     * @return 详情实体
     */
    public static AuditLogDetail createSystemDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue) {
        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailType(AuditDetailType.SYSTEM_TECH.getCode())
                .detailKey(detailKey.getCode())
                .detailValue(detailValue)
                .isSensitive(detailKey.isSensitive())
                .isEncrypted(false)
                .build();
    }

    /**
     * 创建操作对象详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @param detailValue 详情值
     * @return 详情实体
     */
    public static AuditLogDetail createOperationDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue) {
        return AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailType(AuditDetailType.OPERATION_TARGET.getCode())
                .detailKey(detailKey.getCode())
                .detailValue(detailValue)
                .isSensitive(detailKey.isSensitive())
                .isEncrypted(false)
                .build();
    }
}