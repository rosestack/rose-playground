package io.github.rosestack.i18n.spring.cloud.integration;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.spring.boot.actuate.I18nEndpoint;
import io.github.rosestack.i18n.spring.boot.actuate.autoconfigure.I18nEndpointAutoConfiguration;
import io.github.rosestack.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import io.github.rosestack.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Locale;

import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(
	classes = MessageSourceIntegrationTest.Config.class,
	properties = {"management.endpoint.i18n.enabled=true", "management.endpoints.web.exposure.include=*"})
class MessageSourceIntegrationTest {

	@Test
	void shouldGetMessageWhenChangeMessageSourceProperty(ApplicationContext applicationContext) {
		String key = "common.i18n_messages_en.properties";
		I18nMessageSource serviceMessageSource =
			applicationContext.getBean("commonI18nMessageSource", I18nMessageSource.class);

		assertThat(serviceMessageSource.getMessage("a", Locale.ENGLISH)).isEqualTo("a");
		ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
		environment.getPropertySources().addLast(new MapPropertySource("test", Maps.newHashMap(key, "a=a.2024")));

		applicationContext.publishEvent(new EnvironmentChangeEvent(Sets.set(key)));

		assertThat(serviceMessageSource.getMessage("a", Locale.ENGLISH)).isEqualTo("a.2024");

		DocumentContext parse = using(Configuration.defaultConfiguration())
			.parse(applicationContext.getBean(I18nEndpoint.class).invoke());
		MatcherAssert.assertThat(
			parse, hasJsonPath("$.['common.i18n_messages_en.properties'].['a']", equalTo("a.2024")));
	}

	@ImportAutoConfiguration(
		classes = {
			I18nAutoConfiguration.class,
			I18nEndpointAutoConfiguration.class,
			PropertyPlaceholderAutoConfiguration.class
		})
	@Import(value = {ReloadableResourceServiceMessageSourceListener.class})
	static class Config {
	}
}
