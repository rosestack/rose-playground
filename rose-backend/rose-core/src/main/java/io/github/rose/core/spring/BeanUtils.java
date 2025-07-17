package io.github.rose.core.util;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.rose.core.util.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.github.rose.core.util.BeanFactoryUtils.asConfigurableBeanFactory;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class BeanUtils {
    private static final Logger log = LoggerFactory.getLogger(BeanUtils.class);

    public static void invokeBeanInterfaces(Object bean, ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context);
        try {
            invokeInitializingBean(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeInitializingBean(Object bean) throws Exception {
        if (bean instanceof InitializingBean initializingBean) {
            initializingBean.afterPropertiesSet();
        }
    }

    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, asConfigurableBeanFactory(beanFactory));
    }

    public static void invokeAwareInterfaces(Object bean, ConfigurableBeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, beanFactory);
    }

    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (beanFactory instanceof ApplicationContext context) {
            invokeAwareInterfaces(bean, context);
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
        if (bean instanceof BeanNameAware beanNameAware) {
            BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
            BeanDefinition beanDefinition = rootBeanDefinition(bean.getClass()).getBeanDefinition();
            String beanName = generateBeanName(beanDefinition, registry);
            beanNameAware.setBeanName(beanName);
        }
    }

    static void invokeAwareInterfaces(Object bean, ApplicationContext context) {
        invokeAwareInterfaces(bean, context, asConfigurableApplicationContext(context));
    }

    public static ConfigurableApplicationContext asConfigurableApplicationContext(ApplicationContext context) {
        return ClassUtils.cast(context, ConfigurableApplicationContext.class);
    }

    public static void invokeAwareInterfaces(Object bean, ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context, context);
    }

    static void invokeAwareInterfaces(Object bean, ApplicationContext context, @Nullable ConfigurableApplicationContext applicationContext) {
        if (bean == null || context == null) {
            return;
        }

        ConfigurableListableBeanFactory beanFactory = applicationContext != null ? applicationContext.getBeanFactory() : null;

        invokeBeanFactoryAwareInterfaces(bean, beanFactory, beanFactory);

        BeanPostProcessor beanPostProcessor = io.github.rose.core.util.ApplicationContextUtils.getApplicationContextAwareProcessor(beanFactory);

        if (beanPostProcessor != null) {
            beanPostProcessor.postProcessBeforeInitialization(bean, "");
        }
    }

    public static void invokeBeanNameAware(Object bean, String beanName) {
        if (bean instanceof BeanNameAware beanNameAware) {
            beanNameAware.setBeanName(beanName);
        }
    }

    static void invokeBeanFactoryAware(Object bean, BeanFactory beanFactory) {
        if (bean instanceof BeanFactoryAware beanFactoryAware) {
            beanFactoryAware.setBeanFactory(beanFactory);
        }
    }

    static void invokeBeanClassLoaderAware(Object bean, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (bean instanceof BeanClassLoaderAware beanClassLoaderAware && configurableBeanFactory != null) {
            ClassLoader classLoader = configurableBeanFactory.getBeanClassLoader();
            beanClassLoaderAware.setBeanClassLoader(classLoader);
        }
    }

    public static <T> List<T> getSortedBeans(ListableBeanFactory beanFactory, Class<T> type) {
        Map<String, T> beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type);
        List<T> beansList = new ArrayList(beansOfType.values());
        AnnotationAwareOrderComparator.sort(beansList);
        return Collections.unmodifiableList(beansList);
    }

    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, Class<T> beanClass) throws BeansException {
        return getOptionalBean(beanFactory, beanClass, false);
    }

    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, Class<T> beanClass, boolean includingAncestors) throws BeansException {
        String[] beanNames = getBeanNames(beanFactory, beanClass, includingAncestors);
        if (ObjectUtils.isEmpty(beanNames)) {
            if (log.isTraceEnabled()) {
                log.trace("The bean [ class : " + beanClass.getName() + " ] can't be found ");
            }
            return null;
        }

        T bean = null;

        try {
            bean = includingAncestors ? BeanFactoryUtils.beanOfTypeIncludingAncestors(beanFactory, beanClass) : beanFactory.getBean(beanClass);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        return bean;
    }

    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> beanClass) {
        return getBeanNames(beanFactory, beanClass, false);
    }

    /**
     * Get Bean Names from {@link ListableBeanFactory} by type.
     *
     * @param beanFactory        {@link ListableBeanFactory}
     * @param beanClass          The  {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @return If found , return the array of Bean Names , or empty array.
     */
    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> beanClass, boolean includingAncestors) {
        if (includingAncestors) {
            return beanNamesForTypeIncludingAncestors(beanFactory, beanClass, true, false);
        } else {
            return beanFactory.getBeanNamesForType(beanClass, true, false);
        }
    }
}
