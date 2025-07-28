package io.github.rosestack.mybatis;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import io.github.rosestack.mybatis.config.RoseMybatisAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 循环依赖测试
 * 
 * <p>验证 RoseMybatisAutoConfiguration 和 MybatisPlusAutoConfiguration 之间不存在循环依赖
 * 
 * @author chensoul
 * @since 1.0.0
 */
class CircularDependencyTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                DataSourceAutoConfiguration.class,
                MybatisPlusAutoConfiguration.class,
                RoseMybatisAutoConfiguration.class
            ));

    @Test
    void shouldNotHaveCircularDependency() {
        // 测试在包含 MyBatis Plus 自动配置的情况下，不会出现循环依赖
        this.contextRunner
                .withPropertyValues(
                    "rose.mybatis.enabled=true",
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password="
                )
                .run(context -> {
                    // 如果没有循环依赖，上下文应该能够成功启动
                    assertThat(context).hasNotFailed();
                    
                    // 验证关键 Bean 都存在
                    assertThat(context).hasSingleBean(RoseMybatisAutoConfiguration.class);
                    assertThat(context).hasSingleBean(MybatisPlusAutoConfiguration.class);
                });
    }

    @Test
    void shouldStartWithMinimalConfiguration() {
        // 测试最小配置下的启动
        this.contextRunner
                .withPropertyValues(
                    "rose.mybatis.enabled=true",
                    "rose.mybatis.tenant.enabled=false",
                    "rose.mybatis.pagination.enabled=false",
                    "rose.mybatis.optimistic-lock.enabled=false",
                    "spring.datasource.url=jdbc:h2:mem:testdb2",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password="
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(RoseMybatisAutoConfiguration.class);
                });
    }

    @Test
    void shouldStartWithAllFeaturesEnabled() {
        // 测试所有功能启用的情况
        this.contextRunner
                .withPropertyValues(
                    "rose.mybatis.enabled=true",
                    "rose.mybatis.tenant.enabled=true",
                    "rose.mybatis.pagination.enabled=true",
                    "rose.mybatis.optimistic-lock.enabled=true",
                    "rose.mybatis.field-fill.enabled=true",
                    "spring.datasource.url=jdbc:h2:mem:testdb3",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password="
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(RoseMybatisAutoConfiguration.class);
                    assertThat(context).hasSingleBean(MybatisPlusAutoConfiguration.class);
                });
    }
}