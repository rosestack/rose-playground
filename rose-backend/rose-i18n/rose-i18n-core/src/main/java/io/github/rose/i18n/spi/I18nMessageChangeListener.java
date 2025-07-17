package io.github.rose.i18n.spi;

import java.util.Locale;

/**
 * 国际化消息变更监听器
 * 
 * <p>用于监听消息的变更事件，支持热重载功能。</p>
 */
public interface I18nMessageChangeListener {

    /**
     * 消息变更类型
     */
    enum ChangeType {
        ADDED, UPDATED, DELETED
    }

    /**
     * 消息变更事件（通用方法）
     * @param key 消息键
     * @param value 消息值
     * @param locale 语言环境
     * @param source 事件源
     * @param changeType 变更类型
     */
    void onMessageChanged(String key, String value, Locale locale, Object source, ChangeType changeType);

    /**
     * 批量消息重载事件
     * @param locale 语言环境
     * @param source 事件源
     */
    default void onMessagesReloaded(Locale locale, Object source) {
        // 默认实现为空，由具体实现类决定是否处理
    }
} 