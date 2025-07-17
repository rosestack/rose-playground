package io.github.rose.core.spring;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public abstract class BeanFactoryUtils {

    /**
     * Get optional Bean
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanName    the name of Bean
     * @param beanType    the {@link Class type} of Bean
     * @param <T>         the {@link Class type} of Bean
     * @return A bean if present , or <code>null</code>
     */
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) {
        if (!StringUtils.hasText(beanName)) {
            return null;
        }

        String[] beanNames = new String[]{beanName};

        List<T> beans = getBeans(beanFactory, beanNames, beanType);

        return CollectionUtils.isEmpty(beans) ? null : beans.get(0);
    }


    /**
     * Gets name-matched Beans from {@link ListableBeanFactory BeanFactory}
     *
     * @param beanFactory {@link ListableBeanFactory BeanFactory}
     * @param beanNames   the names of Bean
     * @param beanType    the {@link Class type} of Bean
     * @param <T>         the {@link Class type} of Bean
     * @return the read-only and non-null {@link List} of Bean names
     */
    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) {
        int size = ArrayUtils.getLength(beanNames);
        if (size < 1) {
            return Collections.emptyList();
        }

        String[] allBeanNames = org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanType, true, false);
        List<T> beans = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            String beanName = beanNames[i];
            if (ObjectUtils.containsElement(allBeanNames, beanName)) {
                beans.add(beanFactory.getBean(beanName, beanType));
            }
        }
        return Collections.unmodifiableList(beans);
    }

    /**
     * Is the given BeanFactory {@link DefaultListableBeanFactory}
     *
     * @param beanFactory {@link BeanFactory}
     * @return <code>true</code> if it's {@link DefaultListableBeanFactory}, <code>false</code> otherwise
     */
    public static boolean isDefaultListableBeanFactory(Object beanFactory) {
        return beanFactory instanceof DefaultListableBeanFactory;
    }

    public static boolean isBeanDefinitionRegistry(Object beanFactory) {
        return beanFactory instanceof BeanDefinitionRegistry;
    }

    public static BeanDefinitionRegistry asBeanDefinitionRegistry(Object beanFactory) {
        return cast(beanFactory, BeanDefinitionRegistry.class);
    }

    public static ListableBeanFactory asListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ListableBeanFactory.class);
    }

    public static HierarchicalBeanFactory asHierarchicalBeanFactory(Object beanFactory) {
        return cast(beanFactory, HierarchicalBeanFactory.class);
    }

    public static ConfigurableBeanFactory asConfigurableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ConfigurableBeanFactory.class);
    }

    public static AutowireCapableBeanFactory asAutowireCapableBeanFactory(Object beanFactory) {
        return cast(beanFactory, AutowireCapableBeanFactory.class);
    }

    public static ConfigurableListableBeanFactory asConfigurableListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ConfigurableListableBeanFactory.class);
    }

    public static DefaultListableBeanFactory asDefaultListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, DefaultListableBeanFactory.class);
    }

    /**
     * Get all instances of {@link BeanPostProcessor} in the specified {@link BeanFactory}
     *
     * @param beanFactory {@link BeanFactory}
     * @return non-null {@link List}
     */
    public static List<BeanPostProcessor> getBeanPostProcessors(@Nullable BeanFactory beanFactory) {
        final List<BeanPostProcessor> beanPostProcessors;
        if (beanFactory instanceof AbstractBeanFactory abf) {
            beanPostProcessors = Collections.unmodifiableList(abf.getBeanPostProcessors());
        } else {
            beanPostProcessors = Collections.emptyList();
        }
        return beanPostProcessors;
    }

    private static <T> T cast(@Nullable Object beanFactory, Class<T> extendedBeanFactoryType) {
        if (beanFactory == null) {
            return null;
        }
        if (beanFactory instanceof ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }
        Assert.isInstanceOf(extendedBeanFactoryType, beanFactory,
                "The 'beanFactory' argument is not a instance of " + extendedBeanFactoryType +
                        ", is it running in Spring container?");
        return extendedBeanFactoryType.cast(beanFactory);
    }

}