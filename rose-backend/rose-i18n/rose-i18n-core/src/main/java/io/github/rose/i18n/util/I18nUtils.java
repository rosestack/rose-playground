package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeI18nMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spi.EmptyI18nMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Internationalization Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile I18nMessageSource i18nMessageSource;

    public static I18nMessageSource i18nMessageSource() {
        if (i18nMessageSource == null) {
            logger.warn("i18nMessageSource is not initialized, EmptyI18nMessageSource will be used");
            return EmptyI18nMessageSource.INSTANCE;
        }
        return i18nMessageSource;
    }

    public static void setI18nMessageSource(I18nMessageSource i18nMessageSource) {
        I18nUtils.i18nMessageSource = i18nMessageSource;
        logger.debug("I18nUtils.i18nMessageSource is initialized : {}", i18nMessageSource);
    }

    public static void destroyI18nMessageSource() {
        i18nMessageSource = null;
        logger.debug("i18nMessageSource is destroyed");
    }

    public static List<I18nMessageSource> findAllI18nMessageSources(I18nMessageSource i18nMessageSource) {
        List<I18nMessageSource> allI18nMessageSources = new LinkedList<>();
        initI18nMessageSources(i18nMessageSource, allI18nMessageSources);
        return unmodifiableList(allI18nMessageSources);
    }

    public static void initI18nMessageSources(I18nMessageSource serviceMessageSource,
                                              List<I18nMessageSource> allI18nMessageSources) {
        if (serviceMessageSource instanceof CompositeI18nMessageSource) {
            CompositeI18nMessageSource compositeI18nMessageSource = (CompositeI18nMessageSource) serviceMessageSource;
            for (I18nMessageSource subI18nMessageSource : compositeI18nMessageSource.getServiceMessageSources()) {
                initI18nMessageSources(subI18nMessageSource, allI18nMessageSources);
            }
        } else {
            allI18nMessageSources.add(serviceMessageSource);
        }
    }
}