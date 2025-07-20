package io.github.rose.common.config;

import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Properties;

/**
 * Validation framework configuration class for Bean Validation setup.
 *
 * This configuration class provides a customized Bean Validation setup using Hibernate Validator
 * with internationalization support and fail-fast behavior. It integrates with Spring's
 * MessageSource for localized validation messages and configures the validator for optimal
 * performance in web applications.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Internationalization:</strong> Integrates with Spring MessageSource for localized error messages</li>
 *   <li><strong>Fail-Fast Mode:</strong> Stops validation on first constraint violation for better performance</li>
 *   <li><strong>Hibernate Validator:</strong> Uses Hibernate Validator as the JSR-303 implementation</li>
 *   <li><strong>Auto-Configuration:</strong> Automatically configured when present on classpath</li>
 * </ul>
 *
 * <h3>Validation Behavior:</h3>
 * The configured validator operates in fail-fast mode, which means it will stop validation
 * as soon as the first constraint violation is encountered. This improves performance for
 * objects with multiple validation constraints, especially in scenarios where early
 * validation failure is acceptable.
 *
 * <h3>Message Resolution:</h3>
 * Validation error messages are resolved through the configured MessageSource, enabling
 * proper internationalization support. Messages can be customized per locale and will
 * fall back to default messages if locale-specific versions are not available.
 *
 * <h3>Usage:</h3>
 * This configuration is automatically applied when the class is on the classpath.
 * The configured Validator can be injected into any Spring-managed component:
 *
 * <pre>{@code
 * @Autowired
 * private Validator validator;
 *
 * public void validateObject(Object obj) {
 *     Set<ConstraintViolation<Object>> violations = validator.validate(obj);
 *     // Handle violations...
 * }
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see Validator
 * @see HibernateValidator
 * @see MessageSource
 * @see LocalValidatorFactoryBean
 */
@AutoConfiguration
public class ValidatorConfig {

    /**
     * Creates and configures a Bean Validation Validator with internationalization and fail-fast support.
     *
     * This method sets up a LocalValidatorFactoryBean with the following configurations:
     * <ul>
     *   <li><strong>Message Source Integration:</strong> Uses the provided MessageSource for i18n support</li>
     *   <li><strong>Hibernate Validator:</strong> Explicitly sets Hibernate Validator as the provider</li>
     *   <li><strong>Fail-Fast Mode:</strong> Enables fail-fast validation for improved performance</li>
     * </ul>
     *
     * <p><strong>Fail-Fast Behavior:</strong>
     * When fail-fast mode is enabled, the validator will stop processing validation constraints
     * as soon as the first violation is found. This can significantly improve performance for
     * complex objects with many validation rules, especially when early failure detection is
     * sufficient for the application's needs.
     *
     * <p><strong>Internationalization Support:</strong>
     * The validator integrates with Spring's MessageSource to provide localized validation
     * error messages. This allows for proper internationalization of validation messages
     * based on the user's locale or application configuration.
     *
     * <p><strong>Configuration Properties:</strong>
     * The method sets the following Hibernate Validator properties:
     * <ul>
     *   <li><code>hibernate.validator.fail_fast=true</code> - Enables fail-fast validation mode</li>
     * </ul>
     *
     * @param messageSource The Spring MessageSource for internationalized validation messages.
     *                     Must not be null. Used to resolve validation error messages based on locale.
     * @return A fully configured Validator instance ready for use in validation operations
     *
     * @throws IllegalStateException if the LocalValidatorFactoryBean cannot be properly initialized
     * @see LocalValidatorFactoryBean#setValidationMessageSource(MessageSource)
     * @see LocalValidatorFactoryBean#setProviderClass(Class)
     * @see LocalValidatorFactoryBean#setValidationProperties(Properties)
     */
    @Bean
    public Validator validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();

        // Configure internationalization support through MessageSource integration
        // This enables localized validation error messages based on user locale
        factoryBean.setValidationMessageSource(messageSource);

        // Explicitly set Hibernate Validator as the JSR-303 Bean Validation provider
        // This ensures consistent behavior and access to Hibernate-specific features
        factoryBean.setProviderClass(HibernateValidator.class);

        // Configure Hibernate Validator specific properties
        Properties properties = new Properties();

        // Enable fail-fast mode for improved performance
        // Validation will stop on the first constraint violation encountered
        properties.setProperty("hibernate.validator.fail_fast", "true");
        factoryBean.setValidationProperties(properties);

        // Initialize the factory bean to prepare the validator for use
        factoryBean.afterPropertiesSet();

        return factoryBean.getValidator();
    }
}
