package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spi.EmptyMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile I18nMessageSource messageSource;

    public static I18nMessageSource messageSource() {
        if (messageSource == null) {
            logger.warn("serviceMessageSource is not initialized, EmptyServiceMessageSource will be used");
            return EmptyMessageSource.INSTANCE;
        }
        return messageSource;
    }

    public static void setMessageSource(I18nMessageSource serviceMessageSource) {
        I18nUtils.messageSource = serviceMessageSource;
        logger.debug("I18nUtils.serviceMessageSource is initialized : {}", serviceMessageSource);
    }

    public static void destroyMessageSource() {
        messageSource = null;
        logger.debug("messageSource is destroyed");
    }

    public static List<I18nMessageSource> findAllMessageSources(I18nMessageSource serviceMessageSource) {
        List<I18nMessageSource> allServiceMessageSources = new LinkedList<>();
        initMessageSources(serviceMessageSource, allServiceMessageSources);
        return Collections.unmodifiableList(allServiceMessageSources);
    }

    public static void initMessageSources(I18nMessageSource serviceMessageSource,
                                          List<I18nMessageSource> allServiceMessageSources) {
        if (serviceMessageSource instanceof CompositeMessageSource) {
            CompositeMessageSource compositeServiceMessageSource = (CompositeMessageSource) serviceMessageSource;
            for (I18nMessageSource subServiceMessageSource : compositeServiceMessageSource.getMessageSources()) {
                initMessageSources(subServiceMessageSource, allServiceMessageSources);
            }
        } else {
            allServiceMessageSources.add(serviceMessageSource);
        }
    }
}