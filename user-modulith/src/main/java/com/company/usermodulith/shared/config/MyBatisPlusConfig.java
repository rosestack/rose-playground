package com.company.usermodulith.shared.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 * <p>
 * 配置分页插件、乐观锁插件、防全表更新删除插件
 * 配置自动填充处理器
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大分页数量限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Component
    @Slf4j
    public static class CustomMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            log.debug("开始执行插入填充...");
            String currentUsername = getCurrentUsername();
            
            this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "createdBy", String.class, currentUsername);
            this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUsername);
            this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
            this.strictInsertFill(metaObject, "version", Integer.class, 1);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            log.debug("开始执行更新填充...");
            String currentUsername = getCurrentUsername();
            
            this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUsername);
        }

        private String getCurrentUsername() {
            // TODO: 从 Spring Security 上下文获取当前用户
            return "system";
        }
    }
} 