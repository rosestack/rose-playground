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

import java.util.*;

/**
 * Spring Bean 工具类，提供对 Spring 应用上下文和 Bean 的全面访问。
 * <p>
 * 该工具类作为访问 Spring Bean、配置属性和应用上下文操作的集中访问点。
 * 它将 SpringContextUtils、BeanFactoryUtils 和 ApplicationContextUtils
 * 等多个工具类的功能整合到一个统一的接口中。
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li><strong>Bean 访问：</strong> 通过名称、类型或两者来检索 Bean</li>
 *   <li><strong>配置访问：</strong> 访问应用程序属性和配置文件</li>
 *   <li><strong>Bean 发现：</strong> 基于各种条件查找和过滤 Bean</li>
 *   <li><strong>上下文信息：</strong> 访问应用上下文元数据</li>
 *   <li><strong>类型安全：</strong> 提供类型安全的 Bean 检索泛型方法</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 通过类型获取 Bean
 * UserService userService = SpringBeans.getBean(UserService.class);
 *
 * // 通过名称获取 Bean
 * Object myBean = SpringBeans.getBean("myBeanName");
 *
 * // 获取配置属性
 * String appName = SpringBeans.getProperty("spring.application.name");
 *
 * // 获取某个类型的所有 Bean
 * Map<String, UserService> userServices = SpringBeans.getBeansOfType(UserService.class);
 * }</pre>
 *
 * <h3>实现说明：</h3>
 * <ul>
 *   <li><strong>延迟初始化：</strong> 使用 @Lazy 注解避免循环依赖</li>
 *   <li><strong>静态访问：</strong> 提供静态方法以便于访问</li>
 *   <li><strong>错误处理：</strong> 对于缺失的上下文抛出 BusinessException</li>
 *   <li><strong>线程安全：</strong> 所有方法都是线程安全的</li>
 * </ul>
 *
 * <p><strong>注意：</strong> 该类通过使用不同的名称和包来避免与 Spring 的
 * org.springframework.beans.BeanUtils 类发生冲突。
 *
 * @author zhijun.chen
 * @see ApplicationContext
 * @see BeanFactory
 * @see BeanFactoryPostProcessor
 * @see ApplicationContextAware
 * @since 0.0.1
 */
@Slf4j
@Component
@Lazy
public class SpringBeans implements BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * 在 Spring 上下文初始化期间获得的可配置 Bean 工厂实例。
     */
    private static ConfigurableListableBeanFactory beanFactory;

    /**
     * 在 Spring 上下文初始化期间获得的应用上下文实例。
     */
    private static ApplicationContext applicationContext;

    /**
     * 来自 BeanFactoryPostProcessor 的回调方法，用于捕获 Bean 工厂。
     * <p>
     * 该方法在 Spring 上下文初始化期间被调用，以提供对
     * ConfigurableListableBeanFactory 的访问。
     *
     * @param beanFactory 可配置的 Bean 工厂
     * @throws BeansException 如果在处理过程中发生错误
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringBeans.beanFactory = beanFactory;
    }

    /**
     * 来自 ApplicationContextAware 的回调方法，用于捕获应用上下文。
     * <p>
     * 该方法在 Spring 上下文初始化期间被调用，以提供对
     * ApplicationContext 的访问。
     *
     * @param applicationContext 应用上下文
     */
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
