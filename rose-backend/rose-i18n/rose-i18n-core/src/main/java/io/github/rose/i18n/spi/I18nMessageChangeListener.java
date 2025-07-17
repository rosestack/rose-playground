package io.github.rose.i18n.spi;

import java.util.Locale;

/**
 * 国际化消息变更监听器
 * 
 * <p>用于监听消息的变更事件，支持热重载功能。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nMessageChangeListener {

    /**
     * 消息添加事件
     * 
     * @param key 消息键
     * @param value 消息值
     * @param locale 语言环境
     * @param source 事件源
     */
    default void onMessageAdded(String key, String value, Locale locale, Object source) {
        onMessageChanged(key, value, locale, source, ChangeType.ADDED);
    }

    /**
     * 消息更新事件
     * 
     * @param key 消息键
     * @param oldValue 旧消息值
     * @param newValue 新消息值
     * @param locale 语言环境
     * @param source 事件源
     */
    default void onMessageUpdated(String key, String oldValue, String newValue, Locale locale, Object source) {
        onMessageChanged(key, newValue, locale, source, ChangeType.UPDATED);
    }

    /**
     * 消息删除事件
     * 
     * @param key 消息键
     * @param locale 语言环境
     * @param source 事件源
     */
    default void onMessageDeleted(String key, Locale locale, Object source) {
        onMessageChanged(key, null, locale, source, ChangeType.DELETED);
    }

    /**
     * 消息变更事件（通用方法）
     * 
     * @param key 消息键
     * @param value 消息值
     * @param locale 语言环境
     * @param source 事件源
     * @param changeType 变更类型
     */
    void onMessageChanged(String key, String value, Locale locale, Object source, ChangeType changeType);

    /**
     * 批量消息变更事件
     * 
     * @param locale 语言环境
     * @param source 事件源
     */
    default void onMessagesReloaded(Locale locale, Object source) {
        // 默认实现为空，由具体实现类决定是否处理
    }

    /**
     * 变更类型枚举
     */
    enum ChangeType {
        ADDED,
        UPDATED,
        DELETED,
        RELOADED
    }
}
