package io.github.rose.common.config;

import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Properties;


/**
 * Bean Validation 配置类
 * <p>
 * 配置 Hibernate Validator 作为 JSR-303 验证实现，支持国际化消息和快速失败模式。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>集成 Spring MessageSource 实现国际化验证消息</li>
 *   <li>启用快速失败模式，提升验证性能</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @RestController
 * public class UserController {
 *
 *     @PostMapping("/users")
 *     public Result<Void> createUser(@Valid @RequestBody UserDto userDto) {
 *         // 验证失败时自动抛出 MethodArgumentNotValidException
 *         return Result.success();
 *     }
 * }
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see Validator
 * @see HibernateValidator
 */
@AutoConfiguration
public class ValidatorConfig {

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
