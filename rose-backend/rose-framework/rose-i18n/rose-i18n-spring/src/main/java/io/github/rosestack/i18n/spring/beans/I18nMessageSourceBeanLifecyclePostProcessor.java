package io.github.rosestack.i18n.spring.beans;

import io.github.rosestack.i18n.I18nMessageSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.INFER_METHOD;

/**
 * The PostProcessor processes the lifecycle of {@link I18nMessageSource} Beans automatically.
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
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
            return;
        }
        String initMethodName = beanDefinition.getInitMethodName();
        if (StringUtils.isEmpty(initMethodName)) {
            beanDefinition.setInitMethodName("init");
        }
    }

    private void setDestroyMethodName(RootBeanDefinition beanDefinition, Class<?> beanType) {
        if (DisposableBean.class.isAssignableFrom(beanType)) {
            return;
        }
        String destroyMethodName = beanDefinition.getDestroyMethodName();

        if (INFER_METHOD.equals(destroyMethodName)) {
            return;
        }
        if (StringUtils.isEmpty(destroyMethodName)) {
            beanDefinition.setDestroyMethodName("destroy");
        }
    }
}