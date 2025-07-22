package io.github.rose.common.exception;

import io.github.rose.core.exception.BusinessException;
import io.github.rose.core.spring.SpringBeans;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 异常消息解析工具类
 * <p>
 * 提供异常消息的国际化解析功能，支持从 Spring MessageSource 获取本地化消息。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>支持国际化消息解析</li>
 *   <li>线程安全的 MessageSource 缓存</li>
 *   <li>支持参数化消息和默认消息回退</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 简单消息解析
 * String message = ExceptionMessageResolver.resolveMessage("user.not.found");
 *
 * // 带参数的消息解析
 * String message = ExceptionMessageResolver.resolveMessage("user.not.found",
 *     new Object[]{"john", 123});
 *
 * // BusinessException 消息解析
 * String message = ExceptionMessageResolver.resolveMessage(businessException);
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see MessageSource
 * @see BusinessException
 */
public class ExceptionMessageResolver {

    /** 缓存的消息源，用于国际化消息解析 */
    private static volatile MessageSource messageSource;

    /**
     * 解析消息代码为本地化消息
     *
     * @param messageCode 消息代码
     * @return 解析后的消息，解析失败返回 null
     */
    public static String resolveMessage(String messageCode) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), null);
    }

    /**
     * 解析带参数的消息代码为本地化消息
     *
     * @param messageCode 消息代码
     * @param args 消息参数
     * @return 解析后的消息，解析失败返回 null
     */
    public static String resolveMessage(String messageCode, Object[] args) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), args);
    }

    /**
     * 解析消息代码为本地化消息，支持默认消息回退
     *
     * @param messageCode 消息代码
     * @param defaultMessage 默认消息
     * @return 解析后的消息，解析失败返回默认消息
     */
    public static String resolveMessage(String messageCode, String defaultMessage) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), null);
    }

    /**
     * 解析消息代码为本地化消息（完整参数版本）
     * <p>
     * 核心消息解析方法，支持指定区域设置、参数和默认消息。
     *
     * @param messageCode 消息代码
     * @param defaultMessage 默认消息，解析失败时返回
     * @param locale 区域设置
     * @param args 消息参数
     * @return 解析后的消息，解析失败返回默认消息
     */
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

    /**
     * 解析业务异常消息
     *
     * @param exception 业务异常
     * @return 解析后的异常消息
     */
    public static String resolveMessage(BusinessException exception) {
        return resolveMessage(exception, LocaleContextHolder.getLocale());
    }

    /**
     * 解析业务异常消息，支持指定区域设置
     * <p>
     * 根据异常的国际化标识决定是否进行消息解析。
     *
     * @param exception 业务异常
     * @param locale 区域设置
     * @return 解析后的异常消息
     */
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

    /**
     * 获取消息源实例
     * <p>
     * 使用双重检查锁定模式确保线程安全的延迟初始化。
     *
     * @return MessageSource 实例，获取失败返回 null
     */
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

    /**
     * 清除消息源缓存
     * <p>
     * 主要用于测试场景或需要重新加载消息源的情况。
     */
    public static void clearCache() {
        messageSource = null;
    }

    /**
     * 设置消息源实例
     * <p>
     * 主要用于测试场景或手动设置消息源。
     *
     * @param messageSource 消息源实例
     */
    public static void setMessageSource(MessageSource messageSource) {
        ExceptionMessageResolver.messageSource = messageSource;
    }
}