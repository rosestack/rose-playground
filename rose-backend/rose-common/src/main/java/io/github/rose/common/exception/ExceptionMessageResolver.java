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

    public static String resolveMessage(String messageCode, String defaultMessage) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), null);
    }

    /**
     * 快速创建国际化异常消息
     *
     * @param messageCode 消息编码
     * @param args        消息参数
     * @return 解析后的消息
     */
    public static String resolveMessage(String messageCode, Object[] args) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), args);
    }

    /**
     * 快速创建国际化异常消息（带默认消息）
     *
     * @param messageCode    消息编码
     * @param defaultMessage 默认消息
     * @param args           消息参数
     * @return 解析后的消息
     */
    public static String resolveMessage(String messageCode, String defaultMessage, Object[] args) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), args);
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
    public static String resolveMessage(String code, String defaultMessage, Locale locale, Object[] args) {
        String message = null;

        if (!StringUtils.isEmpty(code)) {
            MessageSource msgSource = getMessageSource();
            if (msgSource != null) {
                try {
                    message = msgSource.getMessage(code, args, locale);
                } catch (Exception e) {
                    // 消息获取失败，继续后续处理
                }
            }
        }

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
    public static String resolveMessage(BusinessException exception) {
        return resolveMessage(exception, LocaleContextHolder.getLocale());
    }

    /**
     * 专门处理BusinessException的消息解析
     *
     * @param exception BusinessException实例
     * @param locale    语言环境
     * @return 解析后的消息
     */
    public static String resolveMessage(BusinessException exception, Locale locale) {
        // 如果不需要国际化，直接返回默认消息或getMessage()
        if (!exception.isNeedsInternationalization()) {
            String defaultMessage = exception.getDefaultMessage();
            return defaultMessage != null ? defaultMessage : exception.getMessage();
        }

        // 需要国际化处理
        return resolveMessage(
                exception.getMessageCode(),
                exception.getDefaultMessage(),
                locale,
                exception.getMessageArgs()
        );
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
