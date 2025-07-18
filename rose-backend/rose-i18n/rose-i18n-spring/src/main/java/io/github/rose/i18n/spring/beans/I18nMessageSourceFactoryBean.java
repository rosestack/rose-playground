package io.github.rose.i18n.spring.beans;

import io.github.rose.core.spring.BeanUtils;
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

import static io.github.rose.i18n.spring.I18nConstants.DEFAULT_LOCALE_PROPERTY_NAME;
import static io.github.rose.i18n.spring.I18nConstants.SUPPORTED_LOCALES_PROPERTY_NAME;
import static io.github.rose.i18n.util.I18nUtils.findAllMessageSources;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.parseLocale;

/**
 * {@link I18nMessageSource} {@link FactoryBean} Implementation
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
 * @since 1.0.0
 */
public final class I18nMessageSourceFactoryBean extends CompositeMessageSource implements
        ReloadedResourceMessageSource, InitializingBean, DisposableBean, EnvironmentAware, BeanClassLoaderAware,
        ApplicationContextAware, FactoryBean<ReloadedResourceMessageSource>,
        ApplicationListener<ResourceMessageSourceChangedEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(I18nMessageSourceFactoryBean.class);

    private final String source;
    private ClassLoader classLoader;
    private ConfigurableEnvironment environment;
    private ApplicationContext context;
    private int order;
    private Locale defaultLocale;
    private Set<Locale> supportedLocales;

    public I18nMessageSourceFactoryBean(String source) {
        this(source, Ordered.LOWEST_PRECEDENCE);
    }

    public I18nMessageSourceFactoryBean(String source, int order) {
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

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    private List<AbstractMessageSource> initMessageSources() {
        List<String> factoryNames = loadFactoryNames(AbstractMessageSource.class, classLoader);

        // 优先使用注解配置的值，如果没有配置则使用环境变量
        Locale resolvedDefaultLocale = this.defaultLocale != null ? this.defaultLocale : resolveDefaultLocale(environment);
        Set<Locale> resolvedSupportedLocales = this.supportedLocales != null ? this.supportedLocales : resolveSupportedLocales(environment);

        List<AbstractMessageSource> messageSources = new ArrayList<>(factoryNames.size());

        for (String factoryName : factoryNames) {
            Class<?> factoryClass = resolveClassName(factoryName, classLoader);
            Constructor constructor = getConstructorIfAvailable(factoryClass, String.class);
            AbstractMessageSource serviceMessageSource = (AbstractMessageSource) instantiateClass(constructor, source);
            messageSources.add(serviceMessageSource);

            BeanUtils.invokeAwareInterfaces(serviceMessageSource, context);

            serviceMessageSource.setDefaultLocale(resolvedDefaultLocale);
            serviceMessageSource.setSupportedLocales(resolvedSupportedLocales);
            serviceMessageSource.init();
        }

        OrderComparator.sort(messageSources);

        return messageSources;
    }

    @Override
    public String toString() {
        return "I18nMessageSourceFactoryBean{" +
                "i18nMessageSources = " + getMessageSources() +
                ", order=" + order +
                '}';
    }

    private Locale resolveDefaultLocale(ConfigurableEnvironment environment) {
        String propertyName = DEFAULT_LOCALE_PROPERTY_NAME;
        String localeValue = environment.getProperty(propertyName);
        final Locale locale;
        if (!hasText(localeValue)) {
            locale = getDefaultLocale();
            logger.debug("Default Locale configuration property [name : '{}'] not found, use default value: '{}'", propertyName, locale);
        } else {
            locale = parseLocale(localeValue);
            logger.debug("Default Locale : '{}' parsed by configuration properties [name : '{}']", propertyName, locale);
        }
        return locale;
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
        for (I18nMessageSource i18nMessageSource : getAllI18nMessageSources()) {
            if (i18nMessageSource instanceof ReloadedResourceMessageSource) {
                ReloadedResourceMessageSource reloadableResourceServiceMessageSource = (ReloadedResourceMessageSource) i18nMessageSource;
                if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                    reloadableResourceServiceMessageSource.reload(changedResources);
                    logger.debug("change resource [{}] activate {} reloaded", changedResources, reloadableResourceServiceMessageSource);
                }
            }
        }
    }

    public List<I18nMessageSource> getAllI18nMessageSources() {
        return findAllMessageSources(this);
    }
}