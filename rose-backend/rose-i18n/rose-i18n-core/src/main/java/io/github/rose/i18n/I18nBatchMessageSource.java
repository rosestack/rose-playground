package io.github.rose.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 批量消息操作接口
 * 
 * <p>提供批量获取消息的功能，用于提高性能和减少I/O操作。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nBatchMessageSource {

    /**
     * 批量获取指定键的消息
     * 
     * @param codes 消息键集合
     * @param locale 语言环境
     * @return 消息键值对映射，不存在的键不会包含在结果中
     */
    Map<String, String> getMessages(Set<String> codes, Locale locale);

    /**
     * 获取指定语言环境下的所有消息
     * 
     * @param locale 语言环境
     * @return 所有消息的键值对映射
     */
    Map<String, String> getAllMessages(Locale locale);

    /**
     * 批量获取指定键的消息（带默认值）
     * 
     * @param codes 消息键集合
     * @param defaultMessage 默认消息，当消息不存在时使用
     * @param locale 语言环境
     * @return 消息键值对映射，不存在的键使用默认值
     */
    default Map<String, String> getMessages(Set<String> codes, String defaultMessage, Locale locale) {
        Map<String, String> result = getMessages(codes, locale);
        
        // 为不存在的键添加默认值
        if (defaultMessage != null) {
            for (String code : codes) {
                result.putIfAbsent(code, defaultMessage);
            }
        }
        
        return result;
    }

    /**
     * 批量检查消息是否存在
     *
     * @param codes 消息键集合
     * @param locale 语言环境
     * @return 消息键到存在状态的映射
     */
    default Map<String, Boolean> batchContainsMessages(Set<String> codes, Locale locale) {
        Map<String, String> messages = getMessages(codes, locale);
        return codes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        code -> code,
                        messages::containsKey
                ));
    }
}
