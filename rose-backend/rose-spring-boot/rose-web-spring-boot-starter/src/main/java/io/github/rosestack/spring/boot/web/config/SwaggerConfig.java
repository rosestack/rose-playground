package io.github.rosestack.spring.boot.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Swagger 3 配置
 *
 * <p>集成 SpringDoc OpenAPI 3，支持 JWT Token 和 OAuth2 Token 认证
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.web.swagger", name = "enabled", havingValue = "true")
public class SwaggerConfig {

	private final RoseWebProperties roseWebProperties;

	/**
	 * 配置 OpenAPI 文档
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		RoseWebProperties.Swagger swaggerConfig = roseWebProperties.getSwagger();

		OpenAPI openAPI = new OpenAPI().info(buildInfo(swaggerConfig)).servers(buildServers(swaggerConfig));

		// 根据配置添加安全认证
		if (swaggerConfig.getSecurity().isEnabled()) {
			openAPI.components(buildComponents(swaggerConfig)).security(buildSecurityRequirements(swaggerConfig));
		}

		log.info("启用 OpenAPI 文档: {}", openAPI.getInfo().getTitle());

		return openAPI;
	}

	/**
	 * 构建 API 信息
	 */
	private Info buildInfo(RoseWebProperties.Swagger swaggerConfig) {
		return new Info()
			.title(swaggerConfig.getTitle())
			.description(swaggerConfig.getDescription())
			.version(swaggerConfig.getVersion())
			.contact(new Contact()
				.name(swaggerConfig.getContact().getName())
				.email(swaggerConfig.getContact().getEmail())
				.url(swaggerConfig.getContact().getUrl()))
			.license(new License()
				.name(swaggerConfig.getLicense().getName())
				.url(swaggerConfig.getLicense().getUrl()));
	}

	/**
	 * 构建服务器列表
	 */
	private List<Server> buildServers(RoseWebProperties.Swagger swaggerConfig) {
		return swaggerConfig.getServers().stream()
			.map(serverConfig -> new Server().url(serverConfig.getUrl()).description(serverConfig.getDescription()))
			.toList();
	}

	/**
	 * 构建安全组件
	 */
	private Components buildComponents(RoseWebProperties.Swagger swaggerConfig) {
		Components components = new Components();
		RoseWebProperties.Swagger.Security security = swaggerConfig.getSecurity();

		// JWT Token 认证
		if (security.getJwt().isEnabled()) {
			components.addSecuritySchemes(
				"JWT",
				new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("JWT Token 认证"));
		}

		// OAuth2 认证
		if (security.getOauth2().isEnabled()) {
			SecurityScheme oauth2Scheme =
				new SecurityScheme().type(SecurityScheme.Type.OAUTH2).description("OAuth2 认证");

			// 根据配置的流程类型添加相应的流程
			io.swagger.v3.oas.models.security.OAuthFlows flows = new io.swagger.v3.oas.models.security.OAuthFlows();

			if (security.getOauth2().getAuthorizationCode().isEnabled()) {
				flows.authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
					.authorizationUrl(
						security.getOauth2().getAuthorizationCode().getAuthorizationUrl())
					.tokenUrl(security.getOauth2().getAuthorizationCode().getTokenUrl())
					.refreshUrl(security.getOauth2().getAuthorizationCode().getRefreshUrl())
					.scopes(new io.swagger.v3.oas.models.security.Scopes()));
			}

			if (security.getOauth2().getClientCredentials().isEnabled()) {
				flows.clientCredentials(new io.swagger.v3.oas.models.security.OAuthFlow()
					.tokenUrl(security.getOauth2().getClientCredentials().getTokenUrl())
					.refreshUrl(security.getOauth2().getClientCredentials().getRefreshUrl())
					.scopes(new io.swagger.v3.oas.models.security.Scopes()));
			}

			oauth2Scheme.flows(flows);
			components.addSecuritySchemes("OAuth2", oauth2Scheme);
		}

		// API Key 认证
		if (security.getApiKey().isEnabled()) {
			components.addSecuritySchemes(
				"ApiKey",
				new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.valueOf(
						security.getApiKey().getIn().toUpperCase()))
					.name(security.getApiKey().getName())
					.description("API Key 认证"));
		}

		return components;
	}

	/**
	 * 构建安全要求
	 */
	private List<SecurityRequirement> buildSecurityRequirements(RoseWebProperties.Swagger swaggerConfig) {
		RoseWebProperties.Swagger.Security security = swaggerConfig.getSecurity();
		List<SecurityRequirement> requirements = new java.util.ArrayList<>();

		if (security.getJwt().isEnabled()) {
			requirements.add(new SecurityRequirement().addList("JWT"));
		}

		if (security.getOauth2().isEnabled()) {
			requirements.add(new SecurityRequirement().addList("OAuth2"));
		}

		if (security.getApiKey().isEnabled()) {
			requirements.add(new SecurityRequirement().addList("ApiKey"));
		}

		return requirements;
	}

	/**
	 * 系统管理 API 分组
	 */
	@Bean
	@ConditionalOnProperty(prefix = "rose.web.swagger.groups.system", name = "enabled", havingValue = "true")
	public GroupedOpenApi systemApi() {
		return GroupedOpenApi.builder()
			.group("system")
			.displayName("系统管理")
			.pathsToMatch("/api/system/**")
			.build();
	}

	/**
	 * 业务 API 分组
	 */
	@Bean
	@ConditionalOnProperty(prefix = "rose.web.swagger.groups.business", name = "enabled", havingValue = "true")
	public GroupedOpenApi businessApi() {
		return GroupedOpenApi.builder()
			.group("business")
			.displayName("业务接口")
			.pathsToMatch("/api/business/**", "/api/v1/**")
			.build();
	}

	/**
	 * 公共 API 分组
	 */
	@Bean
	@ConditionalOnProperty(prefix = "rose.web.swagger.groups.public", name = "enabled", havingValue = "true")
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("public")
			.displayName("公共接口")
			.pathsToMatch("/api/public/**")
			.build();
	}

	/**
	 * 内部 API 分组
	 */
	@Bean
	@ConditionalOnProperty(prefix = "rose.web.swagger.groups.internal", name = "enabled", havingValue = "true")
	public GroupedOpenApi internalApi() {
		return GroupedOpenApi.builder()
			.group("internal")
			.displayName("内部接口")
			.pathsToMatch("/api/internal/**")
			.build();
	}

	/**
	 * 监控 API 分组
	 */
	@Bean
	@ConditionalOnProperty(prefix = "rose.web.swagger.groups.actuator", name = "enabled", havingValue = "true")
	public GroupedOpenApi actuatorApi() {
		return GroupedOpenApi.builder()
			.group("actuator")
			.displayName("监控端点")
			.pathsToMatch("/actuator/**")
			.build();
	}
}
