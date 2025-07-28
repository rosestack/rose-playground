package io.github.rosestack.billing.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import io.github.rosestack.billing.handler.BillingMetaObjectHandler;
import io.github.rosestack.billing.handler.BillingTenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 计费系统自动配置类
 *
 * 使用纯注解方式，不依赖 XML 映射文件
 *
 * @author rose
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties(BillingProperties.class)
@MapperScan("io.github.rosestack.billing.repository")
@ConditionalOnProperty(prefix = "rose.billing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BillingAutoConfiguration {

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 多租户插件
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new BillingTenantLineHandler());
        interceptor.addInnerInterceptor(tenantInterceptor);
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        log.info("MyBatis Plus 拦截器配置完成");
        return interceptor;
    }

    /**
     * 元数据处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new BillingMetaObjectHandler();
    }
}
