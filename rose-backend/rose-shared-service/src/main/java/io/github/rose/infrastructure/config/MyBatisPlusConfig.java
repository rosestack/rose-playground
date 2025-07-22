package io.github.rose.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.rose.infrastructure.mybatis.CustomMetaObjectHandler;
import io.github.rose.infrastructure.mybatis.DynamicTableNameFactory;
import io.github.rose.infrastructure.mybatis.SqlPerformanceInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis Plus 配置类
 * <p>
 * 提供MyBatis-Plus的插件配置，包括分页、乐观锁、防全表更新与删除等功能
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class MyBatisPlusConfig {

    /**
     * 配置MybatisPlusInterceptor插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 动态表名插件
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(DynamicTableNameFactory.tableNameHandler());

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        log.info("MyBatis Plus插件配置完成");
        return interceptor;
    }

    /**
     * 配置SQL性能分析插件（仅在开发和测试环境启用）
     */
    @Bean
    @Profile({"dev", "test"})
    public SqlPerformanceInterceptor sqlPerformanceInterceptor() {
        SqlPerformanceInterceptor interceptor = new SqlPerformanceInterceptor();
        interceptor.setMaxTime(1000); // 超过1秒记录警告
        log.info("SQL性能分析插件配置完成");
        return interceptor;
    }

    @Bean
    public CustomMetaObjectHandler customMetaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
}