package io.github.rosestack.i18n.spring.boot.autoconfigure;

import io.github.rosestack.i18n.cache.CacheProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Locale;

/**
 * I18n 配置属性类
 *
 * <p>提供国际化模块的配置属性绑定，支持缓存、本地化等功能的配置。
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.i18n")
public class I18nProperties {
	/**
	 * 消息源列表
	 */
	private List<String> sources;

	/**
	 * 默认语言环境
	 */
	private Locale defaultLocale = Locale.getDefault();

	/**
	 * 支持的语言环境列表
	 */
	private List<Locale> supportedLocales;

	/**
	 * 缓存配置
	 */
	private CacheProperties cache = new CacheProperties();
}
