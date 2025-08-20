package io.github.rosestack.i18n.spring.boot.actuate.autoconfigure;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.github.rosestack.i18n.spring.boot.actuate.I18nEndpoint;
import io.github.rosestack.i18n.spring.boot.config.I18nAutoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class I18nEndpointAutoConfigurationTest {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(I18nAutoConfig.class, I18nEndpointAutoConfiguration.class));
    }

    @Test
    void shouldHaveEndpointBean() {
        applicationContextRunner
                .withPropertyValues("management.endpoints.web.exposure.include=i18n")
                .run(context -> assertThat(context).hasSingleBean(I18nEndpoint.class));
    }

    @Test
    void shouldNotHaveEndpointBean() {
        applicationContextRunner.run(context -> assertThat(context).doesNotHaveBean(I18nEndpoint.class));
    }

    @Test
    void shouldNotHaveEndpointBeanWhenEnablePropertyIsFalse() {
        applicationContextRunner
                .withPropertyValues("management.endpoint.i18n.enabled=false")
                .withPropertyValues("management.endpoints.web.exposure.include=*")
                .run(context -> assertThat(context).doesNotHaveBean(I18nEndpoint.class));
    }
}
