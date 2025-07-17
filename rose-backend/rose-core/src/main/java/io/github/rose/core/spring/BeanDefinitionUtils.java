package io.github.rose.core.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public abstract class BeanDefinitionUtils {
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType) {
        return genericBeanDefinition(beanType, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, Object... constructorArguments) {
        return genericBeanDefinition(beanType, 0, constructorArguments);
    }

    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role) {
        return genericBeanDefinition(beanType, role, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role, Object[] constructorArguments) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanType).setRole(role);
        int length = ArrayUtils.getLength(constructorArguments);

        for(int i = 0; i < length; ++i) {
            Object constructorArgument = constructorArguments[i];
            beanDefinitionBuilder.addConstructorArgValue(constructorArgument);
        }

        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        return beanDefinition;
    }

    public static Class<?> resolveBeanType(RootBeanDefinition beanDefinition) {
        return resolveBeanType(beanDefinition, ClassLoaderUtils.getDefaultClassLoader());
    }

    public static Class<?> resolveBeanType(RootBeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        Class<?> beanClass = null;
        Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
        if (factoryMethod == null) {
            if (beanDefinition.hasBeanClass()) {
                beanClass = beanDefinition.getBeanClass();
            } else {
                String beanClassName = beanDefinition.getBeanClassName();
                if (StringUtils.hasText(beanClassName)) {
                    ClassLoader targetClassLoader = classLoader == null ? ClassLoaderUtils.getDefaultClassLoader() : classLoader;
                    beanClass = ClassLoaderUtils.resolveClass(beanClassName, targetClassLoader, true);
                }
            }
        } else {
            beanClass = factoryMethod.getReturnType();
        }

        return beanClass;
    }

    public static Set<String> findInfrastructureBeanNames(ConfigurableListableBeanFactory beanFactory) {
        return findBeanNames(beanFactory, BeanDefinitionUtils::isInfrastructureBean);
    }

    public static Set<String> findBeanNames(ConfigurableListableBeanFactory beanFactory, Predicate<BeanDefinition> predicate) {
        if (predicate == null) {
            return Collections.emptySet();
        } else {
            Set<String> matchedBeanNames = new LinkedHashSet();
            String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();

            for(String beanDefinitionName : beanDefinitionNames) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
                if (predicate.test(beanDefinition)) {
                    matchedBeanNames.add(beanDefinitionName);
                }
            }

            return Collections.unmodifiableSet(matchedBeanNames);
        }
    }

    public static boolean isInfrastructureBean(BeanDefinition beanDefinition) {
        return beanDefinition != null && 2 == beanDefinition.getRole();
    }
}
