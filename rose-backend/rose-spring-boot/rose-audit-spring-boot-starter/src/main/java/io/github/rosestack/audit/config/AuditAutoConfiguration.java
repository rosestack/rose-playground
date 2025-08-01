package io.github.rosestack.audit.config;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rosestack.audit.aspect.AuditAspect;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.service.AuditLogService;
import io.github.rosestack.audit.service.impl.AuditLogDetailServiceImpl;
import io.github.rosestack.audit.service.impl.AuditLogServiceImpl;
import io.github.rosestack.audit.storage.AuditStorage;
import io.github.rosestack.audit.storage.DatabaseAuditStorage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import jakarta.validation.Validator;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * 审计日志自动配置类
 * <p>
 * 提供审计日志功能的自动配置，包括：
 * - 审计日志服务
 * - 审计切面
 * - 存储实现
 * - 加密脱敏工具
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(AuditProperties.class)
@ComponentScan(basePackages = "io.github.rosestack.audit")
@ConditionalOnProperty(prefix = "rose.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAutoConfiguration {
    private final AuditProperties auditProperties;

    @PostConstruct
    public void init() {
        log.info("审计配置: 存储类型={}", auditProperties.getStorage().getType());
    }

    /**
     * 审计日志服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(IService.class)
    public AuditLogService auditLogService(Validator validator) {
        log.debug("注册 AuditLogService Bean");
        return new AuditLogServiceImpl(validator);
    }

    /**
     * 审计日志详情服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(IService.class)
    public AuditLogDetailService auditLogDetailService() {
        log.debug("注册 AuditLogDetailService Bean");
        return new AuditLogDetailServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(AuditStorage.class)
    @ConditionalOnProperty(prefix = "rose.audit.storage", name = "type", havingValue = "database", matchIfMissing = true)
    @ConditionalOnClass(IService.class)
    public AuditStorage databaseAuditStorage(AuditLogService auditLogService, AuditLogDetailService auditLogDetailService) {
        log.debug("注册 DatabaseAuditStorage Bean");
        return new DatabaseAuditStorage(auditLogService, auditLogDetailService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.audit.aspect", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditAspect auditAspect(ApplicationEventPublisher eventPublisher) {
        log.debug("注册 AuditAspect Bean");
        return new AuditAspect(eventPublisher, auditProperties);
    }
}