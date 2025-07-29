package io.github.rosestack.core.spring;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.*;

@Lazy(value = false)
public class SpringBeanUtils implements ApplicationContextAware, DisposableBean {
    private static final Log logger = LogFactory.getLog(SpringBeanUtils.class);
    private static final boolean APPLICATION_STARTUP_CLASS_PRESENT = ClassUtils.isPresent("org.springframework.core.metrics.ApplicationStartup", (ClassLoader) null);
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeanUtils.applicationContext = applicationContext;
    }

    public static String getApplicationName() {
        return applicationContext.getApplicationName();
    }

    public static String[] getActiveProfiles() {
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    public static String getActiveProfile() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        return ObjectUtils.isEmpty(activeProfiles) ? null : activeProfiles[0];
    }

    public static boolean isBeanPresent(Class<?> beanClass) {
        return isBeanPresent(beanClass, false);
    }

    public static boolean isBeanPresent(Class<?> beanClass, boolean includingAncestors) {
        String[] beanNames = getBeanNames(beanClass, includingAncestors);
        return !ObjectUtils.isEmpty(beanNames);
    }

    public static boolean isBeanPresent(String beanClassName, boolean includingAncestors) {
        boolean present = false;
        ClassLoader classLoader = applicationContext.getClass().getClassLoader();
        if (ClassUtils.isPresent(beanClassName, classLoader)) {
            Class beanClass = ClassUtils.resolveClassName(beanClassName, classLoader);
            present = isBeanPresent(beanClass, includingAncestors);
        }

        return present;
    }

    public static boolean isBeanPresent(String beanClassName) {
        return isBeanPresent(beanClassName, false);
    }

    public static String[] getBeanNames(Class<?> beanClass) {
        return getBeanNames(beanClass, false);
    }

    public static String[] getBeanNames(Class<?> beanClass, boolean includingAncestors) {
        return includingAncestors ? BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, beanClass, true, false) :
                applicationContext.getBeanNamesForType(beanClass, true, false);
    }

    public static Class<?> resolveBeanType(String beanClassName, ClassLoader classLoader) {
        if (!StringUtils.hasText(beanClassName)) {
            return null;
        } else {
            Class<?> beanType = null;

            try {
                beanType = ClassUtils.resolveClassName(beanClassName, classLoader);
                beanType = ClassUtils.getUserClass(beanType);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
            }

            return beanType;
        }
    }

    public static <T> T getBean(Class<T> beanClass, boolean includingAncestors) throws BeansException {
        String[] beanNames = getBeanNames(beanClass, includingAncestors);
        if (ObjectUtils.isEmpty(beanNames)) {
            if (logger.isDebugEnabled()) {
                logger.debug("The bean [ class : " + beanClass.getName() + " ] can't be found ");
            }

            return null;
        } else {
            T bean = null;

            try {
                bean = (T) (includingAncestors ? BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, beanClass) : applicationContext.getBean(beanClass));
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
            }

            return bean;
        }
    }

    public static <T> T getBean(Class<T> beanClass) throws BeansException {
        return (T) getBean(beanClass, false);
    }

    public static <T> List<T> getSortedBeans(Class<T> type) {
        Map<String, T> beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, type);
        List<T> beansList = new ArrayList(beansOfType.values());
        AnnotationAwareOrderComparator.sort(beansList);
        return Collections.unmodifiableList(beansList);
    }

    public static void invokeBeanInterfaces(Object bean, ApplicationContext context) {
        ConfigurableApplicationContext configurableApplicationContext = asConfigurableApplicationContext(context);
        invokeBeanInterfaces(bean, configurableApplicationContext);
    }

    public static void invokeBeanInterfaces(Object bean, ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context);

        try {
            invokeInitializingBean(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeInitializingBean(Object bean) throws Exception {
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

    }

    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, asConfigurableBeanFactory(beanFactory));
    }

    public static void invokeAwareInterfaces(Object bean, ConfigurableBeanFactory beanFactory) {
        invokeAwareInterfaces(bean, (BeanFactory) beanFactory, (ConfigurableBeanFactory) beanFactory);
    }

    static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (beanFactory instanceof ApplicationContext) {
            invokeAwareInterfaces(bean, (ApplicationContext) beanFactory);
        } else {
            invokeBeanFactoryAwareInterfaces(bean, beanFactory, configurableBeanFactory);
        }

    }

    static void invokeBeanFactoryAwareInterfaces(Object bean, BeanFactory beanFactory, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        invokeBeanNameAware(bean, beanFactory);
        invokeBeanClassLoaderAware(bean, configurableBeanFactory);
        invokeBeanFactoryAware(bean, beanFactory);
    }

    static void invokeBeanNameAware(Object bean, BeanFactory beanFactory) {
        if (bean instanceof BeanNameAware) {
            BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
            BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(bean.getClass()).getBeanDefinition();
            String beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
            ((BeanNameAware) bean).setBeanName(beanName);
        }

    }

    public static void invokeBeanNameAware(Object bean, String beanName) {
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }

    }

    static void invokeBeanFactoryAware(Object bean, BeanFactory beanFactory) {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(beanFactory);
        }

    }

    static void invokeBeanClassLoaderAware(Object bean, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (bean instanceof BeanClassLoaderAware && configurableBeanFactory != null) {
            ClassLoader classLoader = configurableBeanFactory.getBeanClassLoader();
            ((BeanClassLoaderAware) bean).setBeanClassLoader(classLoader);
        }

    }

    static void invokeAwareInterfaces(Object bean, ApplicationContext context) {
        invokeAwareInterfaces(bean, context, asConfigurableApplicationContext(context));
    }

    public static void invokeAwareInterfaces(Object bean, ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, (ApplicationContext) context, (ConfigurableApplicationContext) context);
    }

    static void invokeAwareInterfaces(Object bean, ApplicationContext context, @Nullable ConfigurableApplicationContext applicationContext) {
        if (bean != null && context != null) {
            ConfigurableListableBeanFactory beanFactory = applicationContext != null ? applicationContext.getBeanFactory() : null;
            invokeBeanFactoryAwareInterfaces(bean, beanFactory, beanFactory);
            if (bean instanceof EnvironmentAware) {
                ((EnvironmentAware) bean).setEnvironment(context.getEnvironment());
            }

            if (bean instanceof EmbeddedValueResolverAware && beanFactory != null) {
                StringValueResolver embeddedValueResolver = new EmbeddedValueResolver(beanFactory);
                ((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(embeddedValueResolver);
            }

            if (bean instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware) bean).setResourceLoader(context);
            }

            if (bean instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(context);
            }

            if (bean instanceof MessageSourceAware) {
                ((MessageSourceAware) bean).setMessageSource(context);
            }

            if (APPLICATION_STARTUP_CLASS_PRESENT && bean instanceof ApplicationStartupAware && applicationContext != null) {
                ((ApplicationStartupAware) bean).setApplicationStartup(applicationContext.getApplicationStartup());
            }

            if (bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(context);
            }

        }
    }

    static <T> Map<String, T> sort(final Map<String, T> beansMap) {
        Map<String, T> unmodifiableBeansMap = Collections.unmodifiableMap(beansMap);
        List<NamingBean<T>> namingBeans = new ArrayList(unmodifiableBeansMap.size());

        for (Map.Entry<String, T> entry : unmodifiableBeansMap.entrySet()) {
            String beanName = (String) entry.getKey();
            T bean = (T) entry.getValue();
            NamingBean<T> namingBean = new NamingBean<T>(beanName, bean);
            namingBeans.add(namingBean);
        }

        AnnotationAwareOrderComparator.sort(namingBeans);
        Map<String, T> sortedBeansMap = new LinkedHashMap(beansMap.size());

        for (NamingBean<T> namingBean : namingBeans) {
            sortedBeansMap.put(namingBean.name, namingBean.bean);
        }

        return sortedBeansMap;
    }

    @Override
    public void destroy() throws Exception {
        if (applicationContext != null) {
            applicationContext = null;
        }
    }

    static class NamingBean<T> extends AnnotationAwareOrderComparator implements Comparable<NamingBean>, Ordered {
        private final String name;
        private final T bean;

        NamingBean(String name, T bean) {
            this.name = name;
            this.bean = bean;
        }

        public int compareTo(NamingBean o) {
            return this.compare(this, o);
        }

        public int getOrder() {
            return this.getOrder(this.bean);
        }
    }

    public static ConfigurableApplicationContext asConfigurableApplicationContext(ApplicationContext context) {
        return (ConfigurableApplicationContext) cast(context, ConfigurableApplicationContext.class);
    }

    public static ApplicationContext asApplicationContext(BeanFactory beanFactory) {
        return (ApplicationContext) cast(beanFactory, ApplicationContext.class);
    }

    public static BeanDefinitionRegistry asBeanDefinitionRegistry(Object beanFactory) {
        return (BeanDefinitionRegistry) cast(beanFactory, BeanDefinitionRegistry.class);
    }

    public static ListableBeanFactory asListableBeanFactory(Object beanFactory) {
        return (ListableBeanFactory) cast(beanFactory, ListableBeanFactory.class);
    }

    public static HierarchicalBeanFactory asHierarchicalBeanFactory(Object beanFactory) {
        return (HierarchicalBeanFactory) cast(beanFactory, HierarchicalBeanFactory.class);
    }

    public static ConfigurableBeanFactory asConfigurableBeanFactory(Object beanFactory) {
        return (ConfigurableBeanFactory) cast(beanFactory, ConfigurableBeanFactory.class);
    }

    public static AutowireCapableBeanFactory asAutowireCapableBeanFactory(Object beanFactory) {
        return (AutowireCapableBeanFactory) cast(beanFactory, AutowireCapableBeanFactory.class);
    }

    public static ConfigurableListableBeanFactory asConfigurableListableBeanFactory(Object beanFactory) {
        return (ConfigurableListableBeanFactory) cast(beanFactory, ConfigurableListableBeanFactory.class);
    }

    public static DefaultListableBeanFactory asDefaultListableBeanFactory(Object beanFactory) {
        return (DefaultListableBeanFactory) cast(beanFactory, DefaultListableBeanFactory.class);
    }

    public static Set<Class<?>> getResolvableDependencyTypes(ConfigurableListableBeanFactory beanFactory) {
        DefaultListableBeanFactory defaultListableBeanFactory = asDefaultListableBeanFactory(beanFactory);
        return defaultListableBeanFactory == null ? Collections.emptySet() : getResolvableDependencyTypes((DefaultListableBeanFactory) beanFactory);
    }

    public static Set<Class<?>> getResolvableDependencyTypes(DefaultListableBeanFactory beanFactory) {
        Map resolvableDependencies = (Map) FieldUtils.getFieldValue(beanFactory, "resolvableDependencies", Map.class);
        return resolvableDependencies == null ? Collections.emptySet() : resolvableDependencies.keySet();
    }

    public static <T> T cast(Object object, Class<T> castType) {
        if (object != null && castType != null) {
            return (T) (castType.isInstance(object) ? castType.cast(object) : null);
        } else {
            return null;
        }
    }

    public static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName(filter.getClass().getSimpleName());
        registrationBean.setOrder(order);
        return registrationBean;
    }
}