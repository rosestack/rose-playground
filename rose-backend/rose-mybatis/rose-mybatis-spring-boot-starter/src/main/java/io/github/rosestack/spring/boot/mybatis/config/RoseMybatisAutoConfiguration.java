package io.github.rosestack.spring.boot.mybatis.config;

import static io.github.rosestack.mybatis.MybatisConstants.TENANT_ID_FILTER_ORDER;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.encryption.FieldEncryptor;
import io.github.rosestack.encryption.hash.HashService;
import io.github.rosestack.mybatis.provider.CurrentTenantProvider;
import io.github.rosestack.mybatis.provider.CurrentUserProvider;
import io.github.rosestack.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.core.util.FilterRegistrationBeanUtils;
import io.github.rosestack.spring.boot.mybatis.audit.RoseMetaObjectHandler;
import io.github.rosestack.spring.boot.mybatis.encryption.FieldEncryptionInterceptor;
import io.github.rosestack.spring.boot.mybatis.permission.DataPermissionMetrics;
import io.github.rosestack.spring.boot.mybatis.permission.RoseDataPermissionHandler;
import io.github.rosestack.spring.boot.mybatis.permission.controller.DataPermissionController;
import io.github.rosestack.spring.boot.mybatis.permission.provider.DataPermissionProviderManager;
import io.github.rosestack.spring.boot.mybatis.permission.service.DataPermissionService;
import io.github.rosestack.spring.boot.mybatis.provider.DefaultCurrentTenantProvider;
import io.github.rosestack.spring.boot.mybatis.provider.DefaultCurrentUserProvider;
import io.github.rosestack.spring.boot.mybatis.tenant.RoseTenantLineHandler;
import io.github.rosestack.spring.boot.mybatis.tenant.TenantIdFilter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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

