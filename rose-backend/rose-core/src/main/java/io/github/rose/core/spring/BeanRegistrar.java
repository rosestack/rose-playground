package io.github.rose.core.spring;

import java.beans.Introspector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.AliasRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public abstract class BeanRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(BeanRegistrar.class);

    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, Class<?> beanType) {
        BeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanType, 2);
        String beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanType, 2);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType) {
        BeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanType);
        String beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanType);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, Object... constructorArguments) {
        BeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanType, constructorArguments);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, int role) {
        BeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanType, role);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static final boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, BeanDefinition beanDefinition) {
        return registerBeanDefinition(registry, beanName, beanDefinition, false);
    }

    public static final boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, BeanDefinition beanDefinition, boolean allowBeanDefinitionOverriding) {
        boolean registered = false;
        if (!allowBeanDefinitionOverriding && registry.containsBeanDefinition(beanName)) {
            BeanDefinition oldBeanDefinition = registry.getBeanDefinition(beanName);
            if (logger.isWarnEnabled()) {
                logger.warn("The bean[name : '{}'] definition [{}] was registered!", beanName, oldBeanDefinition);
            }
        } else {
            try {
                registry.registerBeanDefinition(beanName, beanDefinition);
                if (logger.isDebugEnabled()) {
                    logger.debug("The bean[name : '{}' , role : {}] definition [{}] has been registered.", new Object[]{beanName, beanDefinition.getRole(), beanDefinition});
                }

                registered = true;
            } catch (BeanDefinitionStoreException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("The bean[name : '{}' , role : {}] definition [{}] can't be registered ", new Object[]{beanName, beanDefinition.getRole(), e});
                }

                registered = false;
            }
        }

        return registered;
    }

    public static void registerSingleton(SingletonBeanRegistry registry, String beanName, Object bean) {
        registry.registerSingleton(beanName, bean);
        if (logger.isInfoEnabled()) {
            logger.info("The singleton bean [name : '{}' , instance : {}] has been registered into the BeanFactory.", beanName, bean);
        }

    }

    public static boolean hasAlias(AliasRegistry registry, String beanName, String alias) {
        return StringUtils.hasText(beanName) && StringUtils.hasText(alias) && ObjectUtils.containsElement(registry.getAliases(beanName), alias);
    }

    public static int registerSpringFactoriesBeans(BeanDefinitionRegistry registry, Class<?>... factoryClasses) {
        int count = 0;
        ClassLoader classLoader = registry.getClass().getClassLoader();

        for (int i = 0; i < factoryClasses.length; ++i) {
            Class<?> factoryClass = factoryClasses[i];

            for (String factoryImplClassName : SpringFactoriesLoader.loadFactoryNames(factoryClass, classLoader)) {
                Class<?> factoryImplClass = ClassUtils.resolveClassName(factoryImplClassName, classLoader);
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(factoryImplClassName));
                if (registerInfrastructureBean(registry, beanName, factoryImplClass)) {
                    ++count;
                } else if (logger.isWarnEnabled()) {
                    logger.warn(String.format("The Factory Class bean[%s] has been registered with bean name[%s]", factoryImplClassName, beanName));
                }
            }
        }

        return count;
    }

    public static final void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        AbstractBeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(DelegatingFactoryBean.class, new Object[]{bean});
        beanDefinition.setSource(bean);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        registerBean(registry, beanName, bean, false);
    }

    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean, boolean primary) {
        Class beanClass = AopUtils.getTargetClass(bean);
        AbstractBeanDefinition beanDefinition = BeanDefinitionUtils.genericBeanDefinition(beanClass);
        beanDefinition.setInstanceSupplier(() -> bean);
        beanDefinition.setPrimary(primary);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }
}