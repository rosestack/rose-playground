package io.github.rosestack.audit.config;

import io.github.rosestack.audit.aspect.AuditAspect;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.service.AuditLogService;
import io.github.rosestack.audit.storage.AuditStorage;
import io.github.rosestack.audit.storage.DatabaseAuditStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计自动配置测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

    @Test
    void testAutoConfigurationEnabled() {
        contextRunner
                .withPropertyValues("rose.audit.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditAutoConfiguration.class);
                    assertThat(context).hasSingleBean(AuditProperties.class);
                });
    }

    @Test
    void testAutoConfigurationDisabled() {
        contextRunner
                .withPropertyValues("rose.audit.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditAutoConfiguration.class);
                });
    }

    @Test
    void testAutoConfigurationDefault() {
        contextRunner
                .run(context -> {
                    // 默认启用
                    assertThat(context).hasSingleBean(AuditAutoConfiguration.class);
                    assertThat(context).hasSingleBean(AuditProperties.class);
                });
    }

    @Test
    void testServiceBeansRegistration() {
        contextRunner
                .withPropertyValues("rose.audit.enabled=true")
                .run(context -> {
                    // 验证服务层 Bean 是否注册
                    assertThat(context).hasBean("auditLogService");
                    assertThat(context).hasBean("auditLogDetailService");

                    // 验证 Bean 类型
                    assertThat(context.getBean("auditLogService")).isInstanceOf(AuditLogService.class);
                    assertThat(context.getBean("auditLogDetailService")).isInstanceOf(AuditLogDetailService.class);
                });
    }

    @Test
    void testAspectBeanRegistration() {
        contextRunner
                .withPropertyValues(
                    "rose.audit.enabled=true",
                    "rose.audit.aspect.enabled=true"
                )
                .run(context -> {
                    // 验证切面 Bean 是否注册
                    assertThat(context).hasBean("auditAspect");
                    assertThat(context.getBean("auditAspect")).isInstanceOf(AuditAspect.class);
                });
    }

    @Test
    void testAspectBeanDisabled() {
        contextRunner
                .withPropertyValues(
                    "rose.audit.enabled=true",
                    "rose.audit.aspect.enabled=false"
                )
                .run(context -> {
                    // 验证切面 Bean 未注册
                    assertThat(context).doesNotHaveBean("auditAspect");
                });
    }

    @Test
    void testStorageBeanRegistration() {
        contextRunner
                .withPropertyValues(
                    "rose.audit.enabled=true",
                    "rose.audit.storage.type=database"
                )
                .run(context -> {
                    // 验证存储 Bean 是否注册
                    assertThat(context).hasBean("databaseAuditStorage");
                    assertThat(context.getBean("databaseAuditStorage")).isInstanceOf(DatabaseAuditStorage.class);

                    // 验证接口类型
                    assertThat(context.getBean(AuditStorage.class)).isInstanceOf(DatabaseAuditStorage.class);
                });
    }

    @Test
    void testBridgeBeanDisabled() {
        contextRunner
                .withPropertyValues(
                    "rose.audit.enabled=true",
                    "rose.audit.mybatis-bridge.enabled=false"
                )
                .run(context -> {
                    // 验证桥接 Bean 未注册
                    assertThat(context).doesNotHaveBean("auditStorageBridge");
                });
    }

    @Test
    void testPropertiesBinding() {
        contextRunner
                .withPropertyValues(
                    "rose.audit.enabled=true",
                    "rose.audit.storage.type=database",
                    "rose.audit.storage.async=true",
                    "rose.audit.encryption.enabled=true",
                    "rose.audit.masking.enabled=true"
                )
                .run(context -> {
                    AuditProperties properties = context.getBean(AuditProperties.class);

                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getStorage().getType()).isEqualTo("database");
                    assertThat(properties.getStorage().isAsync()).isTrue();
                    assertThat(properties.getEncryption().isEnabled()).isTrue();
                });
    }
}