/**
 * Rose MyBatis Plus 自动配置类
 *
 * <p>提供 MyBatis Plus 的自动配置和增强功能，包括： - 多租户支持 - 分页插件 - 乐观锁插件 - 字段自动填充 - 逻辑删除
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
@Import({DataPermissionController.class, DataPermissionService.class})
public class RoseMybatisAutoConfiguration {
    private final RoseMybatisProperties properties;
    private final ObjectProvider<DataSource> dataSourceProvider;

    @PostConstruct
    public void init() {
        // 动态 SQL 智能优化支持本地缓存加速解析，更完善的租户复杂 XML 动态 SQL 支持，静态注入缓存
        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(
                (cache) -> cache.maximumSize(1024).expireAfterWrite(5, TimeUnit.SECONDS)));

        log.info("Rose Mybatis 自动配置已启用");
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            @Autowired(required = false) RoseTenantLineHandler roseTenantLineHandler,
            @Autowired(required = false) RoseDataPermissionHandler roseDataPermissionHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件（必须放在第一位）
        if (properties.getTenant().isEnabled() && roseTenantLineHandler != null) {
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(roseTenantLineHandler);
            interceptor.addInnerInterceptor(tenantInterceptor);
            log.info("启用多租户插件，租户字段: {}", properties.getTenant().getColumn());
        }

        OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
        interceptor.addInnerInterceptor(optimisticLockerInterceptor);
        log.info("启用乐观锁插件");

        if (properties.getDataPermission().isEnabled() && roseDataPermissionHandler != null) {
            DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();
            dataPermissionInterceptor.setDataPermissionHandler(roseDataPermissionHandler);
            interceptor.addInnerInterceptor(dataPermissionInterceptor);
            log.info(
                    "启用数据权限插件, 缓存时间: {} 分钟",
                    properties.getDataPermission().getCache().getExpireMinutes());
        }

        if (properties.getPagination().isEnabled()) {
            PaginationInnerInterceptor paginationInterceptor = createPaginationInterceptor();
            interceptor.addInnerInterceptor(paginationInterceptor);
            log.info(
                    "启用 {} 数据库分页插件，分页大小最大限制: {}",
                    paginationInterceptor.getDbType(),
                    properties.getPagination().getMaxLimit());
        }
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(TenantLineHandler.class)
    public TenantLineHandler roseTenantLineHandler() {
        return new RoseTenantLineHandler(properties);
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    @ConditionalOnProperty(
            prefix = "rose.mybatis.field-fill",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public MetaObjectHandler roseMetaObjectHandler() {
        log.info("启用元数据处理器");
        return new RoseMetaObjectHandler(properties);
    }

    /**
     * 字段加密拦截器
     */
    @Bean
    @ConditionalOnMissingBean(FieldEncryptionInterceptor.class)
    @ConditionalOnProperty(
            prefix = "rose.mybatis.encryption",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnBean(FieldEncryptor.class)
    public FieldEncryptionInterceptor fieldEncryptionInterceptor(
            FieldEncryptor fieldEncryptor,
            HashService hashService,
            @Autowired(required = false) MeterRegistry registry) {
        log.info("启用字段加密解密拦截器，默认算法: AES");
        return new FieldEncryptionInterceptor(fieldEncryptor, hashService, registry);
    }

    @Bean
    @ConditionalOnMissingBean(DataPermissionProviderManager.class)
    @ConditionalOnProperty(
            prefix = "rose.mybatis.data-permission",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public DataPermissionProviderManager dataPermissionProviderManager(ApplicationContext applicationContext) {
        return new DataPermissionProviderManager(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(DataPermissionHandler.class)
    @ConditionalOnProperty(
            prefix = "rose.mybatis.data-permission",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RoseDataPermissionHandler roseDataPermissionHandler(
            DataPermissionProviderManager providerManager,
            Cache<String, List<String>> dataPermissionCache,
            CurrentUserProvider currentUserProvider,
            @Autowired(required = false) MeterRegistry registry) {
        return new RoseDataPermissionHandler(providerManager, dataPermissionCache, currentUserProvider, registry);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean
    public DataPermissionMetrics dataPermissionMetrics(MeterRegistry registry) {
        return new DataPermissionMetrics(registry);
    }

    // Caffeine 缓存 Bean
    @Bean
    @ConditionalOnMissingBean(name = "dataPermissionCache")
    @ConditionalOnProperty(
            prefix = "rose.mybatis.data-permission",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public Cache<String, java.util.List<String>> dataPermissionCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.getDataPermission().getCache().getMaxPermissionCacheSize())
                .expireAfterWrite(properties.getDataPermission().getCache().getExpireMinutes(), TimeUnit.MINUTES)
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.mybatis.tenant", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<TenantIdFilter> tenantIdFilter() {
        return FilterRegistrationBeanUtils.createFilterBean(
                new TenantIdFilter(properties.getTenant().getIgnoreTablePrefixes()), TENANT_ID_FILTER_ORDER);
    }

    @Bean
    @ConditionalOnMissingBean(ISqlInjector.class)
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }

    @Bean
    @ConditionalOnMissingBean(CurrentUserProvider.class)
    public CurrentUserProvider currentUserProvider() {
        return new DefaultCurrentUserProvider();
    }

    @Bean
    @ConditionalOnMissingBean(CurrentTenantProvider.class)
    public CurrentTenantProvider currentTenantProvider() {
        return new DefaultCurrentTenantProvider();
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
     *
     * <p>根据配置或者数据源自动检测数据库类型。 如果配置中指定了数据库类型，则使用配置的类型。 否则尝试从数据源中检测。
     *
     * @return 数据库类型
     */
    private DbType detectDbType() {
        String configDbType = properties.getPagination().getDbType();

        if (configDbType != null && !configDbType.isEmpty()) {
            return DbType.getDbType(configDbType);
        }
        // 尝试从 DataSource URL 判定
        try {
            DataSource ds = dataSourceProvider != null ? dataSourceProvider.getIfAvailable() : null;
            if (ds != null) {
                try (java.sql.Connection conn = ds.getConnection()) {
                    String url = conn.getMetaData().getURL();
                    if (url != null) {
                        String u = url.toLowerCase();
                        if (u.contains(":mysql:")) return DbType.MYSQL;
                        if (u.contains(":postgresql:")) return DbType.POSTGRE_SQL;
                        if (u.contains(":oracle:")) return DbType.ORACLE;
                        if (u.contains(":sqlserver:")) return DbType.SQL_SERVER;
                        if (u.contains(":h2:")) return DbType.H2;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        // 默认使用 MySQL
        return DbType.MYSQL;
    }
}
