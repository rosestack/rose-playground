package io.github.rosestack.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import io.github.rosestack.mybatis.audit.RoseMetaObjectHandler;
import io.github.rosestack.mybatis.config.RoseMybatisAutoConfiguration;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.tenant.RoseTenantLineHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rose MyBatis Plus 自动配置测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class RoseMybatisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoseMybatisAutoConfiguration.class));

    @Test
    void shouldAutoConfigureWhenEnabled() {
        this.contextRunner
                .withPropertyValues("rose.mybatis.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RoseMybatisProperties.class);
                    assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);
                    assertThat(context).hasSingleBean(MetaObjectHandler.class);
                });
    }

    @Test
    void shouldNotAutoConfigureWhenDisabled() {
        this.contextRunner
                .withPropertyValues("rose.mybatis.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MybatisPlusInterceptor.class);
                    assertThat(context).doesNotHaveBean(RoseTenantLineHandler.class);
                    assertThat(context).doesNotHaveBean(MetaObjectHandler.class);
                });
    }

    @Test
    void shouldConfigureTenantWhenEnabled() {
        this.contextRunner
                .withPropertyValues(
                        "rose.mybatis.enabled=true",
                        "rose.mybatis.tenant.enabled=true",
                        "rose.mybatis.tenant.column=tenant_id"
                )
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);
                    assertThat(properties.getTenant().isEnabled()).isTrue();
                    assertThat(properties.getTenant().getColumn()).isEqualTo("tenant_id");
                });
    }

    @Test
    void shouldNotConfigureTenantWhenDisabled() {
        this.contextRunner
                .withPropertyValues(
                        "rose.mybatis.enabled=true",
                        "rose.mybatis.tenant.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RoseTenantLineHandler.class);
                });
    }

    @Test
    void shouldConfigurePaginationWithCustomSettings() {
        this.contextRunner
                .withPropertyValues(
                        "rose.mybatis.enabled=true",
                        "rose.mybatis.pagination.enabled=true",
                        "rose.mybatis.pagination.max-limit=500",
                        "rose.mybatis.pagination.db-type=postgresql"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);

                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);
                    assertThat(properties.getPagination().isEnabled()).isTrue();
                    assertThat(properties.getPagination().getMaxLimit()).isEqualTo(500L);
                    assertThat(properties.getPagination().getDbType()).isEqualTo("postgresql");
                });
    }

    @Test
    void shouldConfigureFieldFillWithCustomColumns() {
        this.contextRunner
                .withPropertyValues(
                        "rose.mybatis.enabled=true",
                        "rose.mybatis.field-fill.enabled=true",
                        "rose.mybatis.field-fill.create-time-column=create_time",
                        "rose.mybatis.field-fill.update-time-column=update_time"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MetaObjectHandler.class);
                    assertThat(context.getBean(MetaObjectHandler.class))
                            .isInstanceOf(RoseMetaObjectHandler.class);

                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);
                    assertThat(properties.getFieldFill().isEnabled()).isTrue();
                    assertThat(properties.getFieldFill().getCreateTimeColumn()).isEqualTo("create_time");
                    assertThat(properties.getFieldFill().getUpdateTimeColumn()).isEqualTo("update_time");
                });
    }

    @Test
    void shouldNotConfigureFieldFillWhenDisabled() {
        this.contextRunner
                .withPropertyValues(
                        "rose.mybatis.enabled=true",
                        "rose.mybatis.field-fill.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MetaObjectHandler.class);
                });
    }


    @Test
    void shouldConfigureOptimisticLockWithCustomColumn() {
        this.contextRunner
                .withPropertyValues(
                        "rose.mybatis.enabled=true",
                        "rose.mybatis.optimistic-lock.enabled=true"
                )
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);
                    assertThat(properties.getOptimisticLock().isEnabled()).isTrue();
                });
    }

    @Test
    void shouldUseDefaultPropertiesWhenNotSpecified() {
        this.contextRunner
                .withPropertyValues("rose.mybatis.enabled=true",
                        "rose.mybatis.tenant.enabled=true")
                .run(context -> {
                    RoseMybatisProperties properties = context.getBean(RoseMybatisProperties.class);

                    // 验证默认值
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getTenant().isEnabled()).isTrue();
                    assertThat(properties.getTenant().getColumn()).isEqualTo("tenant_id");
                    assertThat(properties.getPagination().isEnabled()).isTrue();
                    assertThat(properties.getPagination().getMaxLimit()).isEqualTo(1000L);

                    assertThat(properties.getOptimisticLock().isEnabled()).isTrue();
                    assertThat(properties.getFieldFill().isEnabled()).isTrue();
                    assertThat(properties.getPerformance().isEnabled()).isTrue();
                });
    }
}
