/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.rosestack.i18n.spring.annotation;

import io.github.rosestack.i18n.spring.*;
import io.github.rosestack.i18n.spring.context.I18nApplicationListener;
import io.github.rosestack.i18n.spring.context.MessageSourceAdapter;
import io.github.rosestack.spring.bean.BeanRegistrar;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.context.support.AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * I18n {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ImportBeanDefinitionRegistrar
 * @see EnableI18n
 * @since 1.0.0
 */
public class I18nImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableI18n.class;

    private static Logger logger = LoggerFactory.getLogger(ANNOTATION_TYPE);

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (isEnabled()) {
            AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
            registerI18nMessageSourceBeanDefinitions(attributes, registry);
            registerMessageSourceAdapterBeanDefinition(attributes, registry);
            registerI18nApplicationListenerBeanDefinition(registry);
            registerBeanPostProcessorBeanDefinitions(registry);
        }
    }

    private void registerI18nMessageSourceBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        Set<String> sources = resolveSources(attributes);
        Locale defaultLocale = resolveDefaultLocale(attributes);
        Set<Locale> supportedLocales = resolveSupportedLocales(attributes);

        for (String source : sources) {
            String beanName = source + "I18nMessageSource";
            BeanDefinition beanDefinition = rootBeanDefinition(I18nMessageSourceFactoryBean.class)
                    .addConstructorArgValue(source)
                    .addPropertyValue("defaultLocale", defaultLocale)
                    .addPropertyValue("supportedLocales", supportedLocales)
                    .getBeanDefinition();
            registry.registerBeanDefinition(beanName, beanDefinition);
        }

        // Register DelegatingServiceMessageSource as the Spring Primary Bean
        BeanDefinition primaryBeanDefinition =
                rootBeanDefinition(DelegatingI18nMessageSource.class)
                        .setPrimary(true)
                        .getBeanDefinition();
        registry.registerBeanDefinition(I18nConstants.I18N_MESSAGE_SOURCE_BEAN_NAME, primaryBeanDefinition);
    }

    private Set<String> resolveSources(AnnotationAttributes attributes) {
        Set<String> sources = new LinkedHashSet<>();
        initSources(sources, () -> environment.getProperty(I18nConstants.SOURCES_PROPERTY_NAME, String[].class, ArrayUtils.EMPTY_STRING_ARRAY));
        initSources(sources, () -> attributes.getStringArray("sources"));
        return sources;
    }

    private void initSources(Set<String> sources, Supplier<String[]> sourcesSupplier) {
        for (String source : sourcesSupplier.get()) {
            sources.add(environment.resolvePlaceholders(source));
        }
    }

    /**
     * 解析默认 Locale
     */
    private Locale resolveDefaultLocale(AnnotationAttributes attributes) {
        String defaultLocaleStr = attributes.getString("defaultLocale");
        if (defaultLocaleStr != null && !defaultLocaleStr.trim().isEmpty()) {
            defaultLocaleStr = environment.resolvePlaceholders(defaultLocaleStr);
            return StringUtils.parseLocale(defaultLocaleStr);
        }
        return null;
    }

    /**
     * 解析支持的 Locales
     */
    private Set<Locale> resolveSupportedLocales(AnnotationAttributes attributes) {
        String[] locales = attributes.getStringArray("supportedLocales");

        Set<Locale> supportedLocales = Arrays.stream(locales)
                .map(locale -> environment.resolvePlaceholders(locale))
                .map(StringUtils::parseLocale).collect(Collectors.toSet());
        return Collections.unmodifiableSet(supportedLocales);
    }

    private void registerMessageSourceAdapterBeanDefinition(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean exposeMessageSource = attributes.getBoolean("exposeMessageSource");
        if (exposeMessageSource) {
            BeanRegistrar.registerBeanDefinition(registry, MESSAGE_SOURCE_BEAN_NAME, MessageSourceAdapter.class);
        }
    }

    private void registerI18nApplicationListenerBeanDefinition(BeanDefinitionRegistry registry) {
        BeanRegistrar.registerBeanDefinition(registry, I18nApplicationListener.class);
    }

    private void registerBeanPostProcessorBeanDefinitions(BeanDefinitionRegistry registry) {
        BeanRegistrar.registerBeanDefinition(registry, I18nBeanPostProcessor.class);
        BeanRegistrar.registerBeanDefinition(registry, I18nMessageSourceBeanLifecyclePostProcessor.class);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private boolean isEnabled() {
        String propertyName = I18nConstants.ENABLED_PROPERTY_NAME;
        boolean enabled = environment.getProperty(propertyName, boolean.class, I18nConstants.DEFAULT_ENABLED);
        logger.debug("Rose i18n is {} , cased by the Spring property[name : '{}', default value : '{}']",
                enabled ? "enabled" : "disabled",
                propertyName,
                I18nConstants.DEFAULT_ENABLED);
        return enabled;
    }
}
