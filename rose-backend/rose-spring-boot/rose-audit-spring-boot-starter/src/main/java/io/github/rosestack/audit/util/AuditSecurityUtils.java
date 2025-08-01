package io.github.rosestack.audit.util;

import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 审计安全工具类
 * <p>
 * 提供审计日志的安全功能，包括哈希值生成、数字签名、敏感数据脱敏等。
 * 确保审计数据的完整性、机密性和不可否认性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class AuditSecurityUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // 敏感数据脱敏模式
    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
        Pattern.compile("\\b\\d{15,19}\\b"),                    // 银行卡号
        Pattern.compile("\\b\\d{17}[\\dXx]\\b"),                // 身份证号
        Pattern.compile("\\b1[3-9]\\d{9}\\b"),                  // 手机号
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // 邮箱
        Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b")     // 信用卡号
    );

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
     * 生成审计日志链的哈希值（包含前一条记录的哈希）
     *
     * @param auditLog 当前审计日志
     * @param prevHash 前一条记录的哈希值
     * @return 链式哈希值
     */
    public static String generateChainHash(AuditLog auditLog, String prevHash) {
        try {
            String currentData = buildCanonicalHashData(auditLog);
            String chainData = (prevHash != null ? prevHash : "") + currentData;
            return generateSHA256Hash(chainData);
        } catch (Exception e) {
            log.error("生成审计日志链哈希值失败: {}", e.getMessage(), e);
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
     * 对敏感数据进行脱敏处理
     *
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    public static String maskSensitiveData(String data) {
        if (!StringUtils.hasText(data)) {
            return data;
        }

        String maskedData = data;
        
        // 应用所有敏感数据模式
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            maskedData = pattern.matcher(maskedData).replaceAll(match -> {
                String matched = match.group();
                if (matched.length() <= 4) {
                    return "****";
                }
                // 保留前2位和后2位，中间用*替换
                return matched.substring(0, 2) + 
                       "*".repeat(matched.length() - 4) + 
                       matched.substring(matched.length() - 2);
            });
        }
        
        return maskedData;
    }

    /**
     * 检查数据是否包含敏感信息
     *
     * @param data 数据
     * @return 是否包含敏感信息
     */
    public static boolean containsSensitiveData(String data) {
        if (!StringUtils.hasText(data)) {
            return false;
        }
        
        return SENSITIVE_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(data).find());
    }

    /**
     * 生成安全的随机盐值
     *
     * @param length 盐值长度
     * @return 盐值
     */
    public static String generateSalt(int length) {
        StringBuilder salt = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            salt.append(SALT_CHARS.charAt(SECURE_RANDOM.nextInt(SALT_CHARS.length())));
        }
        return salt.toString();
    }

    /**
     * 对审计详情进行安全处理
     *
     * @param detail 审计详情
     */
    public static void secureAuditDetail(AuditLogDetail detail) {
        if (detail.getDetailValue() != null) {
            // 检查是否包含敏感数据
            boolean isSensitive = containsSensitiveData(detail.getDetailValue());
            detail.setIsSensitive(isSensitive);
            
            // 如果包含敏感数据，进行脱敏处理
            if (isSensitive) {
                String maskedValue = maskSensitiveData(detail.getDetailValue());
                detail.setDetailValue(maskedValue);
                log.debug("审计详情包含敏感数据，已进行脱敏处理，详情ID: {}", detail.getId());
            }
        }
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
            return generateSHA256Hash(data + System.currentTimeMillis());
        }
    }
}
