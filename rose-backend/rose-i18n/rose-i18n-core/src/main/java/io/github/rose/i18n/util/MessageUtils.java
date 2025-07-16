package io.github.rose.i18n.util;

import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.MessageSourceManager;

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
        MessageSource messageSource = MessageSourceManager.getInstance();
        Locale locale = messageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    public static String getLocalizedMessage(String messagePattern, Locale locale, Object... args) {
        if (messagePattern == null) return null;
        String messageCode = resolveMessageCode(messagePattern);
        if (messageCode == null) return messagePattern;

        MessageSource messageSource = MessageSourceManager.getInstance();
        String localizedMessage = messageSource.getMessage(messageCode, locale, args);
        if (localizedMessage != null && !localizedMessage.isBlank()) {
            return localizedMessage;
        }
        int afterDotIndex = messageCode.indexOf('.') + 1;
        return (afterDotIndex > 0 && afterDotIndex < messageCode.length())
                ? messageCode.substring(afterDotIndex)
                : messagePattern;
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