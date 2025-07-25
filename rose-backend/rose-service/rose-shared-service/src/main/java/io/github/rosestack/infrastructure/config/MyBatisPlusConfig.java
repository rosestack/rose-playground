package io.github.rosestack.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.rosestack.infrastructure.mybatis.CustomMetaObjectHandler;
import io.github.rosestack.infrastructure.mybatis.DynamicTableNameFactory;
import io.github.rosestack.infrastructure.mybatis.SqlPerformanceInterceptor;
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
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 动态表名插件
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(DynamicTableNameFactory.tableNameHandler());
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);

        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大分页数量限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

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
        return interceptor;
    }

    @Bean
    public CustomMetaObjectHandler customMetaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
}