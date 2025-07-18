package io.github.rose.core.spring;

import io.github.rose.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spring Bean工具类
 * 提供Bean获取、配置访问、Bean查询等核心功能
 * 整合了原来的SpringContextUtils、BeanFactoryUtils、ApplicationContextUtils功能
 *
 * 注意：避免与Spring框架的org.springframework.beans.BeanUtils冲突
 *
 * @author zhijun.chen
 * @since 0.0.1
 */
@Slf4j
@Component
@Lazy
public class SpringBeans implements BeanFactoryPostProcessor, ApplicationContextAware {
    private static ConfigurableListableBeanFactory beanFactory;
    private static ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringBeans.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeans.applicationContext = applicationContext;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取ListableBeanFactory
     *
     * @return ListableBeanFactory
     */
    public static ListableBeanFactory getBeanFactory() {
        final ListableBeanFactory factory = null == beanFactory ? applicationContext : beanFactory;
        if (null == factory) {
            throw new BusinessException("No ConfigurableListableBeanFactory or ApplicationContext injected, maybe not in the Spring environment?");
        }
        return factory;
    }

    /**
     * 通过name获取Bean
     *
     * @param <T>  Bean类型
     * @param name Bean名称
     * @return Bean
     */
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param <T>   Bean类型
     * @param clazz Bean类
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return getBeanFactory().getBean(clazz);
    }

    /**
     * 通过name和class获取Bean
     *
     * @param <T>   Bean类型
     * @param name  Bean名称
     * @param clazz Bean类型
     * @return Bean对象
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    /**
     * 获取指定类型对应的所有Bean，包括子类
     *
     * @param <T>  Bean类型
     * @param type 类、接口
     * @return 类型对应的bean，key是bean注册的name，value是Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 获取指定类型对应的Bean名称，包括子类
     *
     * @param type 类、接口
     * @return bean名称数组
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * 获取指定类型的所有Bean并排序
     *
     * @param type Bean类型
     * @param <T>  Bean类型
     * @return 排序后的Bean列表
     */
    public static <T> List<T> getSortedBeans(Class<T> type) {
        Map<String, T> beansOfType = getBeansOfType(type);
        if (CollectionUtils.isEmpty(beansOfType)) {
            return Collections.emptyList();
        }

        List<T> beansList = new ArrayList<>(beansOfType.values());
        // 这里可以添加排序逻辑，比如基于@Order注解
        return Collections.unmodifiableList(beansList);
    }

    // ==================== 基础Bean工厂方法 ====================

    /**
     * 检查是否包含指定名称的Bean
     *
     * @param name Bean名称
     * @return 是否包含
     */
    public static boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    /**
     * 判断指定名称的Bean是否为单例
     *
     * @param name Bean名称
     * @return 是否为单例
     * @throws NoSuchBeanDefinitionException 如果Bean不存在
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().isSingleton(name);
    }

    /**
     * 获取指定名称Bean的类型
     *
     * @param name Bean名称
     * @return Bean类型
     * @throws NoSuchBeanDefinitionException 如果Bean不存在
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getType(name);
    }

    /**
     * 获取指定名称Bean的别名
     *
     * @param name Bean名称
     * @return 别名数组
     * @throws NoSuchBeanDefinitionException 如果Bean不存在
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getAliases(name);
    }

    // ==================== 配置属性方法 ====================

    /**
     * 获取配置属性值
     *
     * @param key 配置键
     * @return 配置值
     */
    public static String getProperty(String key) {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 获取配置属性值，带默认值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getProperty(String key, String defaultValue) {
        if (applicationContext == null) {
            return defaultValue;
        }
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称
     */
    public static String getApplicationName() {
        return getProperty("spring.application.name");
    }

    /**
     * 获取当前激活的配置文件
     *
     * @return 激活的配置文件数组
     */
    public static String[] getActiveProfiles() {
        if (applicationContext == null) {
            return new String[0];
        }
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取第一个激活的配置文件
     *
     * @return 第一个激活的配置文件
     */
    public static String getActiveProfile() {
        String[] activeProfiles = getActiveProfiles();
        return ObjectUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
    }

    // ==================== Bean查询和过滤方法 ====================

    /**
     * 查找基础设施Bean名称
     *
     * @return Bean名称集合
     */
    public static Set<String> findInfrastructureBeanNames() {
        return findBeanNames(beanDefinition ->
            beanDefinition != null && beanDefinition.getRole() == org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE);
    }

    /**
     * 根据条件查找Bean名称
     *
     * @param predicate 过滤条件
     * @return Bean名称集合
     */
    public static Set<String> findBeanNames(java.util.function.Predicate<org.springframework.beans.factory.config.BeanDefinition> predicate) {
        if (predicate == null) {
            return Collections.emptySet();
        }

        org.springframework.beans.factory.config.ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        Set<String> matchedBeanNames = new LinkedHashSet<>();
        String[] beanDefinitionNames = factory.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            org.springframework.beans.factory.config.BeanDefinition beanDefinition = factory.getBeanDefinition(beanDefinitionName);
            if (predicate.test(beanDefinition)) {
                matchedBeanNames.add(beanDefinitionName);
            }
        }

        return Collections.unmodifiableSet(matchedBeanNames);
    }

    /**
     * 获取ConfigurableListableBeanFactory
     *
     * @return ConfigurableListableBeanFactory
     */
    public static org.springframework.beans.factory.config.ConfigurableListableBeanFactory getConfigurableBeanFactory() {
        final org.springframework.beans.factory.config.ConfigurableListableBeanFactory factory;
        if (null != beanFactory) {
            factory = beanFactory;
        } else if (applicationContext instanceof ConfigurableApplicationContext) {
            factory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        } else {
            throw new BusinessException("No ConfigurableListableBeanFactory from context!");
        }
        return factory;
    }
}
