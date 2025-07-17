package io.github.rose.i18n.spi;

import io.github.rose.core.lang.Prioritized;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 国际化消息提供者SPI接口
 * 
 * <p>基于SPI机制的消息提供者接口，支持可插拔的数据源实现。
 * 不同的实现可以从不同的数据源加载消息，如文件、数据库、HTTP API等。</p>
 * 
 * <p>实现类需要：</p>
 * <ul>
 *   <li>在META-INF/services/io.github.rose.i18n.spi.I18nMessageProvider文件中注册</li>
 *   <li>提供无参构造函数</li>
 *   <li>实现所有必需的方法</li>
 * </ul>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nMessageProvider extends Prioritized {

    /**
     * 获取提供者名称
     * 
     * @return 提供者名称，用于标识和配置
     */
    String getName();

    /**
     * 检查是否支持指定的配置
     * 
     * @param config 配置信息
     * @return 如果支持返回true，否则返回false
     */
    boolean supports(I18nProviderConfig config);

    /**
     * 初始化提供者
     * 
     * @param config 配置信息
     */
    void initialize(I18nProviderConfig config);

    /**
     * 加载指定语言环境的消息
     * 
     * @param locale 语言环境
     * @return 消息键值对映射
     */
    Map<String, String> loadMessages(Locale locale);

    /**
     * 批量加载多个语言环境的消息
     * 
     * @param locales 语言环境集合
     * @return 语言环境到消息映射的映射
     */
    default Map<Locale, Map<String, String>> loadMessages(Set<Locale> locales) {
        return locales.stream()
                .collect(java.util.stream.Collectors.toMap(
                        locale -> locale,
                        this::loadMessages
                ));
    }

    /**
     * 获取支持的语言环境
     * 
     * @return 支持的语言环境集合
     */
    Set<Locale> getSupportedLocales();

    /**
     * 检查是否支持热重载
     * 
     * @return 如果支持热重载返回true，否则返回false
     */
    boolean supportsHotReload();

    /**
     * 检查是否支持写入操作
     * 
     * @return 如果支持写入返回true，否则返回false
     */
    default boolean supportsWrite() {
        return false;
    }

    /**
     * 保存消息（如果支持写入）
     * 
     * @param key 消息键
     * @param value 消息值
     * @param locale 语言环境
     * @throws UnsupportedOperationException 如果不支持写入操作
     */
    default void saveMessage(String key, String value, Locale locale) {
        throw new UnsupportedOperationException("Write operation not supported by " + getName());
    }

    /**
     * 删除消息（如果支持写入）
     * 
     * @param key 消息键
     * @param locale 语言环境
     * @throws UnsupportedOperationException 如果不支持写入操作
     */
    default void deleteMessage(String key, Locale locale) {
        throw new UnsupportedOperationException("Write operation not supported by " + getName());
    }

    /**
     * 添加变更监听器（如果支持热重载）
     * 
     * @param listener 变更监听器
     */
    default void addChangeListener(I18nMessageChangeListener listener) {
        // 默认实现为空，由支持热重载的实现类重写
    }

    /**
     * 移除变更监听器
     * 
     * @param listener 变更监听器
     */
    default void removeChangeListener(I18nMessageChangeListener listener) {
        // 默认实现为空，由支持热重载的实现类重写
    }

    /**
     * 刷新消息缓存
     */
    default void refresh() {
        // 默认实现为空，由支持热重载的实现类重写
    }

    /**
     * 销毁提供者，释放资源
     */
    default void destroy() {
        // 默认实现为空，由需要清理资源的实现类重写
    }

    /**
     * 获取提供者状态信息
     * 
     * @return 状态信息
     */
    default I18nProviderStatus getStatus() {
        return I18nProviderStatus.builder()
                .name(getName())
                .priority(getPriority())
                .supportsHotReload(supportsHotReload())
                .supportsWrite(supportsWrite())
                .supportedLocales(getSupportedLocales())
                .build();
    }
}
