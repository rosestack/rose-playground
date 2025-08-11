package io.github.rosestack.spring.boot.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import io.github.rosestack.core.spring.SpringContextUtils;
import io.github.rosestack.core.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.common.encryption.FieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.hash.HashService;
import io.github.rosestack.spring.boot.mybatis.handler.RoseMetaObjectHandler;
import io.github.rosestack.spring.boot.mybatis.handler.RoseTenantLineHandler;
import io.github.rosestack.spring.boot.mybatis.interceptor.FieldEncryptionInterceptor;
import io.github.rosestack.spring.boot.mybatis.support.datapermission.DataPermissionProviderManager;
import io.github.rosestack.spring.boot.mybatis.support.datapermission.RoseDataPermissionHandler;
import io.github.rosestack.spring.boot.mybatis.support.datapermission.controller.DataPermissionController;
import io.github.rosestack.spring.boot.mybatis.support.datapermission.service.DataPermissionService;
import io.github.rosestack.spring.boot.mybatis.support.tenant.TenantIdFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

import static io.github.rosestack.core.Constants.FilterOrder.TENANT_ID_FILTER_ORDER;

/**
 * Rose MyBatis Plus 自动配置类
 * <p>
 * 提供 MyBatis Plus 的自动配置和增强功能，包括：
 * - 多租户支持
 * - 分页插件
 * - 乐观锁插件
 * - 字段自动填充
 * - 逻辑删除
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@PropertySource(value = "classpath:application-rose-mybatis.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.mybatis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseMybatisProperties.class)
@ConditionalOnClass({DataSource.class, MybatisPlusInterceptor.class})
@AutoConfiguration
@Import({
        DataPermissionController.class,
        DataPermissionService.class
})
public class RoseMybatisAutoConfiguration {
    private final RoseMybatisProperties properties;

    static {
        // 动态 SQL 智能优化支持本地缓存加速解析，更完善的租户复杂 XML 动态 SQL 支持，静态注入缓存
        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(
                (cache) -> cache.maximumSize(1024).expireAfterWrite(5, TimeUnit.SECONDS)));
    }

    @PostConstruct
    public void init() {
        log.info("Rose Mybatis 自动配置已启用");
    }

    /**
     * MyBatis Plus 拦截器配置
     * <p>
     * 配置 MyBatis Plus 的各种拦截器，包括多租户、分页、乐观锁等。
     * 拦截器的顺序很重要，需要按照正确的顺序添加。
     * </p>
     *
     * @return MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(@Autowired(required = false) RoseTenantLineHandler tenantLineHandler,
                                                         @Autowired(required = false) RoseDataPermissionHandler roseDataPermissionHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件（必须放在第一位）
        if (properties.getTenant().isEnabled() && tenantLineHandler != null) {
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(new RoseTenantLineHandler(properties));
            interceptor.addInnerInterceptor(tenantInterceptor);
            log.info("启用多租户插件，租户字段: {}", properties.getTenant().getColumn());
        }

        OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
        interceptor.addInnerInterceptor(optimisticLockerInterceptor);
        log.info("启用乐观锁插件");

        // 数据权限插件
        if (properties.getDataPermission().isEnabled() && roseDataPermissionHandler != null) {
            DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();
            dataPermissionInterceptor.setDataPermissionHandler(roseDataPermissionHandler);
            interceptor.addInnerInterceptor(dataPermissionInterceptor);
            log.info("启用数据权限插件, 缓存时间: {} 分钟", properties.getDataPermission().getCache().getExpireMinutes());
        }

        // 分页插件
        if (properties.getPagination().isEnabled()) {
            PaginationInnerInterceptor paginationInterceptor = createPaginationInterceptor();
            interceptor.addInnerInterceptor(paginationInterceptor);
            log.info("启用 {} 数据库分页插件，分页大小最大限制: {}", paginationInterceptor.getDbType(), properties.getPagination().getMaxLimit());
        }
        return interceptor;
    }

    /**
     * 元数据处理器
     * <p>
     * 提供字段自动填充功能，包括创建时间、更新时间、创建人、更新人等。
     * </p>
     *
     * @return 元数据处理器实例
     */
    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    @ConditionalOnProperty(prefix = "rose.mybatis.field-fill", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MetaObjectHandler roseMetaObjectHandler() {
        log.info("启用元数据处理器");
        return new RoseMetaObjectHandler(properties);
    }

    /**
     * 字段加密拦截器
     */
    @Bean
    @ConditionalOnMissingBean(FieldEncryptionInterceptor.class)
    @ConditionalOnProperty(prefix = "rose.mybatis.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(FieldEncryptor.class)
    public FieldEncryptionInterceptor fieldEncryptionInterceptor(FieldEncryptor fieldEncryptor, HashService hashService) {
        log.info("启用字段加密解密拦截器，默认算法: AES");
        return new FieldEncryptionInterceptor(fieldEncryptor, hashService);
    }

    @Bean
    @ConditionalOnMissingBean(DataPermissionProviderManager.class)
    @ConditionalOnProperty(prefix = "rose.mybatis.data-permission", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataPermissionProviderManager dataPermissionProviderManager(ApplicationContext applicationContext) {
        return new DataPermissionProviderManager(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(RoseDataPermissionHandler.class)
    @ConditionalOnProperty(prefix = "rose.mybatis.data-permission", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RoseDataPermissionHandler roseDataPermissionHandler(DataPermissionProviderManager providerManager) {
        return new RoseDataPermissionHandler(providerManager, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.mybatis.tenant", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<TenantIdFilter> tenantIdFilter() {
        return SpringContextUtils.createFilterBean(new TenantIdFilter(properties.getTenant().getIgnoreTablePrefixes()), TENANT_ID_FILTER_ORDER);
    }

    @Bean
    @ConditionalOnMissingBean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }

    /**
     * 创建分页拦截器
     *
     * @return 分页拦截器实例
     */
    private PaginationInnerInterceptor createPaginationInterceptor() {
        // 自动检测数据库类型
        DbType dbType = detectDbType();

        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(dbType);
        paginationInterceptor.setMaxLimit(properties.getPagination().getMaxLimit());

        // 设置合理化分页
        if (properties.getPagination().isReasonable()) {
            // 当页码小于1时，自动跳转到第1页；当页码大于总页数时，自动跳转到最后一页
            paginationInterceptor.setOptimizeJoin(true);
        }

        return paginationInterceptor;
    }

    /**
     * 自动检测数据库类型
     * <p>
     * 根据配置或者数据源自动检测数据库类型。
     * 如果配置中指定了数据库类型，则使用配置的类型。
     * 否则尝试从数据源中检测。
     * </p>
     *
     * @return 数据库类型
     */
    private DbType detectDbType() {
        String configDbType = properties.getPagination().getDbType();

        if (configDbType != null && !configDbType.isEmpty()) {
            return DbType.getDbType(configDbType);
        }

        // 默认使用 MySQL
        return DbType.MYSQL;
    }
}
