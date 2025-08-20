package io.github.rosestack.spring.boot.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import io.github.rosestack.mybatis.tenant.CurrentTenantProvider;
import io.github.rosestack.spring.boot.mybatis.tenant.DefaultCurrentTenantProvider;
import io.github.rosestack.spring.boot.mybatis.tenant.RoseTenantLineHandler;
import io.github.rosestack.spring.boot.mybatis.tenant.TenantIdFilter;
import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

import static io.github.rosestack.mybatis.MybatisConstants.TENANT_ID_FILTER_ORDER;

@Slf4j
@ConditionalOnProperty(
	prefix = "rose.mybatis.tenant",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true)
public class MybatisTenantConfig {

	@Bean
	@ConditionalOnMissingBean
	public TenantLineHandler tenantLineHandler(MybatisProperties properties) {
		log.info("启用租户处理器, 租户 ID 字段: {}", properties.getTenant().getColumn());

		return new RoseTenantLineHandler(properties);
	}

	@Bean
	@ConditionalOnProperty(prefix = "rose.mybatis.tenant", name = "enabled", havingValue = "true")
	public FilterRegistrationBean<TenantIdFilter> tenantIdFilter() {
		TenantIdFilter filter = new TenantIdFilter(new ArrayList<>());
		FilterRegistrationBean<TenantIdFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
		registrationBean.addUrlPatterns("/*");
		registrationBean.setName(filter.getClass().getSimpleName());
		registrationBean.setOrder(TENANT_ID_FILTER_ORDER);
		return registrationBean;
	}

	@Bean
	@ConditionalOnMissingBean(CurrentTenantProvider.class)
	public CurrentTenantProvider currentTenantProvider() {
		return new DefaultCurrentTenantProvider();
	}
}
