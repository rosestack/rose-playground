package io.github.rosestack.i18n.spring;

import io.github.rosestack.core.spring.SpringBeanUtils;
import io.github.rosestack.i18n.spring.context.MessageSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.util.ClassUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.aop.support.AopUtils.getTargetClass;


/**
 * Internationalization {@link BeanPostProcessor}, Processing：
 * <ul>
 *     <li>{@link LocalValidatorFactoryBean#setValidationMessageSource(MessageSource)} associates {@link MessageSourceAdapter}</li>
 * </ul>
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
 * @since 1.0.0
 */

public class I18nBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(I18nBeanPostProcessor.class);
    private static final ClassLoader classLoader = I18nBeanPostProcessor.class.getClassLoader();
    private static final Class<?> VALIDATOR_FACTORY_CLASS = ClassUtils.resolveClassName("jakarta.validation.ValidatorFactory", classLoader);
    private static final Class<?> LOCAL_VALIDATOR_FACTORY_BEAN_CLASS = ClassUtils.resolveClassName("org.springframework.validation.beanvalidation.LocalValidatorFactoryBean", classLoader);

    private final ConfigurableApplicationContext context;

    public I18nBeanPostProcessor(ConfigurableApplicationContext context) {
        this.context = context;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (VALIDATOR_FACTORY_CLASS == null || LOCAL_VALIDATOR_FACTORY_BEAN_CLASS == null) {
            return bean;
        }

        Class<?> beanType = getTargetClass(bean);
        if (LOCAL_VALIDATOR_FACTORY_BEAN_CLASS.equals(beanType)) {
            MessageSourceAdapter messageSourceAdapter = SpringBeanUtils.getOptionalBean(context, MessageSourceAdapter.class);
            if (messageSourceAdapter == null) {
                logger.warn("No MessageSourceAdapter BeanDefinition was found!");
            } else {
                LocalValidatorFactoryBean localValidatorFactoryBean = (LocalValidatorFactoryBean) bean;
                localValidatorFactoryBean.setValidationMessageSource(messageSourceAdapter);
                logger.debug("LocalValidatorFactoryBean[name : '{}'] is associated with MessageSource : {}", beanName, messageSourceAdapter);
            }
        }
        return bean;
    }
}