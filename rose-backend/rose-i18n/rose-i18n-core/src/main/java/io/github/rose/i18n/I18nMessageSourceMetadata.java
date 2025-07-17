package io.github.rose.i18n;

import java.util.Locale;
import java.util.Set;

/**
 * 消息源元数据接口
 * 
 * <p>提供消息源的元数据信息，如支持的语言环境、默认语言环境等。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nMessageSourceMetadata {

    /**
     * 获取支持的语言环境列表
     * 
     * @return 支持的语言环境集合
     */
    Set<Locale> getSupportedLocales();

    /**
     * 获取默认语言环境
     * 
     * @return 默认语言环境
     */
    Locale getDefaultLocale();

    /**
     * 获取当前语言环境（从上下文中获取）
     * 
     * <p>通常从ThreadLocal或请求上下文中获取当前用户的语言环境</p>
     * 
     * @return 当前语言环境
     */
    Locale getCurrentLocale();

    /**
     * 检查是否支持指定的语言环境
     * 
     * @param locale 语言环境
     * @return 如果支持返回true，否则返回false
     */
    default boolean supportsLocale(Locale locale) {
        return getSupportedLocales().contains(locale);
    }

    /**
     * 获取消息源的名称
     * 
     * @return 消息源名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取消息源的描述信息
     * 
     * @return 描述信息
     */
    default String getDescription() {
        return "Rose I18n Message Source: " + getName();
    }

    /**
     * 获取消息源的版本信息
     * 
     * @return 版本信息
     */
    default String getVersion() {
        return "1.0.0";
    }
}
