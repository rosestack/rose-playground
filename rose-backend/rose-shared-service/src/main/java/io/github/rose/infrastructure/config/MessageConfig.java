
package io.github.rose.infrastructure.config;

import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Properties;

@AutoConfiguration
@EnableWebMvc
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * 创建自定义验证器
     * <p>
     * 配置 Hibernate Validator 支持国际化消息和快速失败模式。
     * 快速失败模式下，遇到第一个验证错误即停止后续验证。
     *
     * @param messageSource 国际化消息源
     * @return 配置完成的验证器
     */
    @Bean
    public Validator validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setValidationMessageSource(messageSource);
        factoryBean.setProviderClass(HibernateValidator.class);

        Properties properties = new Properties();
        properties.setProperty("hibernate.validator.fail_fast", "true");
        factoryBean.setValidationProperties(properties);
        factoryBean.afterPropertiesSet();

        return factoryBean.getValidator();
    }
}