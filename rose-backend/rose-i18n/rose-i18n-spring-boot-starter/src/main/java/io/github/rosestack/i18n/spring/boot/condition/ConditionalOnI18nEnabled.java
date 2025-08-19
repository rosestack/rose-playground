package io.github.rosestack.i18n.spring.boot.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

import static io.github.rosestack.i18n.spring.I18nConstants.ENABLED_PROPERTY_NAME;

/**
 * {@link Conditional @Conditional} that checks whether the I18n enabled
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnClass(
	name = {
		"io.github.rosestack.i18n.I18nMessageSource", // rose-i18n-core
		"io.github.rosestack.i18n.spring.annotation.EnableI18n", // rose-i18n-spring
	})
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnI18nEnabled {
}
