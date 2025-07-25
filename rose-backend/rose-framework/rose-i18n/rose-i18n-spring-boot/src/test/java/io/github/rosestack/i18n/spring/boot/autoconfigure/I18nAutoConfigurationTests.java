package io.github.rosestack.i18n.spring.boot.autoconfigure;

import io.github.rosestack.i18n.spring.DelegatingI18nMessageSource;
import io.github.rosestack.i18n.spring.I18nBeanPostProcessor;
import io.github.rosestack.i18n.spring.I18nMessageSourceBeanLifecyclePostProcessor;
import io.github.rosestack.i18n.spring.I18nMessageSourceFactoryBean;
import io.github.rosestack.i18n.spring.context.I18nApplicationListener;
import io.github.rosestack.i18n.spring.context.MessageSourceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class I18nAutoConfigurationTests {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner();
    }

    @Test
    void shouldContainServiceMessageSourceFactoryBean() {
        applicationContextRunner.withPropertyValues("spring.application.name=I18nAutoConfigurationTests")
                .withConfiguration(AutoConfigurations.of(I18nAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class))
                .run(context -> {
                    assertThat(context)
                            .hasBean("applicationMessageSource")
                            .getBean("applicationMessageSource")
                            .hasFieldOrPropertyWithValue("source", "I18nAutoConfigurationTests");


                    assertThat(context).getBeans(I18nMessageSourceFactoryBean.class).hasSizeGreaterThanOrEqualTo(1);

                    assertThat(context).getBean("i18nMessageSource").isInstanceOf(DelegatingI18nMessageSource.class);
                    assertThat(context).getBean("messageSource").isInstanceOf(MessageSourceAdapter.class);

                    assertThat(context).hasSingleBean(I18nApplicationListener.class)
                            .hasSingleBean(I18nBeanPostProcessor.class)
                            .hasSingleBean(I18nMessageSourceBeanLifecyclePostProcessor.class);
                });
    }
}
