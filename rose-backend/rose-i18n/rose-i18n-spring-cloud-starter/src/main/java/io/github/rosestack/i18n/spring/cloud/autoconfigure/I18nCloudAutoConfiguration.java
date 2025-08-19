package io.github.rosestack.i18n.spring.cloud.autoconfigure;

import io.github.rosestack.i18n.spring.boot.condition.ConditionalOnI18nEnabled;
import io.github.rosestack.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * I18n Auto-Configuration for Spring Cloud
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ConditionalOnI18nEnabled
@ConditionalOnClass(
        name = {
            "org.springframework.cloud.context.environment.EnvironmentChangeEvent", // spring-cloud-context
        })
@Import({ReloadableResourceServiceMessageSourceListener.class})
public class I18nCloudAutoConfiguration {}
