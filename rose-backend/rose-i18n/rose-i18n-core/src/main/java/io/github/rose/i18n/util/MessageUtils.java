package io.github.rose.i18n.util;

import io.github.rose.i18n.I18nMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBetween;

/**
 * Message Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class MessageUtils {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    /**
     * Message Code pattern prefix
     */
    public static final String MESSAGE_PATTERN_PREFIX = "{";

    /**
     * Message Code pattern suffix
     */
    public static final String MESSAGE_PATTERN_SUFFIX = "}";

    private MessageUtils() {
    }

    /**
     * Get I18n Message
     *
     * @param messagePattern Message or Message Pattern
     * @param args           the arguments of Message Pattern
     * @return Internationalized Message returns the original message if it exists
     */
    public static String getLocalizedMessage(String messagePattern, Object... args) {
        I18nMessageSource i18nMessageSource = I18nUtils.i18nMessageSource();
        Locale locale = i18nMessageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    public static String getLocalizedMessage(String messagePattern, Locale locale, Object... args) {
        if (messagePattern == null) {
            return null;
        }

        String messageCode = resolveMessageCode(messagePattern);

        if (messageCode == null) {
            logger.debug("Message code not found in messagePattern'{}", messagePattern);
            return messagePattern;
        }

        I18nMessageSource i18nMessageSource = I18nUtils.i18nMessageSource();
        String localizedMessage = i18nMessageSource.getMessage(messageCode, locale, args);
        if (isNotBlank(localizedMessage)) {
            logger.debug("Message Pattern ['{}'] corresponds to Locale ['{}'] with MessageSage:'{}'", messagePattern, locale, localizedMessage);
        } else {
            int afterDotIndex = messageCode.indexOf(".") + 1;
            if (afterDotIndex > 0 && afterDotIndex < messageCode.length()) {
                localizedMessage = messageCode.substring(afterDotIndex);
            } else {
                localizedMessage = messagePattern;
            }
            logger.debug("No Message['{}'] found for Message Pattern ['{}'], returned: {}", messagePattern, locale, localizedMessage);
        }

        return localizedMessage;
    }

    public static String resolveMessageCode(String messagePattern) {
        String messageCode = substringBetween(messagePattern, MESSAGE_PATTERN_PREFIX, MESSAGE_PATTERN_SUFFIX);
        return messageCode;
    }
}