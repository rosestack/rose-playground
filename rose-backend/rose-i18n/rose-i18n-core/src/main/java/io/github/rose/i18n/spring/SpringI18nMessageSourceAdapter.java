package io.github.rose.i18n.spring;

import io.github.rose.i18n.*;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

/**
 * Spring MessageSource适配器
 * 
 * <p>将Rose I18n的I18nMessageSource适配为Spring的MessageSource接口，
 * 实现与Spring框架的无缝集成。</p>
 * 
 * <p>这个适配器允许Rose I18n框架与Spring的国际化机制完全兼容，
 * 可以在Spring应用中直接使用，支持Spring MVC、Spring Boot等。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class SpringI18nMessageSourceAdapter implements MessageSource {

    private final I18nMessageSource i18nMessageSource;

    /**
     * 构造函数
     * 
     * @param i18nMessageSource Rose I18n消息源
     */
    public SpringI18nMessageSourceAdapter(I18nMessageSource i18nMessageSource) {
        if (i18nMessageSource == null) {
            throw new IllegalArgumentException("I18nMessageSource cannot be null");
        }
        this.i18nMessageSource = i18nMessageSource;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (code == null) {
            return defaultMessage;
        }

        if (locale == null) {
            locale = getDefaultLocale();
        }

        return i18nMessageSource.getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        if (code == null) {
            throw new NoSuchMessageException(code, locale);
        }

        if (locale == null) {
            locale = getDefaultLocale();
        }

        try {
            return i18nMessageSource.getMessage(code, args, locale);
        } catch (I18nMessageNotFoundException e) {
            throw new NoSuchMessageException(code, locale);
        }
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        if (resolvable == null) {
            throw new NoSuchMessageException("", locale);
        }
        
        if (locale == null) {
            locale = getDefaultLocale();
        }
        
        // 尝试解析消息代码
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                try {
                    return i18nMessageSource.getMessage(code, resolvable.getArguments(), locale);
                } catch (I18nMessageNotFoundException e) {
                    // 继续尝试下一个代码
                }
            }
        }
        
        // 如果所有代码都失败，返回默认消息
        String defaultMessage = resolvable.getDefaultMessage();
        if (defaultMessage != null) {
            return defaultMessage;
        }
        
        // 如果没有默认消息，抛出异常
        String code = (codes != null && codes.length > 0) ? codes[0] : "";
        throw new NoSuchMessageException(code, locale);
    }

    /**
     * 获取底层的I18nMessageSource
     * 
     * @return I18nMessageSource实例
     */
    public I18nMessageSource getI18nMessageSource() {
        return i18nMessageSource;
    }

    /**
     * 检查消息是否存在
     *
     * @param code 消息代码
     * @param locale 语言环境
     * @return 如果消息存在返回true，否则返回false
     */
    public boolean containsMessage(String code, Locale locale) {
        if (code == null) {
            return false;
        }

        if (locale == null) {
            locale = getDefaultLocale();
        }

        if (i18nMessageSource instanceof I18nMessageSourceQuery) {
            return ((I18nMessageSourceQuery) i18nMessageSource).containsMessage(code, locale);
        }

        // 回退方法：尝试获取消息
        try {
            i18nMessageSource.getMessage(code, null, locale);
            return true;
        } catch (I18nMessageNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取支持的语言环境
     *
     * @return 支持的语言环境集合
     */
    public java.util.Set<Locale> getSupportedLocales() {
        if (i18nMessageSource instanceof I18nMessageSourceMetadata) {
            return ((I18nMessageSourceMetadata) i18nMessageSource).getSupportedLocales();
        }
        return java.util.Collections.emptySet();
    }

    /**
     * 获取默认语言环境
     *
     * @return 默认语言环境
     */
    public Locale getDefaultLocale() {
        if (i18nMessageSource instanceof I18nMessageSourceMetadata) {
            return ((I18nMessageSourceMetadata) i18nMessageSource).getDefaultLocale();
        }
        return Locale.getDefault();
    }

    /**
     * 刷新消息缓存
     */
    public void refresh() {
        if (i18nMessageSource instanceof I18nMessageSourceLifecycle) {
            ((I18nMessageSourceLifecycle) i18nMessageSource).refresh();
        }
    }

    /**
     * 获取所有消息
     *
     * @param locale 语言环境
     * @return 消息键值对映射
     */
    public java.util.Map<String, String> getAllMessages(Locale locale) {
        if (locale == null) {
            locale = getDefaultLocale();
        }
        if (i18nMessageSource instanceof I18nBatchMessageSource) {
            return ((I18nBatchMessageSource) i18nMessageSource).getAllMessages(locale);
        }
        return java.util.Collections.emptyMap();
    }

    /**
     * 批量获取消息
     *
     * @param keys 消息键集合
     * @param locale 语言环境
     * @return 消息键值对映射
     */
    public java.util.Map<String, String> getMessages(java.util.Set<String> keys, Locale locale) {
        if (locale == null) {
            locale = getDefaultLocale();
        }
        if (i18nMessageSource instanceof I18nBatchMessageSource) {
            return ((I18nBatchMessageSource) i18nMessageSource).getMessages(keys, locale);
        }
        return java.util.Collections.emptyMap();
    }
}
