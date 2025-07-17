package io.github.rose.i18n;

import java.util.Locale;

/**
 * 国际化消息变更监听器
 *
 * <p>用于监听消息的变更事件，支持热重载功能。</p>
 */
public interface MessageSourceChangeListener {

    /**
     * 消息变更类型
     */
    enum ChangeType {
        ADDED, UPDATED, DELETED
    }

    /**
     * 消息变更事件（通用方法）
     *
     * @param key        消息键
     * @param value      消息值
     * @param locale     语言环境
     * @param source     事件源
     * @param changeType 变更类型
     */
    void onMessageChanged(String key, String value, Locale locale, ReloadedResourceMessageSource source, ChangeType changeType);

    /**
     * 批量消息重载事件
     *
     * @param resource 资源文件
     * @param source   事件源
     */
    default void onMessagesReloaded(String resource, ReloadedResourceMessageSource source) {
        //TODO resource 是绝对路径？
        source.reload(resource);
    }
} 