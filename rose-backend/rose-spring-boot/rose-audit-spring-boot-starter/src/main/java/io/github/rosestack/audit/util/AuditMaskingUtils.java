package io.github.rosestack.audit.util;

import io.github.rosestack.audit.properties.AuditProperties;
import io.github.rosestack.core.jackson.desensitization.Desensitization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;

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

    /**
     * 批量脱敏
     *
     * @param dataMap 数据映射
     * @return 脱敏后的数据映射
     */
    public static Map<String, String> maskBatch(Map<String, String> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }

        dataMap.replaceAll((key, value) -> maskByFieldName(key, value));
        return dataMap;
    }

    /**
     * 检查字符串是否包含敏感信息
     *
     * @param data       待检查的数据
     * @param properties 审计配置
     * @return 是否包含敏感信息
     */
    public static boolean containsSensitiveInfo(String data, AuditProperties properties) {
        if (!StringUtils.hasText(data)) {
            return false;
        }

        // 通过尝试脱敏来判断是否包含敏感信息
        // 如果脱敏后的结果与原始数据不同，说明包含敏感信息
        String maskedData = Desensitization.maskByPattern(data);
        return !data.equals(maskedData) ||
                data.toLowerCase().contains("password") ||
                data.toLowerCase().contains("token") ||
                data.toLowerCase().contains("authorization");
    }

    /**
     * 获取脱敏统计信息
     */
    public static String getMaskingStats(Map<String, String> originalData, Map<String, String> maskedData) {
        if (originalData == null || maskedData == null) {
            return "无统计数据";
        }

        int totalFields = originalData.size();
        int maskedFields = 0;

        for (Map.Entry<String, String> entry : originalData.entrySet()) {
            String key = entry.getKey();
            String originalValue = entry.getValue();
            String maskedValue = maskedData.get(key);

            if (maskedValue != null && !originalValue.equals(maskedValue)) {
                maskedFields++;
            }
        }

        return String.format("总字段数: %d, 脱敏字段数: %d, 脱敏率: %.2f%%",
                totalFields, maskedFields, (double) maskedFields / totalFields * 100);
    }
}