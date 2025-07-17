package io.github.rose.i18n;

import java.util.Locale;

/**
 * 国际化消息未找到异常
 * 
 * <p>当请求的消息键在指定的语言环境中不存在时抛出此异常。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class I18nMessageNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageKey;
    private final Locale locale;

    /**
     * 构造函数
     * 
     * @param messageKey 消息键
     * @param locale 语言环境
     */
    public I18nMessageNotFoundException(String messageKey, Locale locale) {
        super(String.format("Message not found for key '%s' in locale '%s'", messageKey, locale));
        this.messageKey = messageKey;
        this.locale = locale;
    }

    /**
     * 构造函数
     * 
     * @param messageKey 消息键
     * @param locale 语言环境
     * @param cause 原因异常
     */
    public I18nMessageNotFoundException(String messageKey, Locale locale, Throwable cause) {
        super(String.format("Message not found for key '%s' in locale '%s'", messageKey, locale), cause);
        this.messageKey = messageKey;
        this.locale = locale;
    }

    /**
     * 获取消息键
     * 
     * @return 消息键
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * 获取语言环境
     * 
     * @return 语言环境
     */
    public Locale getLocale() {
        return locale;
    }
}
