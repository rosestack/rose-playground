package io.github.rosestack.mybatis;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import io.github.rosestack.mybatis.audit.RoseMetaObjectHandler;
import io.github.rosestack.mybatis.config.RoseMybatisAutoConfiguration;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandler;
import io.github.rosestack.mybatis.desensitization.SensitiveFieldInterceptor;
import io.github.rosestack.mybatis.encryption.DefaultFieldEncryptor;
import io.github.rosestack.mybatis.encryption.FieldEncryptionInterceptor;
import io.github.rosestack.mybatis.tenant.RoseTenantLineHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rose MyBatis 自动配置测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class RoseMybatisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoseMybatisAutoConfiguration.class));

    @Test
    void testAutoConfiguration_DefaultSettings() {
        contextRunner.run(context -> {
            // 验证配置属性被创建
            assertThat(context).hasSingleBean(RoseMybatisProperties.class);

            // 验证核心组件被创建
            assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);
            assertThat(context).hasSingleBean(RoseMetaObjectHandler.class);

            // 验证默认启用的功能
            assertThat(context).hasSingleBean(RoseDataPermissionHandler.class);
            assertThat(context).hasSingleBean(SensitiveFieldInterceptor.class);
        });
    }

    @Test
    void testAutoConfiguration_WithTenantEnabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.tenant.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RoseTenantLineHandler.class);
                });
    }

    @Test
    void testAutoConfiguration_WithTenantDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.tenant.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RoseTenantLineHandler.class);
                });
    }

    @Test
    void testAutoConfiguration_WithEncryptionEnabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.encryption.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(DefaultFieldEncryptor.class);
                    assertThat(context).hasSingleBean(FieldEncryptionInterceptor.class);
                });
    }

    @Test
    void testAutoConfiguration_WithEncryptionDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.encryption.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FieldEncryptionInterceptor.class);
                });
    }

    @Test
    void testAutoConfiguration_WithDataPermissionDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.data-permission.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RoseDataPermissionHandler.class);
                });
    }

    @Test
    void testAutoConfiguration_WithDesensitizationDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.desensitization.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SensitiveFieldInterceptor.class);
                });
    }

    @Test
    void testAutoConfiguration_WithFieldFillDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.field-fill.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RoseMetaObjectHandler.class);
                });
    }

    @Test
    void testAutoConfiguration_WithCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "rose.mybatis.tenant.column=org_id",
                        "rose.mybatis.encryption.default-algorithm=DES",
                        "rose.mybatis.data-permission.default-field=dept_id",
                        "rose.mybatis.field-fill.default-user=admin"
                )
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);

                    assertThat(properties.getTenant().getColumn()).isEqualTo("org_id");
                    assertThat(properties.getEncryption().getDefaultAlgorithm()).isEqualTo("DES");
                    assertThat(properties.getDataPermission().getDefaultField()).isEqualTo("dept_id");
                    assertThat(properties.getFieldFill().getDefaultUser()).isEqualTo("admin");
                });
    }

    @Test
    void testAutoConfiguration_WithUserDefinedBeans() {
        contextRunner
                .withUserConfiguration(CustomConfiguration.class)
                .run(context -> {
                    // 验证用户自定义的Bean被使用
                    assertThat(context).hasSingleBean(RoseMetaObjectHandler.class);
                    assertThat(context.getBean(RoseMetaObjectHandler.class))
                            .isInstanceOf(CustomRoseMetaObjectHandler.class);
                });
    }

    @Test
    void testAutoConfiguration_PaginationSettings() {
        contextRunner
                .withPropertyValues(
                        "rose.mybatis.pagination.enabled=true",
                        "rose.mybatis.pagination.max-limit=500"
                )
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);
                    assertThat(properties.getPagination().isEnabled()).isTrue();
                    assertThat(properties.getPagination().getMaxLimit()).isEqualTo(500);
                });
    }

    @Test
    void testAutoConfiguration_OptimisticLockSettings() {
        contextRunner
                .withPropertyValues(
                        "rose.mybatis.optimistic-lock.enabled=true"
                )
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);
                    assertThat(properties.getOptimisticLock().isEnabled()).isTrue();
                });
    }

    /**
     * 自定义配置类
     */
    @Configuration
    static class CustomConfiguration {

        @Bean
        public RoseMetaObjectHandler customRoseMetaObjectHandler(RoseMybatisProperties properties) {
            return new CustomRoseMetaObjectHandler(properties);
        }
    }

    /**
     * 自定义元数据处理器
     */
    static class CustomRoseMetaObjectHandler extends RoseMetaObjectHandler {

        public CustomRoseMetaObjectHandler(RoseMybatisProperties properties) {
            super(properties);
        }
    }
}
