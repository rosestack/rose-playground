package io.github.rosestack.audit.util;

import io.github.rosestack.core.jackson.desensitization.Desensitization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 审计脱敏工具类
 * <p>
 * 提供审计日志数据的脱敏功能，复用现有的 Desensitization 工具。
 * 支持多种脱敏规则，可配置自定义脱敏策略。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public final class AuditMaskingUtils {

    private AuditMaskingUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 根据字段名进行脱敏（简化版本，使用默认配置）
     *
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @return 脱敏后的值
     */
    public static String maskByFieldName(String fieldName, String fieldValue) {
        return doMaskByFieldName(fieldName, fieldValue);
    }

    /**
     * 执行字段脱敏的核心逻辑
     */
    private static String doMaskByFieldName(String fieldName, String fieldValue) {
        if (!StringUtils.hasText(fieldValue) || !StringUtils.hasText(fieldName)) {
            return fieldValue;
        }

        String lowerFieldName = fieldName.toLowerCase();

        // 根据字段名判断脱敏类型，使用 Desensitization 类的方法
        if (lowerFieldName.contains("phone") || lowerFieldName.contains("mobile")) {
            return Desensitization.maskPhone(fieldValue);
        } else if (lowerFieldName.contains("email") || lowerFieldName.contains("mail")) {
            return Desensitization.maskEmail(fieldValue);
        } else if (lowerFieldName.contains("idcard") || lowerFieldName.contains("id_card")) {
            return Desensitization.maskIdCard(fieldValue);
        } else if (lowerFieldName.contains("bankcard") || lowerFieldName.contains("bank_card") || lowerFieldName.contains("card_no")) {
            return Desensitization.maskBankCard(fieldValue);
        } else if (lowerFieldName.contains("password") || lowerFieldName.contains("pwd")) {
            return Desensitization.maskPassword(fieldValue);
        } else if (lowerFieldName.contains("token") || lowerFieldName.contains("authorization")) {
            return Desensitization.maskToken(fieldValue);
        } else if (lowerFieldName.contains("client_ip") || lowerFieldName.contains("server_ip")) {
            return Desensitization.maskIpAddress(fieldValue);
        } else if (lowerFieldName.contains("name") && !lowerFieldName.contains("username")) {
            return Desensitization.maskName(fieldValue);
        } else if (lowerFieldName.contains("license_plate")) {
            return Desensitization.maskLicensePlate(fieldValue);
        }

        // 使用智能检测
        return Desensitization.maskByPattern(fieldValue);
    }
}