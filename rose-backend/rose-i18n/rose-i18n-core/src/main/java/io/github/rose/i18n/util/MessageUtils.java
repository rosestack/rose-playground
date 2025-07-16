package io.github.rose.i18n.util;

import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.MessageSourceManager;

import java.util.Locale;

/**
 * Message utility class.
 * <p>
 * Provides internationalized message parsing, placeholder handling, and related utilities.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class MessageUtils {
    /** Message placeholder prefix */
    public static final String MESSAGE_PATTERN_PREFIX = "{";
    /** Message placeholder suffix */
    public static final String MESSAGE_PATTERN_SUFFIX = "}";

    private MessageUtils() {}

    /**
     * Get the localized message (auto-detect current Locale).
     * @param messagePattern message or message placeholder
     * @param args           placeholder arguments
     * @return localized message, or original text if not found
     */
    public static String getLocalizedMessage(String messagePattern, Object... args) {
        MessageSource messageSource = MessageSourceManager.getInstance();
        Locale locale = messageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    /**
     * Get the localized message (specify Locale).
     * @param messagePattern message or message placeholder
     * @param locale         locale
     * @param args           placeholder arguments
     * @return localized message, or original text if not found
     */
    public static String getLocalizedMessage(String messagePattern, Locale locale, Object... args) {
        if (messagePattern == null) return null;
        String messageCode = extractMessageCode(messagePattern);
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

    /**
     * Parse message placeholder, extract code.
     * @param messagePattern message pattern
     * @return code or null
     */
    public static String extractMessageCode(String messagePattern) {
        if (messagePattern == null) return null;
        int start = messagePattern.indexOf(MESSAGE_PATTERN_PREFIX);
        int end = messagePattern.indexOf(MESSAGE_PATTERN_SUFFIX, start + 1);
        if (start != -1 && end != -1 && end > start) {
            return messagePattern.substring(start + 1, end);
        }
        return null;
    }
}