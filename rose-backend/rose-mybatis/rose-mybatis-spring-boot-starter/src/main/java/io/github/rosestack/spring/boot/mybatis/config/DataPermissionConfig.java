package io.github.rosestack.spring.boot.mybatis.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.mybatis.permission.CurrentUserProvider;
import io.github.rosestack.spring.boot.mybatis.permission.DataPermissionMetrics;
import io.github.rosestack.spring.boot.mybatis.permission.RoseDataPermissionHandler;
import io.github.rosestack.spring.boot.mybatis.permission.controller.DataPermissionController;
import io.github.rosestack.spring.boot.mybatis.permission.provider.DataPermissionProviderManager;
import io.github.rosestack.spring.boot.mybatis.permission.service.DataPermissionService;
import io.github.rosestack.spring.boot.mybatis.permission.provider.DefaultCurrentUserProvider;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@ConditionalOnProperty(
	prefix = "rose.mybatis.permission",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true)
@RequiredArgsConstructor
@Import({DataPermissionController.class, DataPermissionService.class})
public class DataPermissionConfig {
	private final RoseMybatisProperties properties;

	@Bean
	@ConditionalOnMissingBean(DataPermissionProviderManager.class)
	public DataPermissionProviderManager dataPermissionProviderManager(ApplicationContext applicationContext) {
		return new DataPermissionProviderManager(applicationContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public RoseDataPermissionHandler roseDataPermissionHandler(
		DataPermissionProviderManager providerManager,
		CurrentUserProvider currentUserProvider,
		@Autowired(required = false) Cache<String, List<String>> dataPermissionCache,
		@Autowired(required = false) MeterRegistry registry) {
		log.info("启用数据权限处理器");

		return new RoseDataPermissionHandler(providerManager, dataPermissionCache, currentUserProvider, registry);
	}

	@Bean
	@ConditionalOnBean(MeterRegistry.class)
	@ConditionalOnMissingBean
	public DataPermissionMetrics dataPermissionMetrics(MeterRegistry registry) {
		return new DataPermissionMetrics(registry);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(
		prefix = "rose.mybatis.permission.cache",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true)
	public Cache<String, List<String>> dataPermissionCache() {
		log.info("数据权限处理器启用本地缓存, 缓存过期时间: {} 分钟", properties.getPermission().getCache().getExpireMinutes());

		return Caffeine.newBuilder()
			.maximumSize(properties.getPermission().getCache().getMaxPermissionCacheSize())
			.expireAfterWrite(properties.getPermission().getCache().getExpireMinutes(), TimeUnit.MINUTES)
			.build();
	}

	@Bean
	@ConditionalOnMissingBean(CurrentUserProvider.class)
	public CurrentUserProvider currentUserProvider() {
		return new DefaultCurrentUserProvider();
	}
}
