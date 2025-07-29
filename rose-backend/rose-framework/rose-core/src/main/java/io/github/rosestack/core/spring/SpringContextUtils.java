package io.github.rosestack.core.spring;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 简化的 Spring 上下文工具类
 * <p>
 * 提供常用的 Spring Bean 获取和操作功能，替代复杂的 SpringBeanUtils
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Lazy
public class SpringContextUtils implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
        log.debug("SpringContextUtils 初始化完成");
    }

    /**
     * 获取 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取应用名称
     */
    public static String getApplicationName() {
        return applicationContext != null ? applicationContext.getApplicationName() : "unknown";
    }

    /**
     * 获取激活的配置文件
     */
    public static String[] getActiveProfiles() {
        return applicationContext != null ?
                applicationContext.getEnvironment().getActiveProfiles() :
                new String[0];
    }

    /**
     * 获取第一个激活的配置文件
     */
    public static String getActiveProfile() {
        String[] activeProfiles = getActiveProfiles();
        return ObjectUtils.isEmpty(activeProfiles) ? null : activeProfiles[0];
    }

    /**
     * 根据类型获取 Bean
     */
    public static <T> T getBean(Class<T> beanClass) throws BeansException {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 根据名称和类型获取 Bean
     */
    public static <T> T getBean(String name, Class<T> beanClass) throws BeansException {
        return applicationContext.getBean(name, beanClass);
    }

    /**
     * 根据名称获取 Bean
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    /**
     * 获取指定类型的所有 Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return applicationContext.getBeansOfType(type);
    }

    /**
     * 获取指定类型的所有 Bean，并按 @Order 注解排序
     */
    public static <T> List<T> getSortedBeans(Class<T> type) {
        Map<String, T> beansOfType = getBeansOfType(type);
        List<T> beansList = new ArrayList<>(beansOfType.values());
        AnnotationAwareOrderComparator.sort(beansList);
        return Collections.unmodifiableList(beansList);
    }

    /**
     * 检查是否包含指定名称的 Bean
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 检查指定名称的 Bean 是否为单例
     */
    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    /**
     * 获取指定名称 Bean 的类型
     */
    public static Class<?> getType(String name) {
        return applicationContext.getType(name);
    }

    /**
     * 获取指定名称 Bean 的别名
     */
    public static String[] getAliases(String name) {
        return applicationContext.getAliases(name);
    }

    /**
     * 发布事件
     */
    public static void publishEvent(Object event) {
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        }
    }

    /**
     * 获取环境变量
     */
    public static String getProperty(String key) {
        return applicationContext != null ?
                applicationContext.getEnvironment().getProperty(key) : null;
    }

    /**
     * 获取环境变量，如果不存在则返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return applicationContext != null ?
                applicationContext.getEnvironment().getProperty(key, defaultValue) :
                defaultValue;
    }

    /**
     * 获取指定类型的环境变量
     */
    public static <T> T getProperty(String key, Class<T> targetType) {
        return applicationContext != null ?
                applicationContext.getEnvironment().getProperty(key, targetType) : null;
    }

    /**
     * 获取指定类型的环境变量，如果不存在则返回默认值
     */
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return applicationContext != null ?
                applicationContext.getEnvironment().getProperty(key, targetType, defaultValue) :
                defaultValue;
    }

    /**
     * 检查是否存在指定的环境变量
     */
    public static boolean containsProperty(String key) {
        return applicationContext != null &&
                applicationContext.getEnvironment().containsProperty(key);
    }

    /**
     * 检查应用上下文是否已初始化
     */
    public static boolean isInitialized() {
        return applicationContext != null;
    }

    /**
     * 获取 Bean 的数量
     */
    public static int getBeanDefinitionCount() {
        return applicationContext != null ? applicationContext.getBeanDefinitionCount() : 0;
    }

    /**
     * 获取所有 Bean 的名称
     */
    public static String[] getBeanDefinitionNames() {
        return applicationContext != null ?
                applicationContext.getBeanDefinitionNames() :
                new String[0];
    }

    /**
     * 调用 Aware 接口方法
     *
     * @param bean        Bean 实例
     * @param beanFactory Bean 工厂
     */
    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory) {
        if (bean instanceof ApplicationContextAware && beanFactory instanceof ApplicationContext) {
            ((ApplicationContextAware) bean).setApplicationContext((ApplicationContext) beanFactory);
        }
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(beanFactory);
        }
        if (bean instanceof EnvironmentAware && beanFactory instanceof ApplicationContext) {
            ((EnvironmentAware) bean).setEnvironment(((ApplicationContext) beanFactory).getEnvironment());
        }
    }

    @Override
    public void destroy() throws Exception {
        log.debug("SpringContextUtils 正在销毁");
        applicationContext = null;
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
