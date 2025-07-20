package io.github.rose.common.exception;

import io.github.rose.core.exception.BusinessException;
import io.github.rose.core.spring.SpringBeans;
import io.github.rose.core.util.FormatUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

public class ExceptionMessageResolver {
    private static volatile MessageSource messageSource;

    /**
     * 解析异常消息
     *
     * @param code           消息代码
     * @param args           消息参数
     * @param defaultMessage 默认消息
     * @return 解析后的消息
     */
    public static String resolveMessage(String code, Object[] args, String defaultMessage) {
        return resolveMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 解析异常消息
     *
     * @param code           消息代码
     * @param args           消息参数
     * @param defaultMessage 默认消息
     * @param locale         语言环境
     * @return 解析后的消息
     */
    public static String resolveMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String message = null;

        // 1. 尝试从MessageSource获取国际化消息
        if (!StringUtils.isEmpty(code)) {
            MessageSource msgSource = getMessageSource();
            if (msgSource != null) {
                try {
                    message = msgSource.getMessage(code, args, locale);
                } catch (Exception e) {
                    // 消息获取失败，继续后续处理
                }
            }

            // 2. 如果MessageSource获取失败，使用FormatUtils处理占位符
            if (message == null) {
                message = FormatUtils.replacePlaceholders(code, args);
            }
        }

        // 3. 如果仍然为空，使用默认消息
        if (message == null) {
            message = defaultMessage;
        }

        return message;
    }

    /**
     * 专门处理BusinessException的消息解析
     *
     * @param exception BusinessException实例
     * @return 解析后的消息
     */
    public static String resolveBusinessExceptionMessage(BusinessException exception) {
        return resolveBusinessExceptionMessage(exception, LocaleContextHolder.getLocale());
    }

    /**
     * 专门处理BusinessException的消息解析
     *
     * @param exception BusinessException实例
     * @param locale    语言环境
     * @return 解析后的消息
     */
    public static String resolveBusinessExceptionMessage(BusinessException exception, Locale locale) {
        // 如果不需要国际化，直接返回默认消息或getMessage()
        if (!exception.isNeedsInternationalization()) {
            String defaultMessage = exception.getDefaultMessage();
            return defaultMessage != null ? defaultMessage : exception.getMessage();
        }

        // 需要国际化处理
        return resolveMessage(
                exception.getMessageCode(),
                exception.getMessageArgs(),
                exception.getDefaultMessage(),
                locale
        );
    }

    /**
     * 快速创建国际化异常消息
     *
     * @param messageCode 消息编码
     * @param args        消息参数
     * @return 解析后的消息
     */
    public static String resolveI18nMessage(String messageCode, Object... args) {
        return resolveMessage(messageCode, args, null, LocaleContextHolder.getLocale());
    }

    /**
     * 快速创建国际化异常消息（带默认消息）
     *
     * @param messageCode    消息编码
     * @param defaultMessage 默认消息
     * @param args           消息参数
     * @return 解析后的消息
     */
    public static String resolveI18nMessage(String messageCode, String defaultMessage, Object... args) {
        return resolveMessage(messageCode, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 获取MessageSource，支持懒加载和缓存
     */
    private static MessageSource getMessageSource() {
        if (messageSource == null) {
            synchronized (ExceptionMessageResolver.class) {
                if (messageSource == null) {
                    try {
                        messageSource = SpringBeans.getBean(MessageSource.class);
                    } catch (Exception e) {
                        // Spring上下文未初始化或Bean不存在，返回null
                        return null;
                    }
                }
            }
        }
        return messageSource;
    }

    /**
     * 清除缓存的MessageSource（用于测试）
     */
    public static void clearCache() {
        messageSource = null;
    }

    /**
     * 设置MessageSource（用于测试）
     */
    public static void setMessageSource(MessageSource messageSource) {
        ExceptionMessageResolver.messageSource = messageSource;
    }
}
