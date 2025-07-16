package io.github.rose.i18n.util;

import io.github.rose.i18n.MessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static io.github.rose.i18n.util.I18nUtils.messageSource;
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
        MessageSource MessageSource = messageSource();
        Locale locale = MessageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    /**
     * Get I18n Message
     * <pre>
     * // Testing Simplified Chinese
     * // null
     * assertEquals(null, MessageUtils.getLocalizedMessage(null));
     * // If the message argument is "a", the pattern "{" "}" is not included, and the original content is returned
     * assertEquals("a", MessageUtils.getLocalizedMessage("a"));
     * // "{a}" is the Message Code template, where "a" is Message Code
     * assertEquals("测试-a", MessageUtils.getLocalizedMessage("{a}"));
     *
     * // The same is true for overloaded methods with Message Pattern arguments
     * assertEquals("hello", MessageUtils.getLocalizedMessage("hello", "World"));
     * assertEquals("您好,World", MessageUtils.getLocalizedMessage("{hello}", "World"));
     *
     * // If message code does not exist, return the original content of message
     * assertEquals("{code-not-found}", MessageUtils.getLocalizedMessage("{code-not-found}"));
     * assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{microsphere-test.code-not-found}"));
     * assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{common.code-not-found}"));
     *
     * // The test of English
     * assertEquals("hello", MessageUtils.getLocalizedMessage("hello", Locale.ENGLISH, "World"));
     * assertEquals("Hello,World", MessageUtils.getLocalizedMessage("{hello}", Locale.ENGLISH, "World"));
     * </pre>
     *
     * @param messagePattern Message or Message Pattern
     * @param locale         {@link Locale}
     * @param args           the arguments of Message Pattern
     * @return Internationalized Message returns the original message if it exists
     */
    public static String getLocalizedMessage(String messagePattern, Locale locale, Object... args) {
        if (messagePattern == null) {
            return null;
        }

        String messageCode = resolveMessageCode(messagePattern);

        if (messageCode == null) {
            logger.debug("Message code not found in messagePattern'{}", messagePattern);
            return messagePattern;
        }

        MessageSource MessageSource = messageSource();
        String localizedMessage = MessageSource.getMessage(messageCode, locale, args);
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
