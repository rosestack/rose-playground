package io.github.rosestack.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.toolkit.NetUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import io.github.rosestack.mybatis.datapermission.DataPermissionHandler;
import io.github.rosestack.mybatis.datapermission.DefaultDataPermissionHandler;
import io.github.rosestack.mybatis.encryption.DefaultFieldEncryptor;
import io.github.rosestack.mybatis.encryption.FieldEncryptor;
import io.github.rosestack.mybatis.handler.RoseMetaObjectHandler;
import io.github.rosestack.mybatis.handler.RoseTenantLineHandler;
import io.github.rosestack.mybatis.interceptor.DataPermissionInterceptor;
import io.github.rosestack.mybatis.interceptor.FieldEncryptionInterceptor;
import io.github.rosestack.mybatis.interceptor.SensitiveFieldInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

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
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnClass({DataSource.class, MybatisPlusInterceptor.class})
@ConditionalOnProperty(prefix = "rose.mybatis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseMybatisProperties.class)
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
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("初始化 Rose MyBatis Plus 拦截器");
        
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 多租户插件（必须放在第一位）
        if (properties.getTenant().isEnabled()) {
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(roseTenantLineHandler());
            interceptor.addInnerInterceptor(tenantInterceptor);
            log.info("启用多租户插件，租户字段: {}", properties.getTenant().getColumn());
        }

        // 2. 分页插件
        if (properties.getPagination().isEnabled()) {
            PaginationInnerInterceptor paginationInterceptor = createPaginationInterceptor();
            interceptor.addInnerInterceptor(paginationInterceptor);
            log.info("启用分页插件，最大限制: {}", properties.getPagination().getMaxLimit());
        }

        // 3. 乐观锁插件
        if (properties.getOptimisticLock().isEnabled()) {
            OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
            interceptor.addInnerInterceptor(optimisticLockerInterceptor);
            log.info("启用乐观锁插件，版本字段: {}", properties.getOptimisticLock().getColumn());
        }



        log.info("Rose MyBatis Plus 拦截器初始化完成");
        return interceptor;
    }

    /**
     * 租户处理器
     *
     * @return 租户处理器实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.mybatis.tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RoseTenantLineHandler roseTenantLineHandler() {
        log.info("初始化租户处理器");
        return new RoseTenantLineHandler(properties);
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
     * 字段加密器
     *
     * @return 字段加密器实例
     */
    @Bean
    @ConditionalOnMissingBean(FieldEncryptor.class)
    @ConditionalOnProperty(prefix = "rose.mybatis.encryption", name = "enabled", havingValue = "true")
    public FieldEncryptor fieldEncryptor() {
        log.info("初始化字段加密器");
        return new DefaultFieldEncryptor(properties);
    }

    /**
     * 数据权限处理器
     *
     * @return 数据权限处理器实例
     */
    @Bean
    @ConditionalOnMissingBean(DataPermissionHandler.class)
    @ConditionalOnProperty(prefix = "rose.mybatis.data-permission", name = "enabled", havingValue = "true")
    public DataPermissionHandler dataPermissionHandler() {
        log.info("初始化数据权限处理器");
        return new DefaultDataPermissionHandler(properties);
    }

    /**
     * 字段加密拦截器
     * <p>
     * 注册 MyBatis 拦截器到 SqlSessionFactory
     * </p>
     */
    @Autowired(required = false)
    public void addInterceptors(SqlSessionFactory sqlSessionFactory) {
        // 字段加密拦截器
        if (properties.getEncryption().isEnabled()) {
            FieldEncryptionInterceptor encryptionInterceptor = new FieldEncryptionInterceptor(fieldEncryptor());
            sqlSessionFactory.getConfiguration().addInterceptor(encryptionInterceptor);
            log.info("已注册字段加密拦截器，默认算法: {}", properties.getEncryption().getDefaultAlgorithm());
        }

        // 数据权限拦截器
        if (properties.getDataPermission().isEnabled()) {
            DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor(dataPermissionHandler());
            sqlSessionFactory.getConfiguration().addInterceptor(dataPermissionInterceptor);
            log.info("已注册数据权限拦截器，默认字段: {}", properties.getDataPermission().getDefaultField());
        }

        // 敏感字段脱敏拦截器
        if (properties.getDesensitization().isEnabled()) {
            SensitiveFieldInterceptor sensitiveFieldInterceptor = new SensitiveFieldInterceptor(properties);
            sqlSessionFactory.getConfiguration().addInterceptor(sensitiveFieldInterceptor);
            log.info("已注册敏感字段脱敏拦截器，环境: {}", properties.getDesensitization().getEnvironments());
        }
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
            // 当页码小于1时，自动跳转到第1页
            // 当页码大于总页数时，自动跳转到最后一页
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
            try {
                return DbType.valueOf(configDbType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的数据库类型配置: {}，使用默认的 MySQL", configDbType);
            }
        }

        // 默认使用 MySQL
        return DbType.MYSQL;
    }

    @Bean
    @ConditionalOnMissingBean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }

    /**
     * SQL 性能监控配置
     * <p>
     * 当启用性能监控时，配置 P6Spy 进行 SQL 监控。
     * 这是一个可选的配置，需要引入 p6spy 依赖。
     * </p>
     */
    @Configuration
    @ConditionalOnClass(name = "com.p6spy.engine.spy.P6DataSource")
    @ConditionalOnProperty(prefix = "rose.mybatis.performance", name = "enabled", havingValue = "true")
    static class PerformanceConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public PerformanceMonitoringConfiguration performanceMonitoringConfiguration(RoseMybatisProperties properties) {
            log.info("启用 SQL 性能监控，慢查询阈值: {}ms", properties.getPerformance().getSlowSqlThreshold());
            return new PerformanceMonitoringConfiguration(properties);
        }
    }

    /**
     * 性能监控配置类
     */
    static class PerformanceMonitoringConfiguration {
        
        private final RoseMybatisProperties properties;

        public PerformanceMonitoringConfiguration(RoseMybatisProperties properties) {
            this.properties = properties;
            configureP6Spy();
        }

        /**
         * 配置 P6Spy
         */
        private void configureP6Spy() {
            // 这里可以添加 P6Spy 的配置逻辑
            // 例如设置慢查询阈值、SQL 格式化等
            System.setProperty("p6spy.config.executionThreshold", 
                    String.valueOf(properties.getPerformance().getSlowSqlThreshold()));
            
            if (properties.getPerformance().isFormatSql()) {
                System.setProperty("p6spy.config.logMessageFormat", 
                        "com.p6spy.engine.spy.appender.CustomLineFormat");
            }
            
            log.info("P6Spy 配置完成");
        }
    }
}
