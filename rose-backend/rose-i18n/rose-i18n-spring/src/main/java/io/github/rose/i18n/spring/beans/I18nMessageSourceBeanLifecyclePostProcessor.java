package io.github.rose.i18n.spring.beans;

import io.github.rose.i18n.I18nMessageSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.INFER_METHOD;

/**
 * The PostProcessor processes the lifecycle of {@link I18nMessageSource} Beans automatically.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nMessageSource
 * @see MergedBeanDefinitionPostProcessor
 * @since 1.0.0
 */
public class I18nMessageSourceBeanLifecyclePostProcessor implements MergedBeanDefinitionPostProcessor {

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (I18nMessageSource.class.isAssignableFrom(beanType)) {
            setInitMethodName(beanDefinition, beanType);
            setDestroyMethodName(beanDefinition, beanType);
        }
    }

    private void setInitMethodName(RootBeanDefinition beanDefinition, Class<?> beanType) {
        if (InitializingBean.class.isAssignableFrom(beanType)) {
            // If ServiceMessageSource bean implements the interface InitializingBean,
            // it's ignored immediately.
            return;
        }
        String initMethodName = beanDefinition.getInitMethodName();
        if (StringUtils.isEmpty(initMethodName)) {
            // If The BeanDefinition does not declare the initialization method,
            // ServiceMessageSource#init() method should be a candidate.
            beanDefinition.setInitMethodName("init");
        }
    }

    private void setDestroyMethodName(RootBeanDefinition beanDefinition, Class<?> beanType) {
        if (DisposableBean.class.isAssignableFrom(beanType)) {
            // If ServiceMessageSource bean implements the interface DisposableBean,
            // it's ignored immediately.
            return;
        }
        String destroyMethodName = beanDefinition.getDestroyMethodName();

        if (INFER_METHOD.equals(destroyMethodName)) {
            // If the "(inferred)" method was found, return immediately.
            return;
        }
        if (StringUtils.isEmpty(destroyMethodName)) {
            // If The BeanDefinition does not declare the destroy method,
            // ServiceMessageSource#destroy() method should be a candidate.
            beanDefinition.setDestroyMethodName("destroy");
        }
    }
}