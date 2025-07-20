package io.github.rose.common.config;

import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Properties;


@AutoConfiguration
public class ValidatorConfig {


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
