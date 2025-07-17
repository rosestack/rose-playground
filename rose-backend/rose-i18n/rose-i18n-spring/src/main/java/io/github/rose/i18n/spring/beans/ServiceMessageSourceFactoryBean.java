package io.github.rose.i18n.spring.beans;

import io.github.rose.i18n.AbstractMessageSource;
import io.github.rose.i18n.CompositeMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.ReloadedResourceMessageSource;
import io.github.rose.i18n.spring.context.ResourceMessageSourceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.rose.core.util.BeanUtils.invokeAwareInterfaces;
import static io.github.rose.i18n.util.I18nUtils.findAllMessageSources;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * {@link I18nMessageSource} {@link FactoryBean} Implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ServiceMessageSourceFactoryBean extends CompositeMessageSource implements
        ReloadedResourceMessageSource, InitializingBean, DisposableBean, EnvironmentAware, BeanClassLoaderAware,
        ApplicationContextAware, FactoryBean<ReloadedResourceMessageSource>,
        ApplicationListener<ResourceMessageSourceChangedEvent>, Ordered {
    String PROPERTY_NAME_PREFIX = "microsphere.i18n.";

    String SUPPORTED_LOCALES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "supported-locales";


    private static final Logger logger = LoggerFactory.getLogger(ServiceMessageSourceFactoryBean.class);

    private final String source;

    private ClassLoader classLoader;

    private ConfigurableEnvironment environment;

    private ApplicationContext context;

    private int order;

    public ServiceMessageSourceFactoryBean(String source) {
        this(source, Ordered.LOWEST_PRECEDENCE);
    }

    public ServiceMessageSourceFactoryBean(String source, int order) {
        this.source = source;
        this.order = order;
    }

    @Override
    public ReloadedResourceMessageSource getObject() throws Exception {
        return this;
    }

    @Override
    public Class<ReloadedResourceMessageSource> getObjectType() {
        return ReloadedResourceMessageSource.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void init() {
        this.setMessageSources(initMessageSources());
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "The 'environment' parameter must be of type ConfigurableEnvironment");
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    private List<AbstractMessageSource> initMessageSources() {
        List<String> factoryNames = loadFactoryNames(AbstractMessageSource.class, classLoader);

        Set<Locale> supportedLocales = resolveSupportedLocales(environment);

        List<AbstractMessageSource> serviceMessageSources = new ArrayList<>(factoryNames.size());

        for (String factoryName : factoryNames) {
            Class<?> factoryClass = resolveClassName(factoryName, classLoader);
            Constructor constructor = getConstructorIfAvailable(factoryClass, String.class);
            AbstractMessageSource serviceMessageSource = (AbstractMessageSource) instantiateClass(constructor, source);
            serviceMessageSources.add(serviceMessageSource);

            invokeAwareInterfaces(serviceMessageSource, context);

            serviceMessageSource.setSupportedLocales(supportedLocales);
            serviceMessageSource.init();
        }

        OrderComparator.sort(serviceMessageSources);

        return serviceMessageSources;
    }

    @Override
    public String toString() {
        return "ServiceMessageSourceFactoryBean{" +
                "serviceMessageSources = " + getMessageSources() +
                ", order=" + order +
                '}';
    }

    private Set<Locale> resolveSupportedLocales(ConfigurableEnvironment environment) {
        final Set<Locale> supportedLocales;
        String propertyName = SUPPORTED_LOCALES_PROPERTY_NAME;
        List<String> locales = environment.getProperty(propertyName, List.class, Collections.emptyList());
        if (locales.isEmpty()) {
            supportedLocales = getSupportedLocales();
            logger.debug("Support Locale list configuration property [name : '{}'] not found, use default value: {}", propertyName, supportedLocales);
        } else {
            supportedLocales = locales.stream().map(StringUtils::parseLocale).collect(Collectors.toSet());
            logger.debug("List of supported Locales parsed by configuration property [name : '{}']: {}", propertyName, supportedLocales);
        }
        return Collections.unmodifiableSet(supportedLocales);
    }

    @Override
    public void onApplicationEvent(ResourceMessageSourceChangedEvent event) {
        Iterable<String> changedResources = event.getChangedResources();
        logger.debug("Receive event change resource: {}", changedResources);
        for (I18nMessageSource serviceMessageSource : getAllServiceMessageSources()) {
            if (serviceMessageSource instanceof ReloadedResourceMessageSource) {
                ReloadedResourceMessageSource reloadableResourceServiceMessageSource = (ReloadedResourceMessageSource) serviceMessageSource;
                if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                    reloadableResourceServiceMessageSource.reload(changedResources);
                    logger.debug("change resource [{}] activate {} reloaded", changedResources, reloadableResourceServiceMessageSource);
                }
            }
        }
    }

    public List<I18nMessageSource> getAllServiceMessageSources() {
        return findAllMessageSources(this);
    }
}