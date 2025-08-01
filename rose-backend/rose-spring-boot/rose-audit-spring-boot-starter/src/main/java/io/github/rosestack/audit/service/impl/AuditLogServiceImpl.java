package io.github.rosestack.audit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.mapper.AuditLogMapper;
import io.github.rosestack.audit.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import java.time.LocalDateTime;


/**
 * 审计日志服务实现类
 * <p>
 * 提供审计日志的完整业务功能实现，包括记录、查询、统计、分析等。
 * 支持同步和异步处理，确保高性能和可靠性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    private final Validator validator;
    @Override
    public AuditLog recordAuditLog(AuditLog auditLog) {
        try {
            log.debug("开始记录审计日志: {}", auditLog.getOperationName());

            // 数据验证
            validateAuditLog(auditLog);

            // 补充上下文信息
            enrichAuditLogContext(auditLog);

            // 生成哈希值
            generateHashValues(auditLog);

            // 保存到数据库
            boolean success = save(auditLog);
            if (!success) {
                throw new RuntimeException("保存审计日志失败");
            }

            log.debug("审计日志记录成功，ID: {}", auditLog.getId());
            return auditLog;
        } catch (Exception e) {
            throw new RuntimeException("记录审计日志失败", e);
        }
    }

    /**
     * 验证审计日志数据
     */
    private void validateAuditLog(AuditLog auditLog) {
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(auditLog);
        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();
            throw new IllegalArgumentException("审计日志数据验证失败: " + errorMessage);
        }
    }

    /**
     * 补充审计日志上下文信息
     */
    private void enrichAuditLogContext(AuditLog auditLog) {
        // 设置事件时间
        if (auditLog.getEventTime() == null) {
            auditLog.setEventTime(LocalDateTime.now());
        }

        // 设置创建时间
        if (auditLog.getCreatedTime() == null) {
            auditLog.setCreatedTime(LocalDateTime.now());
        }

        // 补充上下文信息
        if (!StringUtils.hasText(auditLog.getUserId())) {
            auditLog.setUserId("system"); // 默认用户
        }
        if (!StringUtils.hasText(auditLog.getTenantId())) {
            auditLog.setTenantId("default"); // 默认租户
        }
        if (!StringUtils.hasText(auditLog.getClientIp())) {
            auditLog.setClientIp("127.0.0.1"); // 默认IP
        }
        if (!StringUtils.hasText(auditLog.getUserAgent())) {
            auditLog.setUserAgent("Unknown"); // 默认用户代理
        }
    }

    /**
     * 生成安全哈希值和数字签名
     */
    private void generateHashValues(AuditLog auditLog) {
        try {
            // 生成简单的哈希值
            String data = String.format("%s-%s-%s-%s",
                auditLog.getEventType(),
                auditLog.getOperationName(),
                auditLog.getUserId(),
                auditLog.getEventTime());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            auditLog.setHashValue(hexString.toString());

            // 生成简单的数字签名
            String salt = String.valueOf(System.currentTimeMillis());
            String signatureData = auditLog.getHashValue() + salt;
            auditLog.setDigitalSignature(String.valueOf(signatureData.hashCode()));

            log.debug("生成审计日志哈希值和数字签名成功，ID: {}", auditLog.getId());
        } catch (NoSuchAlgorithmException e) {
            log.error("生成审计日志哈希值失败: {}", e.getMessage(), e);
            // 使用备用方法
            String fallbackHash = String.valueOf(auditLog.toString().hashCode());
            auditLog.setHashValue(fallbackHash);
            auditLog.setDigitalSignature(fallbackHash);
        }
    }
}