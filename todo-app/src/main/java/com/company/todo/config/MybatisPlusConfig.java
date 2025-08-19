package com.company.todo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
@Import(MybatisPlusConfig.MetaObjectHandlerConfig.class)
public class MybatisPlusConfig {
    private final MybatisProperties properties;

    public MybatisPlusConfig(MybatisProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 从配置中读取分页大小上限，避免硬编码
        paginationInterceptor.setMaxLimit(properties.getPagination().getMaxPageSize());
        interceptor.addInnerInterceptor(paginationInterceptor);
        // 启用乐观锁插件，配合实体的 @Version 字段生效
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    public static class MetaObjectHandlerConfig implements MetaObjectHandler {
        @Override
        public void insertFill(MetaObject metaObject) {
            strictInsertFill(metaObject, "createdTime", LocalDateTime::now, LocalDateTime.class);
            strictInsertFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            strictUpdateFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        }
    }
}
