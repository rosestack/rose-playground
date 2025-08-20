package io.github.rosestack.spring.boot.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import io.github.rosestack.spring.boot.mybatis.audit.RoseMetaObjectHandler;
import io.github.rosestack.spring.boot.mybatis.permission.RoseDataPermissionHandler;
import io.github.rosestack.spring.boot.mybatis.tenant.RoseTenantLineHandler;
import io.github.rosestack.spring.factory.YmlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

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
@PropertySource(value = "classpath:application-rose-mybatis.yaml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.mybatis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MybatisProperties.class)
@ConditionalOnClass({DataSource.class, MybatisPlusInterceptor.class})
@AutoConfiguration
@Import({PermissionConfig.class, EncryptionConfig.class, TenantConfig.class})
public class MybatisAutoConfig {
	private final MybatisProperties properties;

	@PostConstruct
	public void init() {
		// 动态 SQL 智能优化支持本地缓存加速解析，更完善的租户复杂 XML 动态 SQL 支持，静态注入缓存
		JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(
			(cache) -> cache.maximumSize(1024).expireAfterWrite(5, TimeUnit.SECONDS)));

		log.info("Rose Mybatis 自动配置已启用");
	}

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(
		ObjectProvider<RoseTenantLineHandler> tenantLineHandlerObjectProvider,
		ObjectProvider<RoseDataPermissionHandler> dataPermissionHandlerObjectProvider) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 多租户插件（必须放在第一位）
		if (tenantLineHandlerObjectProvider.getIfAvailable() != null) {
			TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
			tenantInterceptor.setTenantLineHandler(tenantLineHandlerObjectProvider.getIfAvailable());
			interceptor.addInnerInterceptor(tenantInterceptor);
			log.info("启用多租户插件，租户字段: {}", properties.getTenant().getColumn());
		}

		if (dataPermissionHandlerObjectProvider.getIfAvailable() != null) {
			DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();
			dataPermissionInterceptor.setDataPermissionHandler(dataPermissionHandlerObjectProvider.getIfAvailable());
			interceptor.addInnerInterceptor(dataPermissionInterceptor);
			log.info("启用数据权限插件, 缓存时间: {} 分钟", properties.getPermission().getCache().getExpireMinutes());
		}

		if (properties.getPagination().isEnabled()) {
			PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor(properties.getPagination().getDbType());
			pageInterceptor.setMaxLimit(properties.getPagination().getMaxPageSize());
			interceptor.addInnerInterceptor(pageInterceptor);
			log.info("启用 {} 数据库分页插件，分页大小最大限制: {}", pageInterceptor.getDbType(), properties.getPagination().getMaxPageSize());
		}

		interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
		interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
		return interceptor;
	}

	@Bean
	@ConditionalOnMissingBean(MetaObjectHandler.class)
	@ConditionalOnProperty(
		prefix = "rose.mybatis.meta-object",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true)
	public MetaObjectHandler roseMetaObjectHandler() {
		log.info("启用元数据处理器");
		return new RoseMetaObjectHandler(properties);
	}

	@Bean
	@ConditionalOnMissingBean(ISqlInjector.class)
	public ISqlInjector sqlInjector() {
		return new DefaultSqlInjector();
	}
}
