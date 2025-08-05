package io.github.rosestack.spring.boot.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import io.github.rosestack.spring.boot.common.encryption.FieldEncryptor;
import io.github.rosestack.spring.boot.mybatis.handler.RoseMetaObjectHandler;
import io.github.rosestack.spring.boot.mybatis.handler.RoseTenantLineHandler;
import io.github.rosestack.spring.boot.mybatis.interceptor.FieldEncryptionInterceptor;
import io.github.rosestack.spring.boot.mybatis.support.datapermission.RoseDataPermissionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
        contextRunner
                .withPropertyValues("spring.datasource.url=jdbc:h2:mem:testdb")
                .run(context -> {
                    // 验证配置属性被创建
                    assertThat(context).hasSingleBean(RoseMybatisProperties.class);

                    // 验证核心组件被创建
                    assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);
                });
    }

    @Test
    void testAutoConfiguration_WithTenantEnabled() {
        contextRunner
                .withPropertyValues(
                        "spring.datasource.url=jdbc:h2:mem:testdb",
                        "rose.mybatis.tenant.enabled=true"
                )
                .run(context -> {
                    // 租户处理器是在拦截器内部创建的，不是独立的Bean
                    assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);

                    RoseMybatisProperties props = context.getBean(RoseMybatisProperties.class);
                    assertThat(props.getTenant().isEnabled()).isTrue();
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
                .withPropertyValues(
                        "spring.datasource.url=jdbc:h2:mem:testdb",
                        "rose.mybatis.encryption.enabled=true"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FieldEncryptionInterceptor.class);
                    assertThat(context).doesNotHaveBean(FieldEncryptor.class);

                    RoseMybatisProperties props = context.getBean(RoseMybatisProperties.class);
                    assertThat(props.getEncryption().isEnabled()).isTrue();
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
    void testAutoConfiguration_WithFieldFillDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis.field-fill.enabled=false")
                .run(context -> {
                    // 检查自动配置的 Bean 不存在，但可能有用户自定义的 Bean
                    assertThat(context).doesNotHaveBean("roseMetaObjectHandler");
                });
    }

    @Test
    void testAutoConfiguration_WithCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "rose.mybatis.tenant.column=org_id",
                        "rose.mybatis.data-permission.default-field=dept_id",
                        "rose.mybatis.field-fill.default-user=admin"
                )
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);

                    assertThat(properties.getTenant().getColumn()).isEqualTo("org_id");
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
    @EnableConfigurationProperties(RoseMybatisProperties.class)
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
