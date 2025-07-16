package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeMessageSource;
import io.github.rose.i18n.impl.EmptyMessageSource;
import io.github.rose.i18n.MessageSource;
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

    private static volatile MessageSource messageSource;

    public static MessageSource messageSource() {
        if (messageSource == null) {
            logger.warn("MessageSource is not initialized, EmptyMessageSource will be used");
            return EmptyMessageSource.INSTANCE;
        }
        return messageSource;
    }

    public static void setMessageSource(MessageSource MessageSource) {
        I18nUtils.messageSource = MessageSource;
        logger.debug("I18nUtils.MessageSource is initialized : {}", MessageSource);
    }

    public static void destroyMessageSource() {
        messageSource = null;
        logger.debug("MessageSource is destroyed");
    }

    public static List<MessageSource> findAllMessageSources(MessageSource MessageSource) {
        List<MessageSource> allMessageSources = new LinkedList<>();
        initMessageSources(MessageSource, allMessageSources);
        return unmodifiableList(allMessageSources);
    }

    public static void initMessageSources(MessageSource MessageSource,
                                                 List<MessageSource> allMessageSources) {
        if (MessageSource instanceof CompositeMessageSource) {
            CompositeMessageSource compositeMessageSource = (CompositeMessageSource) MessageSource;
            for (MessageSource subMessageSource : compositeMessageSource.getMessageSources()) {
                initMessageSources(subMessageSource, allMessageSources);
            }
        } else {
            allMessageSources.add(MessageSource);
        }
    }
}
