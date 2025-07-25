package io.github.rosestack.i18n.spring.boot.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


class ConditionalOnI18nEnabledTests {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner();
    }

    @Test
    void shouldEnableI18nByDefault() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .run(context -> assertThat(context).hasBean("a"));
    }

    @Test
    void shouldEnableI18nWhenPropertyIsTrue() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .withPropertyValues("rose.i18n.enabled=true")
                .run(context -> assertThat(context).hasBean("a"));
    }

    @Test
    void shouldDisableI18nWhenPropertyIsFalse() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .withPropertyValues("rose.i18n.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean("a"));
    }

    @Test
    void shouldDisableI18nWhenMissingClass() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .withPropertyValues("rose.i18n.enabled=true")
                .withClassLoader(new FilteredClassLoader(
                        "io.github.rosestack.i18n.I18nMessageSource",
                        "io.github.rosestack.i18n.spring.annotation.EnableI18n"))
                .run(context -> assertThat(context).doesNotHaveBean("a"));
    }

    @Configuration
    static class Config {

        @Bean
        @ConditionalOnI18nEnabled
        public String a() {
            return "a";
        }
    }
}
