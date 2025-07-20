package io.github.rose.common.exception;

import io.github.rose.core.exception.BusinessException;
import io.github.rose.core.spring.SpringBeans;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

public class ExceptionMessageResolver {

    private static volatile MessageSource messageSource;

    public static String resolveMessage(String messageCode) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), null);
    }

    public static String resolveMessage(String messageCode, Object[] args) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), args);
    }

    public static String resolveMessage(String messageCode, String defaultMessage) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), null);
    }

    public static String resolveMessage(String messageCode, String defaultMessage, Locale locale, Object[] args) {
        String message = null;

        if (!StringUtils.isEmpty(messageCode)) {
            MessageSource msgSource = getMessageSource();
            if (msgSource != null) {
                try {
                    message = msgSource.getMessage(messageCode, args, locale);
                } catch (Exception e) {
                    // Ignore and use default message
                }
            }
        }

        return message != null ? message : defaultMessage;
    }

    public static String resolveMessage(BusinessException exception) {
        return resolveMessage(exception, LocaleContextHolder.getLocale());
    }

    public static String resolveMessage(BusinessException exception, Locale locale) {
        if (!exception.isNeedsInternationalization()) {
            String defaultMessage = exception.getDefaultMessage();
            return defaultMessage != null ? defaultMessage : exception.getMessage();
        }

        return resolveMessage(
                exception.getMessageCode(),
                exception.getDefaultMessage(),
                locale,
                exception.getMessageArgs()
        );
    }

    private static MessageSource getMessageSource() {
        if (messageSource == null) {
            synchronized (ExceptionMessageResolver.class) {
                if (messageSource == null) {
                    try {
                        messageSource = SpringBeans.getBean(MessageSource.class);
                    } catch (Exception e) {
                        // MessageSource not available
                    }
                }
            }
        }
        return messageSource;
    }

    public static void clearCache() {
        messageSource = null;
    }

    public static void setMessageSource(MessageSource messageSource) {
        ExceptionMessageResolver.messageSource = messageSource;
    }
}