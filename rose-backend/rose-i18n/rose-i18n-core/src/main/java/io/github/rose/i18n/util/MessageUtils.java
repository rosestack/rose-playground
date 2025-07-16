package io.github.rose.i18n.util;

import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.MessageSourceManager;

import java.util.Locale;

/**
 * Message Utilities class
 * <p>
 * 提供国际化消息解析、占位符处理等工具方法。
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class MessageUtils {
    /** 消息占位符前缀 */
    public static final String MESSAGE_PATTERN_PREFIX = "{";
    /** 消息占位符后缀 */
    public static final String MESSAGE_PATTERN_SUFFIX = "}";

    private MessageUtils() {}

    /**
     * 获取国际化消息（自动获取当前 Locale）
     * @param messagePattern 消息或消息占位符
     * @param args           占位符参数
     * @return 国际化消息，若无则返回原文
     */
    public static String getLocalizedMessage(String messagePattern, Object... args) {
        MessageSource messageSource = MessageSourceManager.getInstance();
        Locale locale = messageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    /**
     * 获取国际化消息（指定 Locale）
     * @param messagePattern 消息或消息占位符
     * @param locale         区域
     * @param args           占位符参数
     * @return 国际化消息，若无则返回原文
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
     * 解析消息占位符，提取 code
     * @param messagePattern 消息模式
     * @return code 或 null
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