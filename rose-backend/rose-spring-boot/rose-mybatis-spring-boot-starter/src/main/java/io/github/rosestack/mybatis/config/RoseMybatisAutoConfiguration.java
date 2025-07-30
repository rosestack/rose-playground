package io.github.rosestack.mybatis.config;

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
import io.github.rosestack.mybatis.handler.RoseMetaObjectHandler;
import io.github.rosestack.mybatis.handler.RoseTenantLineHandler;
import io.github.rosestack.mybatis.interceptor.AuditLogInterceptor;
import io.github.rosestack.mybatis.interceptor.FieldEncryptionInterceptor;
import io.github.rosestack.mybatis.support.audit.DefaultAuditStorage;
import io.github.rosestack.mybatis.support.datapermission.RoseDataPermissionHandler;
import io.github.rosestack.mybatis.support.encryption.DefaultFieldEncryptor;
import io.github.rosestack.mybatis.support.encryption.hash.HashService;
import io.github.rosestack.mybatis.support.tenant.TenantIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@AutoConfiguration
@ConditionalOnClass({DataSource.class, MybatisPlusInterceptor.class})
@PropertySource(value = "classpath:application-rose-mybatis.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.mybatis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseMybatisProperties.class)
@ComponentScan(basePackages = "io.github.rosestack.mybatis")
public class RoseMybatisAutoConfiguration {
    private final RoseMybatisProperties properties;

    static {
        // 动态 SQL 智能优化支持本地缓存加速解析，更完善的租户复杂 XML 动态 SQL 支持，静态注入缓存
        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(
                (cache) -> cache.maximumSize(1024).expireAfterWrite(5, TimeUnit.SECONDS)));
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
        log.info("启用 MyBatis Plus 拦截器");

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件（必须放在第一位）
        if (properties.getTenant().isEnabled() && tenantLineHandler != null) {
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(new RoseTenantLineHandler(properties));
            interceptor.addInnerInterceptor(tenantInterceptor);
            log.info("启用多租户插件，租户字段: {}", properties.getTenant().getColumn());
        }

        // 乐观锁插件
        if (properties.getOptimisticLock().isEnabled()) {
            OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
            interceptor.addInnerInterceptor(optimisticLockerInterceptor);
            log.info("启用乐观锁插件");
        }

        // 数据权限插件
        if (properties.getDataPermission().isEnabled() && roseDataPermissionHandler != null) {
            DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();
            dataPermissionInterceptor.setDataPermissionHandler(roseDataPermissionHandler);
            interceptor.addInnerInterceptor(dataPermissionInterceptor);
            log.info("启用数据权限插件, 缓存时间: {} 分钟", properties.getDataPermission().getCache(). getExpireMinutes());
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
        log.info("初始化元数据处理器");
        return new RoseMetaObjectHandler(properties);
    }

    /**
     * 字段加密拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.mybatis.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FieldEncryptionInterceptor fieldEncryptionInterceptor(HashService hashService) {
        log.info("启用字段加密解密拦截器，默认算法: AES");
        return new FieldEncryptionInterceptor(new DefaultFieldEncryptor(properties), hashService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.mybatis.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditLogInterceptor auditInterceptor() {
        log.info("启用审计日志拦截器，日志等级: {}", properties.getAudit().getLogLevel());
        return new AuditLogInterceptor(properties, new DefaultAuditStorage());
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.mybatis.tenant", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<TenantIdFilter> tenantIdFilter() {
        return SpringContextUtils.createFilterBean(new TenantIdFilter(), TENANT_ID_FILTER_ORDER);
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
