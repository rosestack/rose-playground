package io.github.rose.i18n.spring.context;

import io.github.rose.i18n.I18nMessageSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

public class MessageSourceAdapter implements MessageSource, SmartInitializingSingleton {

    private final I18nMessageSource i18nMessageSource;

    private final ObjectProvider<MessageSource> messageSourceProvider;

    private MessageSource defaultMessageSource;

    public MessageSourceAdapter(I18nMessageSource i18nMessageSource, ObjectProvider<MessageSource> messageSourceProvider) {
        this.i18nMessageSource = i18nMessageSource;
        this.messageSourceProvider = messageSourceProvider;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String message = i18nMessageSource.getMessage(code, args, locale);
        if (message == null) {
            message = getDefaultMessage(code, args, defaultMessage, locale);
        }
        return message;
    }

    private String getDefaultMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (defaultMessageSource != null) {
            return defaultMessageSource.getMessage(code, args, defaultMessage, locale);
        }
        return defaultMessage;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getMessage(code, args, null, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String message = null;
        for (String code : resolvable.getCodes()) {
            message = getMessage(code, resolvable.getArguments(), resolvable.getDefaultMessage(), locale);
            if (message != null) {
                break;
            }
        }
        return message;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.defaultMessageSource = messageSourceProvider.getIfAvailable();
    }

    @Override
    public String toString() {
        return "MessageSourceAdapter{" +
                "i18nMessageSource=" + i18nMessageSource +
                ", defaultMessageSource=" + defaultMessageSource +
                '}';
    }
}