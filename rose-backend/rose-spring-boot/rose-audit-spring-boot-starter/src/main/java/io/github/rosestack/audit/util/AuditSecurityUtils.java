package io.github.rosestack.audit.util;

import io.github.rosestack.audit.entity.AuditLog;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计安全工具类
 * <p>
 * 简化的安全工具类，使用 Java 标准库提供基本的安全功能。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class AuditSecurityUtils {
    private static final String HASH_ALGORITHM = "SHA-256";

    private AuditSecurityUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 生成审计日志的安全哈希值
     *
     * @param auditLog 审计日志
     * @return 哈希值
     */
    public static String generateSecureHash(AuditLog auditLog) {
        try {
            String dataForHash = buildCanonicalHashData(auditLog);
            return generateSHA256Hash(dataForHash);
        } catch (Exception e) {
            log.error("生成审计日志哈希值失败: {}", e.getMessage(), e);
            return generateFallbackHash(auditLog);
        }
    }

    /**
     * 构建规范化的哈希数据
     */
    private static String buildCanonicalHashData(AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();

        // 按固定顺序添加关键字段，确保哈希的一致性
        appendField(sb, auditLog.getEventTime());
        appendField(sb, auditLog.getEventType());
        appendField(sb, auditLog.getEventSubtype());
        appendField(sb, auditLog.getOperationName());
        appendField(sb, auditLog.getUserId());
        appendField(sb, auditLog.getRequestUri());
        appendField(sb, auditLog.getHttpMethod());
        appendField(sb, auditLog.getStatus());
        appendField(sb, auditLog.getRiskLevel());
        appendField(sb, auditLog.getClientIp());
        appendField(sb, auditLog.getTenantId());

        return sb.toString();
    }

    /**
     * 添加字段到哈希数据中
     */
    private static void appendField(StringBuilder sb, Object field) {
        if (field != null) {
            if (field instanceof LocalDateTime) {
                sb.append(((LocalDateTime) field).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                sb.append(field.toString().trim());
            }
        }
        sb.append("|"); // 使用分隔符避免字段连接歧义
    }

    /**
     * 生成SHA-256哈希值
     */
    private static String generateSHA256Hash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 生成备用哈希值（当主要方法失败时）
     */
    private static String generateFallbackHash(AuditLog auditLog) {
        return String.valueOf(auditLog.toString().hashCode());
    }

    /**
     * 验证审计日志的完整性
     *
     * @param auditLog 审计日志
     * @return 验证结果
     */
    public static boolean verifyIntegrity(AuditLog auditLog) {
        if (auditLog.getHashValue() == null) {
            return false;
        }

        String expectedHash = generateSecureHash(auditLog);
        return auditLog.getHashValue().equals(expectedHash);
    }

    /**
     * 验证审计日志链的完整性
     *
     * @param currentLog  当前日志
     * @param previousLog 前一条日志
     * @return 验证结果
     */
    public static boolean verifyChainIntegrity(AuditLog currentLog, AuditLog previousLog) {
        if (currentLog.getPrevHash() == null || previousLog.getHashValue() == null) {
            return false;
        }

        return currentLog.getPrevHash().equals(previousLog.getHashValue());
    }

    /**
     * 生成简单的盐值
     *
     * @param length 盐值长度
     * @return 盐值
     */
    public static String generateSalt(int length) {
        return String.valueOf(System.currentTimeMillis()).substring(0, Math.min(length, 13));
    }

    /**
     * 生成数字签名
     *
     * @param data 要签名的数据
     * @param salt 盐值
     * @return 数字签名
     */
    public static String generateDigitalSignature(String data, String salt) throws NoSuchAlgorithmException {
        try {
            String signatureData = data + salt;
            return generateSHA256Hash(signatureData);
        } catch (Exception e) {
            log.error("生成数字签名失败: {}", e.getMessage(), e);
            return String.valueOf((data + System.currentTimeMillis()).hashCode());
        }
    }
}
