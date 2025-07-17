package io.github.rose.i18n;

import jakarta.annotation.Nonnull;

import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;

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
public interface I18nMessageSource extends Lifecycle {

    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    default String getMessage(String code, Object[] args, Locale locale) {
        return getMessage(code, args, null, locale);
    }

    default String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    default Set<Locale> getSupportedLocales() {
        return Set.of(Locale.getDefault(), Locale.ENGLISH);
    }
}
