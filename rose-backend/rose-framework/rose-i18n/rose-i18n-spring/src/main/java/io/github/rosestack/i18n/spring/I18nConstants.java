package io.github.rosestack.i18n.spring;

import io.github.rosestack.i18n.I18nMessageSource;

import java.util.Locale;

/**
 * Internationalization property constants
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
 * @since 1.0.0
 */
public interface I18nConstants {

    String PROPERTY_NAME_PREFIX = "rose.i18n.";

    /**
     * Enabled Configuration Name
     */
    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    /**
     * Enabled By Default
     */
    boolean DEFAULT_ENABLED = true;

    /**
     * The property name of the {@link I18nMessageSource#getSource() sources} of {@link I18nMessageSource}
     *
     * @see I18nMessageSource#getSource()
     */
    String SOURCES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "sources";

    /**
     * Default {@link Locale} property name
     */
    String DEFAULT_LOCALE_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "default-locale";

    /**
     * Supported {@link Locale} list property names
     */
    String SUPPORTED_LOCALES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "supported-locales";

    /**
     * The Primary {@link I18nMessageSource} Bean Bean
     */
    String I18N_MESSAGE_SOURCE_BEAN_NAME = "i18nMessageSource";
}
