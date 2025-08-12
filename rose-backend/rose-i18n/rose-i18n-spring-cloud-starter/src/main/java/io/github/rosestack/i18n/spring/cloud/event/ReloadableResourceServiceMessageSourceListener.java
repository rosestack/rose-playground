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
package io.github.rosestack.i18n.spring.cloud.event;

import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.ReloadedResourceMessageSource;
import io.github.rosestack.i18n.spring.DelegatingI18nMessageSource;
import io.github.rosestack.i18n.spring.I18nConstants;
import io.github.rosestack.i18n.spring.PropertySourceResourceI18nMessageSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import java.util.Set;

/**
 * * An {@link ApplicationListener} of {@link EnvironmentChangeEvent} to reload {@link
 * I18nMessageSource} dynamically at the runtime.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceResourceI18nMessageSource
 * @see DelegatingI18nMessageSource
 * @see ReloadedResourceMessageSource
 * @see I18nMessageSource
 * @see EnvironmentChangeEvent
 * @since 1.0.0
 */
public class ReloadableResourceServiceMessageSourceListener
        implements SmartInitializingSingleton, ApplicationListener<EnvironmentChangeEvent>, BeanFactoryAware {

    private BeanFactory beanFactory;

    private ReloadedResourceMessageSource reloadedResourceMessageSource;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changedPropertyNames = event.getKeys();
        for (String changedPropertyName : changedPropertyNames) {
            String resource = changedPropertyName;
            if (reloadedResourceMessageSource.canReload(resource)) {
                reloadedResourceMessageSource.reload(resource);
            }
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        // Lookup the primary bean of PropertySourcesServiceMessageSource
        this.reloadedResourceMessageSource = this.beanFactory.getBean(
                I18nConstants.I18N_MESSAGE_SOURCE_BEAN_NAME, ReloadedResourceMessageSource.class);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
