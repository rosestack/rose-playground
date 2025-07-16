package io.github.rose.i18n.util;

import io.github.rose.i18n.I18nMessageSource;

import java.util.Locale;

/**
 * Message Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class MessageUtils {

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
            // Message code not found, return original pattern
            return messagePattern;
        }

        I18nMessageSource i18nMessageSource = I18nUtils.i18nMessageSource();
        String localizedMessage = i18nMessageSource.getMessage(messageCode, locale, args);
        if (localizedMessage != null && !localizedMessage.isBlank()) {
            // found localized message
        } else {
            int afterDotIndex = messageCode.indexOf(".") + 1;
            if (afterDotIndex > 0 && afterDotIndex < messageCode.length()) {
                localizedMessage = messageCode.substring(afterDotIndex);
            } else {
                localizedMessage = messagePattern;
            }
        }

        return localizedMessage;
    }

    public static String resolveMessageCode(String messagePattern) {
        if (messagePattern == null) return null;
        int start = messagePattern.indexOf(MESSAGE_PATTERN_PREFIX);
        int end = messagePattern.indexOf(MESSAGE_PATTERN_SUFFIX, start + 1);
        if (start != -1 && end != -1 && end > start) {
            return messagePattern.substring(start + 1, end);
        }
        return null;
    }
}