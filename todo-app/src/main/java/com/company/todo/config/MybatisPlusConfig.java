package com.company.todo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Configuration
@Import(MybatisPlusConfig.MetaObjectHandlerConfig.class)
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(100L); // 设置分页大小上限为100
        interceptor.addInnerInterceptor(paginationInterceptor);
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
