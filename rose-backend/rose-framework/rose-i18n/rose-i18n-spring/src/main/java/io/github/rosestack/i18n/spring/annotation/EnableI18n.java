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

import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.spring.I18nConstants;
import io.github.rosestack.i18n.spring.beans.I18nMessageSourceFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.annotation.*;

import static io.github.rosestack.i18n.I18nMessageSource.COMMON_SOURCE;

/**
 * Enables the extension for Spring Internationalisation.
 * <p>
 * The feature could be disabled by the Spring property if
 * {@link I18nConstants#ENABLED_PROPERTY_NAME "rose.i18n.enabled"} is <code>false</code>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nImportBeanDefinitionRegistrar
 * @see I18nMessageSourceFactoryBean
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
@Import(I18nImportBeanDefinitionRegistrar.class)
public @interface EnableI18n {

    /**
     * Declares the sources of the {@link I18nMessageSource} to register the {@link I18nMessageSourceFactoryBean}
     * Spring Beans whose names are composed by their source content appending "ServiceMessageSource", the default value
     * is {@value I18nMessageSource#COMMON_SOURCE "common"} indicates that the named "commonServiceMessageSource"
     * Spring Bean will be registered.
     * <p>
     * Besides the attribute value, the sources will be extended from the Spring property whose name is
     * {@link I18nConstants#SOURCES_PROPERTY_NAME "rose.i18n.sources"}.
     * <p>
     * Finally, all sourced {@link I18nMessageSource} Spring Beans as the members will be composited into
     * a Primary Spring Bean named {@link I18nConstants#I18N_MESSAGE_SOURCE_BEAN_NAME "serviceMessageSource"}.
     *
     * @return {@link I18nMessageSource#COMMON_SOURCE "common"} as the default
     * @see I18nConstants#SOURCES_PROPERTY_NAME
     * @see I18nConstants#I18N_MESSAGE_SOURCE_BEAN_NAME
     */
    String[] sources() default {COMMON_SOURCE};

    /**
     * Whether to expose {@link I18nConstants#I18N_MESSAGE_SOURCE_BEAN_NAME the primary Spring Bean}
     * {@link I18nMessageSource} as the {@link MessageSource}
     *
     * @return <code>true</code> as default
     * @see AbstractApplicationContext#MESSAGE_SOURCE_BEAN_NAME
     * @see MessageSource
     */
    boolean exposeMessageSource() default true;

    String defaultLocale() default "en";

    String[] supportedLocales() default {"en", "zh_CN"};
}
