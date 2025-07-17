package io.github.rose.i18n;

import java.util.Locale;

/**
 * Rose I18n 消息源核心接口
 *
 * <p>基于SPI机制设计的现代化国际化消息源接口，提供简洁而强大的API。
 * 支持多种数据源、热重载、缓存等企业级特性。</p>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li>简洁性：专注于消息获取的核心功能</li>
 *   <li>一致性：与Spring MessageSource保持参数顺序一致</li>
 *   <li>扩展性：基于SPI机制，支持可插拔的实现</li>
 *   <li>易用性：支持默认值、参数化消息等</li>
 * </ul>
 *
 * <p>参数顺序与Spring MessageSource保持一致：</p>
 * <ul>
 *   <li>code/key: 消息键</li>
 *   <li>args: 消息参数</li>
 *   <li>defaultMessage: 默认消息（可选）</li>
 *   <li>locale: 语言环境</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nMessageSource {

    /**
     * 获取消息（带参数和默认值）
     *
     * <p>参数顺序与Spring MessageSource保持一致</p>
     *
     * @param code 消息键
     * @param args 消息参数，可为null
     * @param defaultMessage 默认消息，当找不到消息时返回
     * @param locale 语言环境
     * @return 格式化后的消息
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    /**
     * 获取消息（带参数）
     *
     * <p>参数顺序与Spring MessageSource保持一致</p>
     *
     * @param code 消息键
     * @param args 消息参数，可为null
     * @param locale 语言环境
     * @return 格式化后的消息
     * @throws I18nMessageNotFoundException 当消息不存在时抛出
     */
    String getMessage(String code, Object[] args, Locale locale) throws I18nMessageNotFoundException;

    /**
     * 获取简单消息（无参数）
     *
     * @param code 消息键
     * @param locale 语言环境
     * @return 消息内容
     * @throws I18nMessageNotFoundException 当消息不存在时抛出
     */
    default String getSimpleMessage(String code, Locale locale) throws I18nMessageNotFoundException {
        return getMessage(code, null, locale);
    }

    /**
     * 获取简单消息（带默认值）
     *
     * @param code 消息键
     * @param defaultMessage 默认消息
     * @param locale 语言环境
     * @return 消息内容
     */
    default String getSimpleMessage(String code, String defaultMessage, Locale locale) {
        return getMessage(code, null, defaultMessage, locale);
    }
}
