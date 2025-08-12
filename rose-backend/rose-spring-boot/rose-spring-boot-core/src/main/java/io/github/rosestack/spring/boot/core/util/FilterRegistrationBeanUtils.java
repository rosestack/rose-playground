package io.github.rosestack.spring.boot.core.util;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class FilterRegistrationBeanUtils {
    /**
     * 创建 Filter 注册 Bean
     * <p>
     * 为 Servlet Filter 创建 FilterRegistrationBean，用于在 Spring Boot 中注册过滤器
     * </p>
     *
     * @param <T>    Filter 类型，必须实现 jakarta.servlet.Filter 接口
     * @param filter Filter 实例，不能为 null
     * @param order  过滤器执行顺序，数值越小优先级越高
     * @return FilterRegistrationBean 实例
     * @throws IllegalArgumentException 如果 filter 为 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }

        FilterRegistrationBean<T> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName(filter.getClass().getSimpleName());

        if (order != null) {
            registrationBean.setOrder(order);
        }

        return registrationBean;
    }
}
